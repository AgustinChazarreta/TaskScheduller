package com.AgsCh.task_scheduler.service.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import java.awt.Color;

import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.FunctionAssignment;
import com.AgsCh.task_scheduler.model.House;
import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.model.ScheduleRun;
import com.AgsCh.task_scheduler.repository.ScheduleRunRepository;
import com.AgsCh.task_scheduler.service.solver.ScheduleService;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;

@Service
public class AdminScheduleService {

    private final ScheduleService solverService;
    private final ScheduleRunRepository scheduleRunRepository;
    private final EmailService emailService;

    public AdminScheduleService(
            ScheduleService solverService,
            ScheduleRunRepository scheduleRunRepository,
            EmailService emailService) {

        this.solverService = solverService;
        this.scheduleRunRepository = scheduleRunRepository;
        this.emailService = emailService;
    }

    /*
     * =========================
     * GENERAR Y RESOLVER
     * =========================
     */

    @Transactional
    public Schedule solve(Schedule schedule, House house) {

        if (schedule == null) {
            throw new BusinessException("No hay Schedule para resolver");
        }

        if (schedule.getStartDate() == null || schedule.getEndDate() == null) {
            throw new BusinessException("El Schedule no tiene rango de fechas definido");
        }

        // 1️⃣ Archivar el último run (ACTIVE o INVALIDATED)
        scheduleRunRepository.findTopByHouse_IdOrderByCreatedAtDesc(house.getId())
                .ifPresent(run -> {
                    if (run.getStatus() != ScheduleRun.Status.ARCHIVED) {
                        run.setStatus(ScheduleRun.Status.ARCHIVED);
                        scheduleRunRepository.save(run);
                    }
                });

        // 2️⃣ Resolver
        Schedule solvedSchedule = solverService.solve(schedule);

        if (solvedSchedule.getScore() == null) {
            throw new BusinessException("El solver no devolvió score");
        }

        // 3️⃣ Crear nuevo ACTIVE
        ScheduleRun run = new ScheduleRun(
                solvedSchedule.getStartDate(),
                solvedSchedule.getEndDate(),
                solvedSchedule.getScore().toString(),
                house);

        run.setStatus(ScheduleRun.Status.ACTIVE);

        for (FunctionAssignment assignment : solvedSchedule.getFunctionAssignmentList()) {
            run.addAssignment(assignment);
        }

        scheduleRunRepository.save(run);

        return solvedSchedule;
    }

    /*
     * =========================
     * OBTENER RUN ACTIVO
     * =========================
     */

    public ScheduleRun getActiveRunByHouse(Long houseId) {
        Optional<ScheduleRun> run = scheduleRunRepository
                .findByHouseIdAndStatus(houseId, ScheduleRun.Status.ACTIVE);

        return run.orElse(null);
    }

    /*
     * =========================
     * INVALIDAR (archivar activo)
     * =========================
     */

    @Transactional
    public void invalidate(House house) {

        scheduleRunRepository
                .findByHouseIdAndStatus(house.getId(), ScheduleRun.Status.ACTIVE)
                .ifPresent(run -> {
                    run.setStatus(ScheduleRun.Status.INVALIDATED);
                    scheduleRunRepository.save(run);
                });
    }

    public ScheduleRun getLastRunByHouse(Long houseId) {
        return scheduleRunRepository
                .findTopByHouse_IdOrderByCreatedAtDesc(houseId)
                .orElse(null);
    }

    @Transactional
    public ScheduleRun createNewRun(Schedule schedule, House house) {
        if (schedule == null) {
            throw new BusinessException("No hay Schedule para crear nueva versión");
        }

        if (schedule.getStartDate() == null || schedule.getEndDate() == null) {
            throw new BusinessException("El Schedule no tiene rango de fechas definido");
        }

        // 1️⃣ Archivar el último run activo o invalidado
        scheduleRunRepository.findTopByHouse_IdOrderByCreatedAtDesc(house.getId())
                .ifPresent(run -> {
                    if (run.getStatus() != ScheduleRun.Status.ARCHIVED) {
                        run.setStatus(ScheduleRun.Status.ARCHIVED);
                        scheduleRunRepository.save(run);
                    }
                });

        // 2️⃣ Crear nuevo ACTIVE run con las asignaciones ya editadas
        ScheduleRun newRun = new ScheduleRun(
                schedule.getStartDate(),
                schedule.getEndDate(),
                schedule.getScore() != null ? schedule.getScore().toString() : "0",
                house);
        newRun.setStatus(ScheduleRun.Status.ACTIVE);

        if (schedule.getFunctionAssignmentList() != null) {
            for (FunctionAssignment assignment : schedule.getFunctionAssignmentList()) {
                newRun.addAssignment(assignment);
            }
        }

        // 3️⃣ Guardar en DB
        scheduleRunRepository.save(newRun);

        return newRun;
    }

    private byte[] generatePdf(List<FunctionAssignment> assignments) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos); // indispensable
            document.open();

            // -----------------------------
            // FUENTES
            // -----------------------------
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font periodFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.GRAY);
            Font userFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
            Font monthFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 19, Color.BLACK);
            Font dayFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.DARK_GRAY);
            Font fnFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);

            float pageWidth = PageSize.A4.getWidth();
            float pageHeight = PageSize.A4.getHeight();
            int numColumns = 3;

            // -----------------------------
            // LOGO
            // -----------------------------
            try {
                Image logo = Image.getInstance("src/main/resources/static/logo.png");
                float logoWidth = 90f;
                float logoHeight = 90f;
                logo.scaleToFit(logoWidth, logoHeight);
                logo.setAbsolutePosition((pageWidth - logoWidth) / 2, pageHeight - 110); // centrado arriba
                document.add(logo);
            } catch (Exception e) {
                System.err.println("No se pudo cargar el logo: " + e.getMessage());
            }

            // -----------------------------
            // HEADER
            // -----------------------------
            Paragraph title = new Paragraph("SCHEDULE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(70f);
            title.setSpacingAfter(5f);
            document.add(title);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate startDate = assignments.get(0).getDate();
            LocalDate endDate = assignments.get(assignments.size() - 1).getDate();

            Paragraph period = new Paragraph(
                    String.format("Período: %s - %s", startDate.format(formatter), endDate.format(formatter)),
                    periodFont);
            period.setAlignment(Element.ALIGN_CENTER);
            period.setSpacingAfter(3f);
            document.add(period);

            // texto debajo del período
            Paragraph userInfo = new Paragraph(
                    String.format("Usuario: %s | Casa: %s",
                            assignments.get(0).getPerson().getFullName(),
                            assignments.get(0).getPerson().getHouse().getName()),
                    userFont);
            userInfo.setAlignment(Element.ALIGN_CENTER);
            userInfo.setSpacingAfter(15f);
            document.add(userInfo);

            // -----------------------------
            // AGRUPAR POR DÍA
            // -----------------------------
            Map<LocalDate, List<FunctionAssignment>> assignmentsByDay = new TreeMap<>();
            for (FunctionAssignment a : assignments) {
                assignmentsByDay.computeIfAbsent(a.getDate(), k -> new ArrayList<>()).add(a);
            }

            // -----------------------------
            // CREAR TABLA DE COLUMNAS
            // -----------------------------
            PdfPTable table = new PdfPTable(numColumns);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.getDefaultCell().setBorderColor(Color.WHITE);

            int col = 0;
            int currentMonth = -1;
            for (LocalDate date : assignmentsByDay.keySet()) {
                int month = date.getMonthValue();
                int year = date.getYear();

                // NUEVO MES
                if (currentMonth == -1 || month != currentMonth) {
                    if (currentMonth != -1) {
                        document.add(table);
                        document.newPage();
                        table = new PdfPTable(numColumns);
                        table.setWidthPercentage(100);
                        table.getDefaultCell().setBorderColor(Color.WHITE);
                        col = 0;
                    }
                    currentMonth = month;

                    String monthName = date.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "AR"));
                    String monthTitle = monthName.substring(0, 1).toUpperCase() + monthName.substring(1) + " " + year;

                    Paragraph monthPara = new Paragraph(monthTitle, monthFont);
                    monthPara.setAlignment(Element.ALIGN_LEFT);
                    monthPara.setSpacingAfter(10f);
                    document.add(monthPara);
                }

                // CELDA DEL DÍA
                PdfPCell dayCell = new PdfPCell();
                dayCell.setBorderColor(Color.WHITE);
                dayCell.setPadding(5f);

                // header del día
                String dayName = date.getDayOfWeek()
                        .getDisplayName(TextStyle.FULL, new Locale("es", "AR"));

                dayName = dayName.substring(0, 1).toUpperCase() + dayName.substring(1);

                String dayLabel = date.getDayOfMonth() + " " + dayName;
                Paragraph dayPara = new Paragraph(dayLabel, dayFont);
                dayPara.setSpacingAfter(3f);
                dayCell.addElement(dayPara);

                // funciones
                for (FunctionAssignment fa : assignmentsByDay.get(date)) {
                    Paragraph fnPara = new Paragraph("• " + fa.getFunction().getName(), fnFont);
                    fnPara.setIndentationLeft(10f);
                    fnPara.setSpacingAfter(2f);
                    dayCell.addElement(fnPara);
                }

                table.addCell(dayCell);
                col++;

                // completar fila si es la última
                if (col == numColumns)
                    col = 0;
            }

            // rellenar celdas vacías si la última fila no está completa
            while (col > 0 && col < numColumns) {
                PdfPCell empty = new PdfPCell();
                empty.setBorder(Rectangle.NO_BORDER);
                table.addCell(empty);
                col++;
            }

            document.add(table);
            document.close();

            return baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generando PDF estilizado", e);
        }
    }

    public void sendPdfToPerson(Person person, List<FunctionAssignment> allAssignments) {
        List<FunctionAssignment> personAssignments = allAssignments.stream()
                .filter(a -> a.getPerson().getFullName().equals(person.getFullName()))
                .toList();

        if (personAssignments.isEmpty())
            return;

        byte[] pdfBytes = generatePdf(personAssignments);

        // Mensaje profesional
        String emailBody = """
                <p>Estimado/a %s,</p>
                <p>Adjunto encontrará su schedule mensual personalizado. Por favor, revise cuidadosamente sus asignaciones y horarios.</p>
                <p>Si tiene alguna consulta, no dude en comunicarse con nuestro equipo de coordinación.</p>
                <br>
                <p>Atentamente,</p>
                <p><b>Departamento de Gestión de Horarios</b><br>Task Scheduler Company</p>
                """
                .formatted(person.getFullName());

        try {
            emailService.sendEmailWithAttachment(
                    person.getEmail(),
                    "Schedule Mensual Personalizado",
                    emailBody,
                    pdfBytes,
                    "schedule_" + person.getFullName().replaceAll("\\s+", "_") + ".pdf");
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Error enviando email a " + person.getFullName(), e);
        }
    }
}
