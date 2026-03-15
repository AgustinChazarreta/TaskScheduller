package com.AgsCh.task_scheduler.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.AgsCh.task_scheduler.controller.auth.HouseRequest;
import com.AgsCh.task_scheduler.dto.response.FunctionResponseDTO;
import com.AgsCh.task_scheduler.dto.response.GroupResponseDTO;
import com.AgsCh.task_scheduler.dto.response.HouseResponseDTO;
import com.AgsCh.task_scheduler.dto.response.ScheduleRunDTO;
import com.AgsCh.task_scheduler.model.House;
import com.AgsCh.task_scheduler.model.ScheduleRun;
import com.AgsCh.task_scheduler.service.admin.GroupService;
import com.AgsCh.task_scheduler.service.admin.HouseService;
import com.AgsCh.task_scheduler.service.admin.WebmasterScheduleService;
import com.AgsCh.task_scheduler.service.domain.FunctionService;

@RestController
@RequestMapping("/api/webmaster/houses")
@PreAuthorize("hasRole('WEBMASTER')")
public class WebmasterHouseApiController {

    private final HouseService houseService;
    private final GroupService groupService;
    private final FunctionService functionService;
    private final WebmasterScheduleService scheduleService;

    public WebmasterHouseApiController(HouseService houseService, GroupService groupService,
            FunctionService functionService, WebmasterScheduleService scheduleService) {
        this.houseService = houseService;
        this.groupService = groupService;
        this.functionService = functionService;
        this.scheduleService = scheduleService;

    }

    @GetMapping
    public List<HouseResponseDTO> list() {
        return houseService.getAllHouses()
                .stream()
                .map(HouseResponseDTO::new)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<HouseResponseDTO> getOne(@PathVariable Long id) {
        House house = houseService.getHouseById(id);

        if (house == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new HouseResponseDTO(house));
    }

    @PostMapping
    public HouseResponseDTO create(@RequestBody HouseRequest request) {

        House house = houseService.createHouse(
                request.getName(),
                request.isActive());

        return new HouseResponseDTO(house);
    }

    @PutMapping("/{id}")
    public void update(
            @PathVariable Long id,
            @RequestBody HouseRequest request) {

        houseService.updateHouse(
                id,
                request.getName(),
                request.isActive());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        houseService.deleteHouse(id);
    }

    @GetMapping("/{houseId}/groups")
    public ResponseEntity<List<GroupResponseDTO>> getGroupsByHouse(@PathVariable Long houseId) {
        List<GroupResponseDTO> groups = groupService.findByHouseId(houseId);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{houseId}/functions")
    public ResponseEntity<List<FunctionResponseDTO>> getFunctionsByHouse(@PathVariable Long houseId) {
        List<FunctionResponseDTO> functions = functionService.findDTOByHouseId(houseId);
        return ResponseEntity.ok(functions);
    }

    @GetMapping("/schedule-runs")
    public List<ScheduleRunDTO> getAllScheduleRuns() {
        List<ScheduleRun> runs = scheduleService.getAllRuns();
        return runs.stream()
                .map(ScheduleRunDTO::new)
                .toList();
    }

}