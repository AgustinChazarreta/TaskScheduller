package com.AgsCh.task_scheduler.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class ExternalJdbcConfig {

    @Bean(name = "externalDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.congregatio")
    public DataSource externalDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(@Qualifier("externalDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }
}