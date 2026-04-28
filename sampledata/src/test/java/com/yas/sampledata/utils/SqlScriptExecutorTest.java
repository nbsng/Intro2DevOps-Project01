package com.yas.sampledata.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class SqlScriptExecutorTest {

    @Test
    void executeScriptsForSchema_runsSqlResources() {
        DataSource dataSource = createDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        SqlScriptExecutor executor = new SqlScriptExecutor();

        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS PUBLIC");

        executor.executeScriptsForSchema(dataSource, "PUBLIC", "classpath*:db/test/*.sql");

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES "
                + "WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = 'SAMPLE_TABLE'",
            Integer.class
        );
        assertThat(count).isEqualTo(1);
        Integer rows = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM SAMPLE_TABLE", Integer.class);
        assertThat(rows).isEqualTo(1);
    }

    @Test
    void executeScriptsForSchema_withMissingResources_doesNotThrow() {
        DataSource dataSource = createDataSource();
        SqlScriptExecutor executor = new SqlScriptExecutor();

        assertDoesNotThrow(() -> executor.executeScriptsForSchema(
            dataSource,
            "public",
            "classpath*:db/missing/*.sql"
        ));
    }

    private DataSource createDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}
