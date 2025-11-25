// src/main/java/com/example/task/dao/TaskStatsDao.java
package com.example.task.repository.jdbc;

import com.example.task.model.entity.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskJdbcDao {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Long> COUNT_MAPPER = (rs, rowNum) -> rs.getLong(1);

    public long countAll() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tasks", COUNT_MAPPER);
    }

    public long countByStatus(TaskStatus status) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tasks WHERE status = ?",
                COUNT_MAPPER, status.name());
    }

    public List<TaskSummary> getTaskSummary() {
        return jdbcTemplate.query("""
                SELECT c.name AS categoryName, t.status, COUNT(*) AS count
                FROM tasks t
                LEFT JOIN categories c ON t.category_id = c.id
                GROUP BY c.name, t.status
                """, new TaskSummaryRowMapper());
    }

    public static class TaskSummary {
        public final String categoryName;
        public final TaskStatus status;
        public final long count;

        public TaskSummary(String categoryName, TaskStatus status, long count) {
            this.categoryName = categoryName;
            this.status = status;
            this.count = count;
        }
    }

    private static class TaskSummaryRowMapper implements RowMapper<TaskSummary> {
        @Override
        public TaskSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
            String categoryName = rs.getString("categoryName");
            TaskStatus status = TaskStatus.valueOf(rs.getString("status"));
            long count = rs.getLong("count");
            return new TaskSummary(categoryName != null ? categoryName : "Bez kategorii", status, count);
        }
    }
}