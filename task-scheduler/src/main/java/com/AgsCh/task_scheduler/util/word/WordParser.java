package com.AgsCh.task_scheduler.util.word;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.web.multipart.MultipartFile;

import com.AgsCh.task_scheduler.dto.request.FunctionRequestDTO;

public class WordParser {

    /*
     * =========================
     * PUBLIC METHODS
     * =========================
     */

    public static Map<String, List<String>> parseTasks(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath);
                XWPFDocument document = new XWPFDocument(fis)) {

            return parseTasksFromDocument(document);

        } catch (IOException e) {
            throw new RuntimeException("Error leyendo el archivo Word", e);
        }
    }

    public static Map<String, List<String>> parseTasks(MultipartFile file) {
        try (InputStream is = file.getInputStream();
                XWPFDocument document = new XWPFDocument(is)) {

            return parseTasksFromDocument(document);

        } catch (IOException e) {
            throw new RuntimeException("Error leyendo el archivo Word", e);
        }
    }

    public static List<FunctionRequestDTO> parseFunctionsFromWord(MultipartFile file) {
        try (InputStream is = file.getInputStream();
                XWPFDocument document = new XWPFDocument(is)) {

            return parseFunctions(document);

        } catch (IOException e) {
            throw new RuntimeException("Error leyendo el archivo Word", e);
        }
    }

    private static final Set<String> DAYS = Set.of(

            // Español
            "lunes",
            "martes",
            "miércoles",
            "miercoles",
            "jueves",
            "viernes",
            "sábado",
            "sabado",
            "domingo",

            // Portugués
            "segunda",
            "terça",
            "terca",
            "quarta",
            "quinta",
            "sexta");

    private static List<String> lastCalendarDays = new ArrayList<>();

    private static boolean isDay(String text) {

        if (text == null)
            return false;

        text = text.trim().toLowerCase();

        return DAYS.contains(text);
    }

    /*
     * =========================
     * TASK PARSER (MAP)
     * =========================
     */

    private static Map<String, List<String>> parseTasksFromDocument(XWPFDocument document) {

        Map<String, List<String>> tasks = new LinkedHashMap<>();

        for (XWPFTable table : document.getTables()) {

            if (!isCalendarTable(table))
                continue;

            List<XWPFTableRow> rows = table.getRows();
            if (rows.size() < 2)
                continue;

            List<String> days = new ArrayList<>();
            List<XWPFTableCell> headerCells = rows.get(0).getTableCells();

            for (int i = 1; i < headerCells.size(); i++) {
                days.add(headerCells.get(i).getText().trim());
            }

            for (int r = 1; r < rows.size(); r++) {
                List<XWPFTableCell> cells = rows.get(r).getTableCells();

                if (cells.isEmpty())
                    continue;

                String taskName = cells.get(0).getText().trim();
                if (taskName.isEmpty())
                    continue;

                List<String> assignedDays = new ArrayList<>();

                for (int c = 1; c < cells.size(); c++) {
                    if (!cells.get(c).getText().trim().isEmpty() && c - 1 < days.size()) {
                        assignedDays.add(days.get(c - 1));
                    }
                }

                tasks.put(taskName, assignedDays);
            }

            break; // usamos solo la primera tabla válida
        }

        return tasks;
    }

    /*
     * =========================
     * MAIN FUNCTION PARSER
     * =========================
     */

    private static List<FunctionRequestDTO> parseFunctions(XWPFDocument document) {

        List<FunctionRequestDTO> result = new ArrayList<>();

        List<XWPFTable> tables = document.getTables();

        for (int i = 0; i < tables.size(); i++) {

            XWPFTable table = tables.get(i);

            System.out.println("\nTabla " + i);

            boolean calendar = isCalendarTable(table);
            boolean simple = isSimpleFunctionTable(table);

            System.out.println("isCalendarTable = " + calendar);
            System.out.println("isSimpleFunctionTable = " + simple);

            if (calendar) {

                System.out.println(">>> ENTRA A parseCalendarTable()");

                // Por defecto asumimos el formato viejo (español):
                // encabezado y datos en la misma tabla.
                XWPFTable dataTable = table;

                // Si la tabla calendario NO tiene funciones (#),
                // buscamos si la siguiente tabla sí las tiene.
                if (!simple && i + 1 < tables.size()) {

                    XWPFTable nextTable = tables.get(i + 1);

                    if (isSimpleFunctionTable(nextTable)) {

                        System.out.println(">>> Usa la tabla siguiente como datos");

                        dataTable = nextTable;

                        // Saltamos la siguiente tabla porque ya la procesamos
                        i++;
                    }
                }

                result.addAll(parseCalendarTable(table, dataTable));
                continue;
            }

            if (isImplicitCalendarTable(table)) {

                System.out.println(">>> ENTRA A parseImplicitCalendarTable()");
                result.addAll(parseImplicitCalendarTable(table));
                continue;
            }

            if (simple) {

                System.out.println(">>> ENTRA A parseSimpleTable()");
                result.addAll(parseSimpleTable(table));
            }
        }

        return result;
    }

    /*
     * =========================
     * CALENDAR TABLE PARSER
     * =========================
     */

    private static List<FunctionRequestDTO> parseCalendarTable(
            XWPFTable headerTable,
            XWPFTable dataTable) {

        List<FunctionRequestDTO> result = new ArrayList<>();

        List<XWPFTableRow> headerRows = headerTable.getRows();
        List<XWPFTableRow> dataRows = dataTable.getRows();

        int headerRow = findHeaderRow(headerTable);

        if (headerRow == -1)
            return result;

        List<String> days = new ArrayList<>();
        List<XWPFTableCell> headerCells = headerRows.get(headerRow).getTableCells();

        for (XWPFTableCell cell : headerCells) {
            days.add(cell.getText().trim());
        }

        lastCalendarDays = new ArrayList<>(days);

        System.out.println("\n==============================================");
        System.out.println("NUEVA TABLA CALENDARIO");
        System.out.println("Filas: " + dataRows.size());
        System.out.println("Días: " + days);
        System.out.println("==============================================");

        Map<String, Map<String, Integer>> functionCounts = new LinkedHashMap<>();

        // Si headerTable y dataTable son la misma tabla (formato español),
        // empezamos después del encabezado.
        // Si son distintas (formato portugués), empezamos desde la primera fila.
        int startRow = (headerTable == dataTable)
                ? headerRow + 1
                : 0;

        for (int r = startRow; r < dataRows.size(); r++) {

            List<XWPFTableCell> cells = dataRows.get(r).getTableCells();

            System.out.println("\nFila " + r + " (" + cells.size() + " celdas)");

            for (int c = 0; c < cells.size(); c++) {

                String day = (c < days.size()) ? days.get(c) : null;
                String text = cells.get(c).getText().trim();

                System.out.printf(
                        "  r=%d c=%d day=%s text=[%s]%n",
                        r,
                        c,
                        day,
                        text);

                if (day == null || text.isEmpty())
                    continue;

                if (!text.contains("#"))
                    continue;

                String functionName = normalizeFunctionName(text);

                functionCounts.putIfAbsent(functionName, new LinkedHashMap<>());
                Map<String, Integer> dayCounts = functionCounts.get(functionName);

                int newValue = dayCounts.getOrDefault(day, 0) + 1;
                dayCounts.put(day, newValue);

                System.out.printf(
                        "      -> %s | %s = %d%n",
                        functionName,
                        day,
                        newValue);
            }
        }

        System.out.println("\n=========== MAPA FINAL ===========");

        for (Map.Entry<String, Map<String, Integer>> e : functionCounts.entrySet()) {
            System.out.println(e.getKey() + " -> " + e.getValue());
        }

        System.out.println("==================================\n");

        long idCounter = 1L;

        for (Map.Entry<String, Map<String, Integer>> entry : functionCounts.entrySet()) {

            Set<DayOfWeek> assignedDays = new HashSet<>();
            int requiredPersons = 0;

            for (Map.Entry<String, Integer> dayEntry : entry.getValue().entrySet()) {
                assignedDays.add(mapDay(dayEntry.getKey()));
                requiredPersons = Math.max(requiredPersons, dayEntry.getValue());
            }

            System.out.printf(
                    "DTO -> %s | requiredPersons=%d | assignedDays=%s%n",
                    entry.getKey(),
                    requiredPersons,
                    assignedDays);

            FunctionRequestDTO dto = new FunctionRequestDTO();
            dto.setId(idCounter++);
            dto.setName(entry.getKey());
            dto.setAssignedDays(assignedDays);
            dto.setSequential(false);
            dto.setRequiredPersons(requiredPersons);

            result.add(dto);
        }

        return result;
    }

    private static List<FunctionRequestDTO> parseImplicitCalendarTable(XWPFTable table) {

        if (lastCalendarDays.isEmpty()) {
            return List.of();
        }

        Map<String, Map<String, Integer>> functionCounts = new LinkedHashMap<>();

        List<XWPFTableCell> cells = table.getRow(0).getTableCells();

        for (int c = 0; c < cells.size() && c < lastCalendarDays.size(); c++) {

            String text = cells.get(c).getText().trim();

            if (!text.contains("#"))
                continue;

            String function = normalizeFunctionName(text);

            functionCounts.putIfAbsent(function, new LinkedHashMap<>());

            Map<String, Integer> days = functionCounts.get(function);

            String day = lastCalendarDays.get(c);

            days.put(day, days.getOrDefault(day, 0) + 1);
        }

        List<FunctionRequestDTO> result = new ArrayList<>();

        long idCounter = 1;

        for (Map.Entry<String, Map<String, Integer>> entry : functionCounts.entrySet()) {

            FunctionRequestDTO dto = new FunctionRequestDTO();

            dto.setId(idCounter++);
            dto.setName(entry.getKey());

            Set<DayOfWeek> assigned = new HashSet<>();

            int persons = 0;

            for (Map.Entry<String, Integer> e : entry.getValue().entrySet()) {
                assigned.add(mapDay(e.getKey()));
                persons = Math.max(persons, e.getValue());
            }

            dto.setAssignedDays(assigned);
            dto.setRequiredPersons(persons);
            dto.setSequential(false);

            result.add(dto);
        }

        return result;
    }

    /*
     * =========================
     * SIMPLE TABLE PARSER
     * =========================
     */

    private static List<FunctionRequestDTO> parseSimpleTable(XWPFTable table) {

        Map<String, Integer> functionCount = new LinkedHashMap<>();

        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {

                String text = cell.getText().trim();

                if (!text.contains("#"))
                    continue;

                String functionName = normalizeFunctionName(text);

                functionCount.put(functionName,
                        functionCount.getOrDefault(functionName, 0) + 1);
            }
        }

        List<FunctionRequestDTO> result = new ArrayList<>();
        long idCounter = 1L;

        for (Map.Entry<String, Integer> entry : functionCount.entrySet()) {

            FunctionRequestDTO dto = new FunctionRequestDTO();
            dto.setId(idCounter++);
            dto.setName(entry.getKey());
            dto.setAssignedDays(new HashSet<>());
            dto.setSequential(false);
            dto.setRequiredPersons(entry.getValue());

            result.add(dto);
        }

        System.out.println("\n===== SIMPLE TABLE =====");

        for (FunctionRequestDTO dto : result) {
            System.out.println(
                    dto.getName()
                            + " | "
                            + dto.getRequiredPersons()
                            + " | "
                            + dto.getAssignedDays());
        }

        System.out.println("========================\n");

        return result;
    }

    /*
     * =========================
     * DETECTION METHODS
     * =========================
     */

    private static boolean isCalendarTable(XWPFTable table) {
        return findHeaderRow(table) != -1;
    }

    private static boolean isSimpleFunctionTable(XWPFTable table) {

        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                if (cell.getText().contains("#")) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean isImplicitCalendarTable(XWPFTable table) {

        if (table.getRows().size() != 1)
            return false;

        List<XWPFTableCell> cells = table.getRow(0).getTableCells();

        if (cells.size() != 7)
            return false;

        int placeholders = 0;

        for (XWPFTableCell cell : cells) {

            if (cell.getText().contains("#")) {
                placeholders++;
            }
        }

        return placeholders == 7;
    }

    /*
     * =========================
     * HELPERS
     * =========================
     */

    private static String normalizeFunctionName(String text) {
        return text
                .replaceAll("[#]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static DayOfWeek mapDay(String day) {

        String normalized = day
                .trim()
                .toLowerCase()
                .replace(":", "")
                .replace(".", "");

        return switch (normalized) {

            // Español
            case "lunes" -> DayOfWeek.MONDAY;
            case "martes" -> DayOfWeek.TUESDAY;
            case "miércoles", "miercoles" -> DayOfWeek.WEDNESDAY;
            case "jueves" -> DayOfWeek.THURSDAY;
            case "viernes" -> DayOfWeek.FRIDAY;
            case "sábado", "sabado" -> DayOfWeek.SATURDAY;
            case "domingo" -> DayOfWeek.SUNDAY;

            // Portugués
            case "segunda" -> DayOfWeek.MONDAY;
            case "terça", "terca" -> DayOfWeek.TUESDAY;
            case "quarta" -> DayOfWeek.WEDNESDAY;
            case "quinta" -> DayOfWeek.THURSDAY;
            case "sexta" -> DayOfWeek.FRIDAY;

            default -> throw new IllegalArgumentException("Día inválido: [" + day + "]");
        };
    }

    private static int findHeaderRow(XWPFTable table) {

        List<XWPFTableRow> rows = table.getRows();

        for (int r = 0; r < rows.size(); r++) {

            int validDays = 0;

            for (XWPFTableCell cell : rows.get(r).getTableCells()) {

                if (isDay(cell.getText())) {
                    validDays++;
                }
            }

            // Si encontramos al menos 5 días, asumimos que es el encabezado
            if (validDays >= 5) {
                return r;
            }
        }

        return -1;
    }
}