package com.example.task.repository.jdbc;

import com.example.task.model.dto.TaskDto;
import com.example.task.model.TaskStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
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

    // --- READ ---
    public List<TaskDto> findAllAsDtos() {
        String sql = "SELECT t.id, t.title, t.description, t.status, t.due_date, t.category_id, " +
                "c.name as category_name, t.created_at, t.updated_at " +
                "FROM tasks t LEFT JOIN categories c ON t.category_id = c.id ORDER BY t.created_at DESC";
        return jdbc.query(sql, rowMapper);
    }

    public TaskDto findById(Long id) {
        String sql = "SELECT t.id, t.title, t.description, t.status, t.due_date, t.category_id, " +
                "c.name as category_name, t.created_at, t.updated_at " +
                "FROM tasks t LEFT JOIN categories c ON t.category_id = c.id WHERE t.id = ?";
        return jdbc.queryForObject(sql, rowMapper, id);
    }

    // --- CREATE ---
    public TaskDto insert(TaskDto dto) {
        String sql = "INSERT INTO tasks (title, description, status, due_date, category_id, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, NOW(), NOW())";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, dto.getTitle());
            ps.setString(2, dto.getDescription());
            ps.setString(3, dto.getStatus() != null ? dto.getStatus().name() : null);
            ps.setDate(4, dto.getDueDate() != null ? Date.valueOf(dto.getDueDate()) : null);
            if (dto.getCategoryId() != null) {
                ps.setLong(5, dto.getCategoryId());
            } else {
                ps.setNull(5, java.sql.Types.BIGINT);
            }
            return ps;
        }, keyHolder);

        dto.setId(keyHolder.getKey().longValue());
        return dto;
    }

    // --- UPDATE ---
    public int update(TaskDto dto) {
        String sql = "UPDATE tasks SET title = ?, description = ?, status = ?, due_date = ?, category_id = ?, updated_at = NOW() " +
                "WHERE id = ?";
        return jdbc.update(sql,
                dto.getTitle(),
                dto.getDescription(),
                dto.getStatus() != null ? dto.getStatus().name() : null,
                dto.getDueDate() != null ? Date.valueOf(dto.getDueDate()) : null,
                dto.getCategoryId(),
                dto.getId());
    }

    // --- DELETE ---
    public int delete(Long id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        return jdbc.update(sql, id);
    }
}
