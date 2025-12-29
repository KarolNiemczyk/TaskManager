package com.example.task.repository;

import com.example.task.model.entity.Task;
import com.example.task.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
    
    Page<Task> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Task> findByStatusAndCategoryId(TaskStatus status, Long categoryId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Task> findByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);

    // Filtr po dacie deadline
    Page<Task> findByDueDateBefore(LocalDate date, Pageable pageable);
    Page<Task> findByDueDateAfter(LocalDate date, Pageable pageable);

    @Query("""
    SELECT t FROM Task t
    WHERE (:status IS NULL OR t.status = :status)
      AND (:categoryId IS NULL OR t.category.id = :categoryId)
      AND (:dueDateBefore IS NULL OR t.dueDate < :dueDateBefore)
      AND (:dueDateAfter IS NULL OR t.dueDate > :dueDateAfter)
      AND (COALESCE(:title, '') = '' OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')))
    """)
    Page<Task> searchTasks(
            @Param("status") TaskStatus status,
            @Param("categoryId") Long categoryId,
            @Param("dueDateBefore") LocalDate dueDateBefore,
            @Param("dueDateAfter") LocalDate dueDateAfter,
            @Param("title") String title,
            Pageable pageable
    );

    Optional<Task> findByIdAndCategoryId(Long id, Long categoryId);
}