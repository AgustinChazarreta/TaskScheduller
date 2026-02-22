package com.AgsCh.task_scheduler.service.admin;

import java.util.List;

import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.model.House;
import com.AgsCh.task_scheduler.repository.HouseRepository;

@Service
public class HouseService {

    private final HouseRepository houseRepository;

    public HouseService(HouseRepository houseRepository) {
        this.houseRepository = houseRepository;
    }

    public House createHouse(String name, boolean active) {
        House house = new House();
        house.setName(name);
        house.setActive(active);
        return houseRepository.save(house);
    }

    public House updateHouse(Long id, String name, boolean active) {
        House house = getHouseById(id);
        house.setName(name);
        house.setActive(active);
        return houseRepository.save(house);
    }

    public void deleteHouse(Long id) {
        houseRepository.deleteById(id);
    }

    public House getHouseById(Long id) {
        return houseRepository.findById(id).orElseThrow(() -> new RuntimeException("House not found"));
    }

    public List<House> getAllHouses() {
        return houseRepository.findAll();
    }

}
