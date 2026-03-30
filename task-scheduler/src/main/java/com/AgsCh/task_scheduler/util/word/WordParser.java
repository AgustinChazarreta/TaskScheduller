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

        for (XWPFTable table : document.getTables()) {

            if (isCalendarTable(table)) {
                result.addAll(parseCalendarTable(table));
                continue;
            }

            if (isSimpleFunctionTable(table)) {
                result.addAll(parseSimpleTable(table));
            }
        }

        int i = 0;
        for (XWPFTable table : document.getTables()) {
            System.out.println("TABLA " + i++);

            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    System.out.print("[" + cell.getText() + "]");
                }
                System.out.println();
            }
        }

        return result;
    }

    /*
     * =========================
     * CALENDAR TABLE PARSER
     * =========================
     */

    private static List<FunctionRequestDTO> parseCalendarTable(XWPFTable table) {

        List<FunctionRequestDTO> result = new ArrayList<>();
        List<XWPFTableRow> rows = table.getRows();

        if (rows.size() < 2)
            return result;

        List<String> days = new ArrayList<>();
        List<XWPFTableCell> headerCells = rows.get(0).getTableCells();

        for (XWPFTableCell cell : headerCells) {
            days.add(cell.getText().trim());
        }

        Map<String, Map<String, Integer>> functionCounts = new LinkedHashMap<>();

        for (int r = 1; r < rows.size(); r++) {

            List<XWPFTableCell> cells = rows.get(r).getTableCells();

            for (int c = 0; c < cells.size(); c++) {

                String day = (c < days.size()) ? days.get(c) : null;
                String text = cells.get(c).getText().trim();

                if (day == null || text.isEmpty())
                    continue;

                if (!text.contains("#"))
                    continue;

                String functionName = normalizeFunctionName(text);

                functionCounts.putIfAbsent(functionName, new LinkedHashMap<>());
                Map<String, Integer> dayCounts = functionCounts.get(functionName);

                dayCounts.put(day, dayCounts.getOrDefault(day, 0) + 1);
            }
        }

        long idCounter = 1L;

        for (Map.Entry<String, Map<String, Integer>> entry : functionCounts.entrySet()) {

            Set<DayOfWeek> assignedDays = new HashSet<>();
            int requiredPersons = 0;

            for (Map.Entry<String, Integer> dayEntry : entry.getValue().entrySet()) {
                assignedDays.add(mapDay(dayEntry.getKey()));
                requiredPersons = Math.max(requiredPersons, dayEntry.getValue());
            }

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

        return result;
    }

    /*
     * =========================
     * DETECTION METHODS
     * =========================
     */

    private static boolean isCalendarTable(XWPFTable table) {

        if (table.getRows().isEmpty())
            return false;

        List<XWPFTableCell> headerCells = table.getRows().get(0).getTableCells();

        int validDays = 0;

        for (XWPFTableCell cell : headerCells) {

            String text = cell.getText().trim().toLowerCase();

            if (text.contains("lunes") ||
                    text.contains("martes") ||
                    text.contains("miercoles") ||
                    text.contains("miércoles") ||
                    text.contains("jueves") ||
                    text.contains("viernes") ||
                    text.contains("sabado") ||
                    text.contains("sábado") ||
                    text.contains("domingo")) {

                validDays++;
            }
        }

        return validDays >= 2;
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
            case "lunes" -> DayOfWeek.MONDAY;
            case "martes" -> DayOfWeek.TUESDAY;
            case "miércoles", "miercoles" -> DayOfWeek.WEDNESDAY;
            case "jueves" -> DayOfWeek.THURSDAY;
            case "viernes" -> DayOfWeek.FRIDAY;
            case "sábado", "sabado" -> DayOfWeek.SATURDAY;
            case "domingo" -> DayOfWeek.SUNDAY;
            default -> throw new IllegalArgumentException("Día inválido: [" + day + "]");
        };
    }
}