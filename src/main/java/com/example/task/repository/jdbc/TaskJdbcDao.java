package com.example.task.repository.jdbc;

import com.example.task.model.dto.TaskDto;
import com.example.task.model.entity.TaskStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class TaskJdbcDao {

    private final JdbcTemplate jdbc;

    public TaskJdbcDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<TaskDto> rowMapper = new RowMapper<>() {
        @Override
        public TaskDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            TaskDto dto = new TaskDto();
            dto.setId(rs.getLong("id"));
            dto.setTitle(rs.getString("title"));
            dto.setDescription(rs.getString("description"));
            String status = rs.getString("status");
            if (status != null && !status.isBlank()) dto.setStatus(TaskStatus.valueOf(status));
            java.sql.Date due = rs.getDate("due_date");
            if (due != null) dto.setDueDate(due.toLocalDate());
            long catId = rs.getLong("category_id");
            if (!rs.wasNull()) dto.setCategoryId(catId);
            dto.setCategoryName(rs.getString("category_name"));

            java.sql.Timestamp created = rs.getTimestamp("created_at");
            if (created != null) dto.setCreatedAt(created.toLocalDateTime());
            java.sql.Timestamp updated = rs.getTimestamp("updated_at");
            if (updated != null) dto.setUpdatedAt(updated.toLocalDateTime());

            return dto;
        }
    };

    public List<TaskDto> findAllAsDtos() {
        String sql = "SELECT t.id, t.title, t.description, t.status, t.due_date, t.category_id, c.name as category_name, t.created_at, t.updated_at " +
                "FROM tasks t LEFT JOIN categories c ON t.category_id = c.id ORDER BY t.created_at DESC";
        return jdbc.query(sql, rowMapper);
    }

}
