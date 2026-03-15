package com.AgsCh.task_scheduler.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.AgsCh.task_scheduler.dto.ScheduleMapper;
import com.AgsCh.task_scheduler.dto.request.ScheduleRequestDTO;
import com.AgsCh.task_scheduler.dto.response.FunctionAssignmentResponseDTO;
import com.AgsCh.task_scheduler.dto.response.ScheduleResponseDTO;
import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.Function;
import com.AgsCh.task_scheduler.model.FunctionAssignment;
import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.model.ScheduleRun;
import com.AgsCh.task_scheduler.model.User;
import com.AgsCh.task_scheduler.repository.FunctionRepository;
import com.AgsCh.task_scheduler.repository.PersonRepository;
import com.AgsCh.task_scheduler.service.admin.AdminScheduleService;
import com.AgsCh.task_scheduler.service.admin.AdminService;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.properties.UnitValue;
import java.util.Locale;
import java.util.Map;

import com.itextpdf.layout.element.Table;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

        private final AdminScheduleService scheduleService;
        private final FunctionRepository functionRepository;
        private final PersonRepository personRepository;
        private final AdminService adminService;

        public ScheduleController(
                        AdminScheduleService scheduleService,
                        FunctionRepository functionRepository,
                        PersonRepository personRepository,
                        AdminService adminService) {

                this.scheduleService = scheduleService;
                this.functionRepository = functionRepository;
                this.personRepository = personRepository;
                this.adminService = adminService;
        }

        /*
         * =========================
         * RESOLVER SCHEDULE
         * =========================
         */
        @PostMapping("/solve")
        @PreAuthorize("hasRole('ADMIN')")
        public ScheduleResponseDTO solve(
                        @Valid @RequestBody ScheduleRequestDTO request,
                        Authentication authentication) {

                User user = getAuthenticatedUser(authentication);

                Schedule schedule = ScheduleMapper.toModel(
                                request,
                                functionRepository,
                                personRepository);

                Schedule solvedSchedule = scheduleService.solve(schedule, user.getHouse());

                return ScheduleMapper.toResponse(solvedSchedule);
        }

        /*
         * =========================
         * VER SCHEDULE ACTUAL
         * =========================
         */
        @GetMapping("/current")
        @PreAuthorize("hasAnyRole('ADMIN','USER')")
        public ScheduleResponseDTO current(@AuthenticationPrincipal User user) {

                validateUserAndHouse(user);

                ScheduleRun activeRun = scheduleService.getActiveRunByHouse(user.getHouse().getId());

                if (activeRun == null) {
                        throw new BusinessException("No hay schedule activo para esta House");
                }

                return ScheduleMapper.toResponse(activeRun);
        }

        /*
         * =========================
         * CREAR NUEVA VERSIÓN (drag & drop)
         * =========================
         */
        @PostMapping("/create-new-run")
        @PreAuthorize("hasRole('ADMIN')")
        public ScheduleResponseDTO createNewRun(
                        @Valid @RequestBody List<FunctionAssignmentResponseDTO> dtos,
                        Authentication authentication) {

                User user = getAuthenticatedUser(authentication);

                List<Function> functions = functionRepository.findAll();
                List<Person> persons = personRepository.findAll();

                Schedule schedule = ScheduleMapper.toModelFromAssignments(dtos, functions, persons);

                ScheduleRun newRun = scheduleService.createNewRun(schedule, user.getHouse());

                return ScheduleMapper.toResponse(newRun);
        }

        /*
         * =========================
         * MÉTODOS PRIVADOS AUXILIARES
         * =========================
         */

        private User getAuthenticatedUser(Authentication authentication) {

                if (authentication == null) {
                        throw new BusinessException("Usuario no autenticado");
                }

                String username = authentication.getName();
                User user = adminService.findByUsername(username);

                if (user == null) {
                        throw new BusinessException("Usuario autenticado no encontrado");
                }

                validateUserAndHouse(user);

                return user;
        }

        private void validateUserAndHouse(User user) {

                if (user == null) {
                        throw new BusinessException("Usuario no autenticado");
                }

                if (user.getHouse() == null) {
                        throw new BusinessException("El usuario no tiene House asignada");
                }
        }

        @PostMapping("/export-pdf")
        @PreAuthorize("hasAnyRole('ADMIN','USER')")
        public ResponseEntity<byte[]> exportPdf(
                        @RequestBody List<FunctionAssignmentResponseDTO> assignments) {

                try {

                        if (assignments == null || assignments.isEmpty()) {
                                return ResponseEntity.badRequest().build();
                        }

                        assignments.sort(Comparator.comparing(FunctionAssignmentResponseDTO::getDate));

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        PdfWriter writer = new PdfWriter(baos);
                        PdfDocument pdf = new PdfDocument(writer);

                        Document document = new Document(pdf);
                        document.setMargins(50, 50, 45, 50); // márgenes equilibrados

                        PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
                        PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

                        // =============================
                        // LOGO INSTITUCIONAL GRANDE
                        // =============================
                        try {
                                InputStream logoStream = getClass()
                                                .getClassLoader()
                                                .getResourceAsStream("static/logo.png");

                                if (logoStream != null) {
                                        byte[] logoBytes = logoStream.readAllBytes();
                                        ImageData imageData = ImageDataFactory.create(logoBytes);
                                        Image logo = new Image(imageData);

                                        Rectangle pageSize = pdf.getDefaultPageSize();
                                        float maxLogoHeight = pageSize.getHeight() * 0.15f; // 25% del alto de la página

                                        logo.scaleToFit(pageSize.getWidth() * 0.5f, maxLogoHeight);

                                        logo.setHorizontalAlignment(HorizontalAlignment.CENTER);
                                        logo.setMarginBottom(10);

                                        document.add(logo);
                                }

                        } catch (Exception e) {
                                System.out.println("Logo no encontrado, continúo sin logo.");
                        }

                        // =============================
                        // TÍTULO PRINCIPAL
                        // =============================
                        document.add(new Paragraph("SCHEDULE MENSUAL")
                                        .setFont(titleFont)
                                        .setFontSize(18)
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setMarginBottom(5));

                        // Línea divisoria formal
                        document.add(new Paragraph(" ")
                                        .setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(1))
                                        .setMarginBottom(15));

                        // =============================
                        // RANGO DE FECHAS
                        // =============================
                        LocalDate startDate = assignments.get(0).getDate();
                        LocalDate endDate = assignments.get(assignments.size() - 1).getDate();

                        DateTimeFormatter shortFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                        document.add(new Paragraph(
                                        "Período: " + startDate.format(shortFormatter)
                                                        + " al " + endDate.format(shortFormatter))
                                        .setFont(normalFont)
                                        .setFontSize(11)
                                        .setTextAlignment(TextAlignment.RIGHT)
                                        .setMarginBottom(20));

                        // =============================
                        // NUMERACIÓN DE PÁGINA
                        // =============================
                        pdf.addEventHandler(PdfDocumentEvent.END_PAGE, event -> {

                                PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
                                PdfDocument pdfDoc = docEvent.getDocument();
                                PdfPage page = docEvent.getPage();

                                int pageNumber = pdfDoc.getPageNumber(page);
                                int totalPages = pdfDoc.getNumberOfPages();

                                Rectangle pageSize = page.getPageSize();
                                PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamAfter(), page.getResources(),
                                                pdfDoc);
                                Canvas canvas = new Canvas(pdfCanvas, pageSize);

                                canvas.showTextAligned(
                                                new Paragraph("Página " + pageNumber + " de " + totalPages)
                                                                .setFont(normalFont)
                                                                .setFontSize(9),
                                                pageSize.getWidth() - 50,
                                                20,
                                                TextAlignment.RIGHT);

                                canvas.close();
                        });

                        // =============================
                        // FORMATEADORES
                        // =============================
                        DateTimeFormatter longFormatter = DateTimeFormatter.ofPattern(
                                        "EEEE d 'de' MMMM 'de' yyyy",
                                        new Locale("es", "ES"));

                        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy",
                                        new Locale("es", "ES"));

                        YearMonth currentMonth = null;
                        Table table = null;

                        for (FunctionAssignmentResponseDTO a : assignments) {

                                YearMonth assignmentMonth = YearMonth.from(a.getDate());

                                if (!assignmentMonth.equals(currentMonth)) {

                                        if (table != null) {
                                                document.add(table);
                                                document.add(new AreaBreak());
                                        }

                                        currentMonth = assignmentMonth;

                                        String monthTitle = assignmentMonth.format(monthFormatter).toUpperCase();

                                        document.add(new Paragraph(monthTitle)
                                                        .setFont(titleFont)
                                                        .setFontSize(14)
                                                        .setMarginBottom(10));

                                        float[] columnWidths = { 4f, 3f, 3f };
                                        table = new Table(columnWidths);
                                        table.setWidth(UnitValue.createPercentValue(100));

                                        table.addHeaderCell(new Cell()
                                                        .add(new Paragraph("Fecha").setFont(titleFont))
                                                        .setBackgroundColor(new DeviceGray(0.92f)));

                                        table.addHeaderCell(new Cell()
                                                        .add(new Paragraph("Persona").setFont(titleFont))
                                                        .setBackgroundColor(new DeviceGray(0.92f)));

                                        table.addHeaderCell(new Cell()
                                                        .add(new Paragraph("Función").setFont(titleFont))
                                                        .setBackgroundColor(new DeviceGray(0.92f)));
                                }

                                String formattedDate = a.getDate().format(longFormatter);
                                formattedDate = formattedDate.substring(0, 1).toUpperCase() +
                                                formattedDate.substring(1);

                                String person = a.getPersonName()
                                                + (a.getPersonNickname() != null && !a.getPersonNickname().isEmpty()
                                                                ? " (" + a.getPersonNickname() + ")"
                                                                : "");

                                table.addCell(new Cell()
                                                .add(new Paragraph(formattedDate).setFont(normalFont))
                                                .setPadding(6));

                                table.addCell(new Cell()
                                                .add(new Paragraph(person).setFont(normalFont))
                                                .setPadding(6));

                                table.addCell(new Cell()
                                                .add(new Paragraph(a.getFunctionName()).setFont(normalFont))
                                                .setPadding(6));
                        }

                        if (table != null) {
                                document.add(table);
                        }

                        document.close();

                        return ResponseEntity.ok()
                                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=schedule.pdf")
                                        .contentType(MediaType.APPLICATION_PDF)
                                        .body(baos.toByteArray());

                } catch (Exception e) {
                        e.printStackTrace();
                        return ResponseEntity.status(500).build();
                }
        }

        @PostMapping("/send-pdfs")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Map<String, Object>> sendAllPdfs() { // 🔥 ya no recibe houseId

                List<Person> persons = personRepository.findAll();

                if (persons.isEmpty()) {
                        return ResponseEntity.badRequest()
                                        .body(Map.of("message", "No hay personas registradas"));
                }

                // 🔥 Tomamos la houseId de la primera persona
                Long houseId = persons.get(0).getHouse().getId();

                ScheduleRun activeRun = scheduleService.getActiveRunByHouse(houseId);

                if (activeRun == null) {
                        return ResponseEntity.badRequest()
                                        .body(Map.of("message", "No hay schedule activo para esta House"));
                }

                List<FunctionAssignment> allAssignments = activeRun.getAssignments();

                List<String> failDetails = new ArrayList<>();
                int successCount = 0;

                for (Person person : persons) {
                        try {
                                scheduleService.sendPdfToPerson(person, allAssignments);
                                successCount++;
                        } catch (Exception e) {
                                failDetails.add(String.format("Error enviando PDF a %s: %s",
                                                person.getFullName(), e.getMessage()));
                                e.printStackTrace();
                        }
                }

                Map<String, Object> response = Map.of(
                                "successCount", successCount,
                                "failCount", failDetails.size(),
                                "failDetails", failDetails);

                return ResponseEntity.ok(response);
        }
}