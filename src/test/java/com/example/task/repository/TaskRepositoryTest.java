package com.example.task.repository;

import com.example.task.model.TaskStatus;
import com.example.task.model.entity.Category;
import com.example.task.model.entity.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Task createTask(String title, TaskStatus status, Category category) {
        Task task = new Task();
        task.setTitle(title);
        task.setStatus(status);
        task.setCategory(category);
        task.setDueDate(LocalDate.now().plusDays(1));
        return task;
    }

    @Test
    void save_ShouldPersistTask() {
        Task task = createTask("Task 1", TaskStatus.TODO, null);

        Task saved = taskRepository.save(task);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Task 1");
    }

    @Test
    void findByStatus_ShouldReturnTasks() {
        taskRepository.save(createTask("T1", TaskStatus.TODO, null));
        taskRepository.save(createTask("T2", TaskStatus.DONE, null));

        Page<Task> page = taskRepository.findByStatus(
                TaskStatus.TODO,
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void findByCategoryId_ShouldReturnTasks() {
        Category category = new Category();
        category.setName("Work");
        Category savedCategory = categoryRepository.save(category);

        taskRepository.save(createTask("Task A", TaskStatus.TODO, savedCategory));

        Page<Task> page = taskRepository.findByCategoryId(
                savedCategory.getId(),
                PageRequest.of(0, 10)
        );

        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findByStatusAndCategoryId_ShouldWork() {
        Category cat = new Category();
        cat.setName("JoinCat");
        cat = categoryRepository.save(cat);

        taskRepository.save(createTask("Join Task", TaskStatus.TODO, cat));

        Page<Task> result = taskRepository.findByStatusAndCategoryId(
                TaskStatus.TODO,
                cat.getId(),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void searchTasks_WithJoinAndFilters_ShouldReturnResult() {
        Category cat = new Category();
        cat.setName("SearchCat");
        cat = categoryRepository.save(cat);

        Task task = createTask("Important Task", TaskStatus.TODO, cat);
        taskRepository.save(task);

        Page<Task> page = taskRepository.searchTasks(
                TaskStatus.TODO,
                cat.getId(),
                null,
                null,
                "important",
                PageRequest.of(0, 10)
        );

        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findByDueDateBefore_ShouldReturnTask() {
        Task task = createTask("Deadline", TaskStatus.TODO, null);
        task.setDueDate(LocalDate.now().minusDays(1));
        taskRepository.save(task);

        Page<Task> page = taskRepository.findByDueDateBefore(
                LocalDate.now(),
                PageRequest.of(0, 10)
        );

        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void delete_ShouldRemoveTask() {
        Task task = taskRepository.save(createTask("DeleteMe", TaskStatus.TODO, null));

        taskRepository.deleteById(task.getId());

        assertThat(taskRepository.findById(task.getId())).isEmpty();
    }
}
