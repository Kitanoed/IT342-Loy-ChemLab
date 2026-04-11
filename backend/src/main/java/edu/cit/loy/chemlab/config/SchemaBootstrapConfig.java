package edu.cit.loy.chemlab.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataAccessException;

@Configuration
public class SchemaBootstrapConfig {

    @Bean
    public ApplicationRunner schemaBootstrapRunner(JdbcTemplate jdbcTemplate) {
        return args -> {
            executeSafe(jdbcTemplate, "ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS lab_id BIGINT");

            executeSafe(jdbcTemplate, "ALTER TABLE IF EXISTS inventory_items ADD COLUMN IF NOT EXISTS description TEXT");
            executeSafe(jdbcTemplate, "ALTER TABLE IF EXISTS inventory_items ADD COLUMN IF NOT EXISTS safety_notes TEXT");
            executeSafe(jdbcTemplate, "ALTER TABLE IF EXISTS inventory_items ADD COLUMN IF NOT EXISTS pubchem_cid INTEGER");
            executeSafe(jdbcTemplate, "ALTER TABLE IF EXISTS inventory_items ADD COLUMN IF NOT EXISTS molecular_formula TEXT");
            executeSafe(jdbcTemplate, "ALTER TABLE IF EXISTS inventory_items ADD COLUMN IF NOT EXISTS molecular_weight TEXT");
            executeSafe(jdbcTemplate, "ALTER TABLE IF EXISTS inventory_items ADD COLUMN IF NOT EXISTS iupac_name TEXT");

            executeSafe(jdbcTemplate, "CREATE INDEX IF NOT EXISTS idx_inventory_items_pubchem_cid ON inventory_items(pubchem_cid)");

            normalizeTextColumnIfBytea(jdbcTemplate, "inventory_items", "item_name");
            normalizeTextColumnIfBytea(jdbcTemplate, "inventory_items", "item_code");
        };
    }

    private void normalizeTextColumnIfBytea(JdbcTemplate jdbcTemplate, String tableName, String columnName) {
        try {
            String dataType = jdbcTemplate.queryForObject(
                    "SELECT data_type FROM information_schema.columns WHERE table_name = ? AND column_name = ?",
                    String.class,
                    tableName,
                    columnName
            );

            if ("bytea".equalsIgnoreCase(dataType)) {
                String alter = "ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " TYPE TEXT USING convert_from(" + columnName + ", 'UTF8')";
                executeSafe(jdbcTemplate, alter);
            }
        } catch (DataAccessException ignored) {
        }
    }

    private void executeSafe(JdbcTemplate jdbcTemplate, String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (DataAccessException ignored) {
        }
    }
}
