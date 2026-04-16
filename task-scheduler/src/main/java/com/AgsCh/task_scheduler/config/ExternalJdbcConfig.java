package com.AgsCh.task_scheduler.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class ExternalJdbcConfig {

    @Bean(name = "congregatioDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.congregatio")
    public DataSource congregatioDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "congregatioJdbcTemplate")
    public JdbcTemplate congregatioJdbcTemplate(
            @Qualifier("congregatioDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}