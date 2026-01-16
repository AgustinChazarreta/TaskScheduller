package com.AgsCh.task_scheduler.util.word;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.web.multipart.MultipartFile;

/**
 * Utilitario para parsear archivos Word y extraer tareas con sus días
 * asignados.
 * Se espera que el Word contenga una tabla donde:
 * - Primera fila: encabezados con los días.
 * - Primera columna: nombres de las tareas.
 */
public class WordParser {

    /**
     * Parseo de tareas desde un archivo Word usando path (tests / herramientas
     * internas).
     */
    public static Map<String, List<String>> parseTasks(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath);
                XWPFDocument document = new XWPFDocument(fis)) {

            return parseDocument(document);

        } catch (IOException e) {
            throw new RuntimeException("Error leyendo el archivo Word", e);
        }
    }

    /**
     * Parseo de tareas desde un archivo Word recibido por multipart (endpoint
     * REST).
     */
    public static Map<String, List<String>> parseTasks(MultipartFile file) {
        try (InputStream is = file.getInputStream();
                XWPFDocument document = new XWPFDocument(is)) {

            return parseDocument(document);

        } catch (IOException e) {
            throw new RuntimeException("Error leyendo el archivo Word", e);
        }
    }

    /**
     * Lógica central de parseo del documento Word.
     */
    private static Map<String, List<String>> parseDocument(XWPFDocument document) {

        Map<String, List<String>> tasks = new LinkedHashMap<>();

        if (document.getTables().isEmpty()) {
            return tasks;
        }

        XWPFTable table = document.getTables().get(0);
        List<XWPFTableRow> rows = table.getRows();
        if (rows.size() < 2) {
            return tasks;
        }

        // Encabezados: días de la semana
        List<String> days = new ArrayList<>();
        List<XWPFTableCell> headerCells = rows.get(0).getTableCells();
        for (int i = 1; i < headerCells.size(); i++) {
            days.add(headerCells.get(i).getText().trim());
        }

        // Filas de tareas
        for (int r = 1; r < rows.size(); r++) {
            List<XWPFTableCell> cells = rows.get(r).getTableCells();
            String taskName = cells.get(0).getText().trim();
            if (taskName.isEmpty())
                continue;

            List<String> assignedDays = new ArrayList<>();
            for (int c = 1; c < cells.size() && c <= days.size(); c++) {
                if (!cells.get(c).getText().trim().isEmpty()) {
                    assignedDays.add(days.get(c - 1));
                }
            }

            tasks.put(taskName, assignedDays);
        }

        return tasks;
    }
}
