package com.AgsCh.task_scheduler.session;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import com.AgsCh.task_scheduler.model.House;

import org.springframework.context.annotation.ScopedProxyMode;

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AdminSession {

    private House house;
    private Long houseId;
    private String houseName;
    private boolean impersonating;

    public House getHouse() {
        return house;
    }

    public void setHouse(House house) {
        this.house = house;
    }

    public Long getHouseId() {
        return houseId;
    }

    public void setHouseId(Long houseId) {
        this.houseId = houseId;
    }

    public String getHouseName() {
        return houseName;
    }

    public void setHouseName(String houseName) {
        this.houseName = houseName;
    }

    public boolean isImpersonating() {
        return impersonating;
    }

    public void setImpersonating(boolean impersonating) {
        this.impersonating = impersonating;
    }

    public void clear() {
        house = null;
        houseId = null;
        houseName = null;
        impersonating = false;
    }
}