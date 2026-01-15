package com.AgsCh.task_scheduler;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.AgsCh.task_scheduler.dto.request.PersonRequestDTO;
import com.AgsCh.task_scheduler.dto.request.ScheduleRequestDTO;
import com.AgsCh.task_scheduler.dto.request.requestAdapter.PlanningPeriodRequestDTO;
import com.AgsCh.task_scheduler.dto.request.requestAdapter.ScheduleRequestDTOAdapter;
import com.AgsCh.task_scheduler.model.Category;
import com.AgsCh.task_scheduler.model.Task;
import com.AgsCh.task_scheduler.util.TaskAssembler;
import com.AgsCh.task_scheduler.util.TaskRefactor;
import com.AgsCh.task_scheduler.util.UserInputSimulator;
import com.AgsCh.task_scheduler.util.WordParser;

public class SchedulePipelineTest {

        public static void main(String[] args) {

                System.out.println("=== PIPELINE TEST START ===");

                // 1️⃣ WORD → tareas
                String path = "D://Agustín//Escritorio/Lorem.docx";

                Map<String, List<String>> parsed = WordParser.parseTasks(path);

                Map<String, List<DayOfWeek>> normalized = TaskRefactor.refactorDays(parsed);

                Map<String, Set<Category>> categories = UserInputSimulator.enrich(normalized);

                List<Task> tasks = TaskAssembler.buildTasks(normalized, categories);

                System.out.println("✔ tareas creadas: " + tasks.size());

                // 2️⃣ PERÍODO
                PlanningPeriodRequestDTO period = new PlanningPeriodRequestDTO();
                period.setStartDate(LocalDate.of(2024, 7, 1));
                period.setEndDate(LocalDate.of(2024, 7, 31));

                // 3️⃣ PERSONS (como si vinieran de Postman)
                PersonRequestDTO p1 = new PersonRequestDTO();
                p1.setName("Ana");
                p1.setCategory(Category.CATEGORY_1);

                PersonRequestDTO p2 = new PersonRequestDTO();
                p2.setName("Juan");
                p2.setCategory(Category.CATEGORY_2);

                List<PersonRequestDTO> persons = List.of(p1, p2);

                // 4️⃣ REQUEST FINAL (EL MISMO DE SIEMPRE)
                ScheduleRequestDTO request = ScheduleRequestDTOAdapter.adapt(period, persons, tasks);

                // 5️⃣ VALIDACIÓN VISUAL
                System.out.println("\n=== REQUEST FINAL ===");
                System.out.println("Periodo: " +
                                request.getPeriod().getStartDate() + " - " +
                                request.getPeriod().getEndDate());

                System.out.println("Persons: " + request.getPersons().size());
                System.out.println("Tasks: " + request.getTasks().size());

                request.getTasks().forEach(t -> {
                        System.out.println(" - " + t.getName()
                                        + " | days=" + t.getAssignedDays()
                                        + " | categories=" + t.getAllowedCategories());
                });

                System.out.println("=== PIPELINE TEST END ===");
        }
}
