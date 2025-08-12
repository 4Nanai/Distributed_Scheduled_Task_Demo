package insight.shinanai.distributed_scheduled_task_demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class DatabaseConnectionTest {
    @Autowired
    private DataSource dataSource;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Test database connection")
    void testDatabaseConnection() {
        assertNotNull(dataSource, "DataSource should not be null");
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "Connection should not be null");
            assertFalse(connection.isClosed(), "Connection should be open");

            // Print database metadata
            DatabaseMetaData metaData = connection.getMetaData();
            System.out.println("Database Product Name: " + metaData.getDatabaseProductName());
            System.out.println("Database version: " + metaData.getDatabaseProductVersion());
            System.out.println("Driver Name: " + metaData.getDriverName());
            System.out.println("Driver Version: " + metaData.getDriverVersion());
        } catch (Exception e) {
            fail("Failed to connect to the database: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test simple SQL queries")
    void testSimpleQuery() {
        // 测试1：简单字符串查询
        String result = jdbcTemplate.queryForObject("SELECT 'Hello Database!'", String.class);
        assertEquals("Hello Database!", result);

        // 测试2：查询当前时间（不使用别名）
        String currentTime = jdbcTemplate.queryForObject("SELECT NOW()", String.class);
        assertNotNull(currentTime);
        System.out.println("数据库当前时间: " + currentTime);

        // 测试3：查询数据库版本
        String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
        assertNotNull(version);
        System.out.println("MySQL版本: " + version);

        // 测试4：简单数学运算
        Integer result2 = jdbcTemplate.queryForObject("SELECT 1 + 1", Integer.class);
        assertEquals(2, result2);
    }
}
