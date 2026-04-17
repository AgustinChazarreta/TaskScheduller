package com.AgsCh.task_scheduler.adapter.external;

import com.AgsCh.task_scheduler.dto.external.ExternalPersonDTO;
import com.AgsCh.task_scheduler.port.external.ExternalPersonSearchPort;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExternalPersonSearchAdapter implements ExternalPersonSearchPort {

    private final JdbcTemplate jdbcTemplate;

    public ExternalPersonSearchAdapter(
            @Qualifier("congregatioJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ExternalPersonDTO> searchByName(String name) {

        String sql = """
                    SELECT
                        `Nome Completo` AS fullName,
                        `Email` AS email,
                        `Ordem` AS orden,
                        `Data Nacimento` AS birthDate,
                        `Foto` AS photo
                    FROM Congregatio
                    WHERE `Nome Completo` LIKE ?
                    LIMIT 10
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new ExternalPersonDTO(
                        rs.getString("fullName"),
                        rs.getString("email"),
                        rs.getString("orden"),
                        rs.getString("birthDate"),
                        rs.getBytes("photo")),
                "%" + name + "%");
    }
}