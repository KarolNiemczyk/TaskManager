package com.example.task.repository;

import com.example.task.model.TaskStatus;
import com.example.task.model.dto.TaskDto;
import com.example.task.repository.jdbc.TaskJdbcDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import(TaskJdbcDao.class)
class TaskJdbcDaoTest {

    @Autowired
    private TaskJdbcDao dao;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM tasks");
        jdbc.update("DELETE FROM categories");
    }

    // --- READ ---

    @Test
    void findAllAsDtos_ShouldMapRowsCorrectly() {
        jdbc.update("INSERT INTO tasks (title, status, created_at, updated_at) VALUES ('Task1', 'TODO', NOW(), NOW())");
        List<TaskDto> result = dao.findAllAsDtos();

        assertThat(result).hasSize(1);
        TaskDto dto = result.get(0);
        assertThat(dto.getTitle()).isEqualTo("Task1");
        assertThat(dto.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getCreatedAt()).isNotNull();
        assertThat(dto.getUpdatedAt()).isNotNull();
    }

    @Test
    void findAllAsDtos_WhenEmpty_ShouldReturnEmptyList() {
        assertThat(dao.findAllAsDtos()).isEmpty();
    }

    @Test
    void findById_ShouldReturnCorrectTask() {
        jdbc.update("INSERT INTO tasks (id, title, status, created_at, updated_at) VALUES (1, 'FindMe', 'TODO', NOW(), NOW())");
        TaskDto task = dao.findById(1L);

        assertThat(task).isNotNull();
        assertThat(task.getTitle()).isEqualTo("FindMe");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void findById_WhenNotExist_ShouldThrow() {
        assertThatThrownBy(() -> dao.findById(999L))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    // --- CREATE ---

    @Test
    void insert_ShouldPersistTaskAndGenerateId() {
        TaskDto dto = new TaskDto();
        dto.setTitle("New Task");
        dto.setDescription("Desc");
        dto.setStatus(TaskStatus.TODO);
        dto.setDueDate(LocalDate.of(2026, 1, 6));

        TaskDto saved = dao.insert(dto);

        assertThat(saved.getId()).isNotNull();
        List<TaskDto> all = dao.findAllAsDtos();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getTitle()).isEqualTo("New Task");
    }

    @Test
    void insert_WhenNullStatus_ShouldPersist() {
        TaskDto dto = new TaskDto();
        dto.setTitle("No Status Task");

        TaskDto saved = dao.insert(dto);
        assertThat(saved.getId()).isNotNull();
        TaskDto fetched = dao.findById(saved.getId());
        assertThat(fetched.getStatus()).isNull();
    }

    @Test
    void insert_WhenNullDueDate_ShouldPersist() {
        TaskDto dto = new TaskDto();
        dto.setTitle("No DueDate");
        dto.setStatus(TaskStatus.TODO);

        TaskDto saved = dao.insert(dto);
        TaskDto fetched = dao.findById(saved.getId());
        assertThat(fetched.getDueDate()).isNull();
    }

    // --- UPDATE ---

    @Test
    void update_ShouldModifyExistingTask() {
        jdbc.update("INSERT INTO tasks (id, title, status, created_at, updated_at) VALUES (1, 'Old Task', 'TODO', NOW(), NOW())");

        TaskDto dto = new TaskDto();
        dto.setId(1L);
        dto.setTitle("Updated Task");
        dto.setDescription("Updated Desc");
        dto.setStatus(TaskStatus.DONE);
        dto.setDueDate(LocalDate.of(2026,1,10));

        int rows = dao.update(dto);
        assertThat(rows).isEqualTo(1);

        TaskDto updated = dao.findById(1L);
        assertThat(updated.getTitle()).isEqualTo("Updated Task");
        assertThat(updated.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(updated.getDescription()).isEqualTo("Updated Desc");
        assertThat(updated.getDueDate()).isEqualTo(LocalDate.of(2026,1,10));
    }

    @Test
    void update_WhenNonExisting_ShouldReturnZero() {
        TaskDto dto = new TaskDto();
        dto.setId(999L);
        dto.setTitle("NonExisting");

        assertThat(dao.update(dto)).isZero();
    }

    @Test
    void update_WhenNullFields_ShouldHandleProperly() {
        jdbc.update("INSERT INTO tasks (id, title, status, created_at, updated_at) VALUES (1, 'Old Task', 'TODO', NOW(), NOW())");

        TaskDto dto = new TaskDto();
        dto.setId(1L);
        dto.setTitle(null);
        dto.setDescription(null);
        dto.setStatus(null);
        dto.setDueDate(null);

        int rows = dao.update(dto);
        assertThat(rows).isEqualTo(1);

        TaskDto updated = dao.findById(1L);
        assertThat(updated.getTitle()).isNull();
        assertThat(updated.getDescription()).isNull();
        assertThat(updated.getStatus()).isNull();
        assertThat(updated.getDueDate()).isNull();
    }

    // --- DELETE ---

    @Test
    void delete_ShouldRemoveTask() {
        jdbc.update("INSERT INTO tasks (id, title, status, created_at, updated_at) VALUES (1, 'ToDelete', 'TODO', NOW(), NOW())");
        int rows = dao.delete(1L);
        assertThat(rows).isEqualTo(1);
        assertThat(dao.findAllAsDtos()).isEmpty();
    }

    @Test
    void delete_WhenNonExisting_ShouldReturnZero() {
        assertThat(dao.delete(999L)).isZero();
    }

    // --- CATEGORY JOIN ---

    @Test
    void findAllAsDtos_ShouldMapCategoryProperly() {
        jdbc.update("INSERT INTO categories (id, name) VALUES (1, 'Cat1')");
        jdbc.update("INSERT INTO tasks (id, title, status, category_id, created_at, updated_at) VALUES (1, 'With Cat', 'TODO', 1, NOW(), NOW())");

        TaskDto task = dao.findAllAsDtos().get(0);
        assertThat(task.getCategoryName()).isEqualTo("Cat1");
    }

    // --- EDGE CASES ---

    @Test
    void findAllAsDtos_WhenCategoryMissing_ShouldReturnNullCategoryName() {
        jdbc.update("INSERT INTO tasks (id, title, status, created_at, updated_at) VALUES (1, 'No Cat', 'TODO', NOW(), NOW())");

        TaskDto task = dao.findAllAsDtos().get(0);
        assertThat(task.getCategoryName()).isNull();
    }

    @Test
    void insert_WithCategoryId_ShouldPersistCategoryId() {
        jdbc.update("INSERT INTO categories (id, name) VALUES (1, 'Cat1')");

        TaskDto dto = new TaskDto();
        dto.setTitle("Task with Category");
        dto.setCategoryId(1L);

        TaskDto saved = dao.insert(dto);
        TaskDto fetched = dao.findById(saved.getId());
        assertThat(fetched.getCategoryId()).isEqualTo(1L);
    }

}
