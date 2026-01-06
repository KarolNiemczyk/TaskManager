package com.example.task.service;

import com.example.task.exception.ResourceNotFoundException;
import com.example.task.mapper.TaskMapper;
import com.example.task.model.dto.TaskCreateDto;
import com.example.task.model.dto.TaskDto;
import com.example.task.model.dto.StatisticsDto;
import com.example.task.model.entity.Category;
import com.example.task.model.entity.Task;
import com.example.task.model.TaskStatus;
import com.example.task.repository.CategoryRepository;
import com.example.task.repository.TaskRepository;
import com.example.task.repository.jdbc.TaskJdbcDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private TaskMapper taskMapper;
    @Mock private TaskJdbcDao taskJdbcDao;

    @InjectMocks private TaskService taskService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }
    // ---------------- getTasksWithFilters ----------------
    @Test
    void getTasksWithFilters_ShouldReturnPage() {
        Task task = new Task();
        task.setId(1L);
        Page<Task> page = new PageImpl<>(List.of(task));
        when(taskRepository.searchTasks(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);
        when(taskMapper.toDto(task)).thenReturn(new TaskDto(){{
            setId(1L);
        }});

        Page<TaskDto> result = taskService.getTasksWithFilters(null, null, null, null, "", 0, 10, "createdAt,desc");

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(taskRepository, times(1))
                .searchTasks(any(), any(), any(), any(), any(), any(Pageable.class));
        verify(taskMapper, times(1)).toDto(task);
    }

    @Test
    void getTasksWithFilters_InvalidPageSize_ShouldUseDefault() {
        Task task = new Task();
        Page<Task> page = new PageImpl<>(List.of(task));
        when(taskRepository.searchTasks(any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);
        when(taskMapper.toDto(task)).thenReturn(new TaskDto(){{
            setId(1L);
        }});

        Page<TaskDto> result = taskService.getTasksWithFilters(null, null, null, null, "", -1, 200, "createdAt");

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(taskRepository, times(1)).searchTasks(any(), any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void getTasksWithFilters_SortingEdgeCase_ShouldDefaultToCreatedAt() {
        Task task = new Task();
        Page<Task> page = new PageImpl<>(List.of(task));
        when(taskRepository.searchTasks(any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);
        when(taskMapper.toDto(task)).thenReturn(new TaskDto(){{
            setId(1L);
        }});

        Page<TaskDto> result = taskService.getTasksWithFilters(null, null, null, null, "", 0, 10, "invalidField,asc");

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(taskRepository, times(1)).searchTasks(any(), any(), any(), any(), any(), any(Pageable.class));
    }

    // ---------------- getTaskById ----------------
    @Test
    void getTaskById_Found_ShouldReturnDto() {
        Task task = new Task(); task.setId(1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(new TaskDto(){{
            setId(1L);
        }});

        TaskDto dto = taskService.getTaskById(1L);

        assertThat(dto.getId()).isEqualTo(1L);
        verify(taskRepository, times(1)).findById(1L);
        verify(taskMapper, times(1)).toDto(task);
    }

    @Test
    void getTaskById_NotFound_ShouldThrow() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(1L));
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void getTaskById_MultipleCalls_ShouldVerify() {
        Task task = new Task(); task.setId(2L);
        when(taskRepository.findById(2L)).thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(new TaskDto(){{
            setId(2L);
        }});

        taskService.getTaskById(2L);
        taskService.getTaskById(2L);

        verify(taskRepository, times(2)).findById(2L);
        verify(taskMapper, times(2)).toDto(task);
    }

    // ---------------- createTask ----------------
    @Test
    void createTask_ShouldSaveAndReturnDto() {
        TaskCreateDto createDto = new TaskCreateDto();
        createDto.setTitle("New");
        createDto.setStatus(TaskStatus.TODO);

        Category cat = new Category(); cat.setId(1L);
        Task task = new Task(); task.setId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        when(taskMapper.toEntity(createDto, cat)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(new TaskDto(){{
            setId(1L);
        }});

        createDto.setCategoryId(1L);
        TaskDto dto = taskService.createTask(createDto);

        assertThat(dto.getId()).isEqualTo(1L);
        verify(categoryRepository).findById(1L);
        verify(taskMapper).toEntity(createDto, cat);
        verify(taskRepository).save(task);
        verify(taskMapper).toDto(task);
    }

    @Test
    void createTask_CategoryNotFound_ShouldThrow() {
        TaskCreateDto createDto = new TaskCreateDto();
        createDto.setCategoryId(999L);

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.createTask(createDto));
        verify(categoryRepository).findById(999L);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_NoCategory_ShouldSave() {
        TaskCreateDto createDto = new TaskCreateDto();
        createDto.setTitle("NoCat"); createDto.setStatus(TaskStatus.TODO);

        Task task = new Task(); task.setId(1L);

        when(taskMapper.toEntity(createDto, null)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(new TaskDto(){{
            setId(1L);
        }});

        TaskDto dto = taskService.createTask(createDto);

        assertThat(dto.getId()).isEqualTo(1L);
        verify(taskRepository).save(task);
        verify(taskMapper).toEntity(createDto, null);
    }

    // ---------------- updateTask ----------------
    @Test
    void updateTask_Found_ShouldUpdate() {
        TaskCreateDto dto = new TaskCreateDto();
        dto.setTitle("Updated"); dto.setCategoryId(1L); dto.setStatus(TaskStatus.IN_PROGRESS);

        Task task = new Task(); task.setId(1L);
        Category cat = new Category(); cat.setId(1L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(new TaskDto(){{
            setId(1L);
        }});

        TaskDto result = taskService.updateTask(1L, dto);

        assertThat(result.getId()).isEqualTo(1L);
        verify(taskRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(taskRepository).save(task);
        verify(taskMapper).updateEntityFromDto(dto, task, cat);
    }

    @Test
    void updateTask_TaskNotFound_ShouldThrow() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        TaskCreateDto dto = new TaskCreateDto();

        assertThrows(ResourceNotFoundException.class, () -> taskService.updateTask(1L, dto));
        verify(taskRepository).findById(1L);
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    void updateTask_CategoryNotFound_ShouldThrow() {
        TaskCreateDto dto = new TaskCreateDto(); dto.setCategoryId(999L);
        Task task = new Task();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.updateTask(1L, dto));
        verify(taskRepository).findById(1L);
        verify(categoryRepository).findById(999L);
    }

    // ---------------- deleteTask ----------------
    @Test
    void deleteTask_Exists_ShouldDelete() {
        when(taskRepository.existsById(1L)).thenReturn(true);

        taskService.deleteTask(1L);

        verify(taskRepository).existsById(1L);
        verify(taskRepository).deleteById(1L);
    }

    @Test
    void deleteTask_NotExists_ShouldThrow() {
        when(taskRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTask(1L));
        verify(taskRepository).existsById(1L);
        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    void deleteTask_MultipleCalls_ShouldVerify() {
        when(taskRepository.existsById(1L)).thenReturn(true);

        taskService.deleteTask(1L);
        taskService.deleteTask(1L);

        verify(taskRepository, times(2)).existsById(1L);
        verify(taskRepository, times(2)).deleteById(1L);
    }

    // ---------------- getTaskStatistics ----------------
    @Test
    void getTaskStatistics_ShouldReturnCorrectCounts() {
        Task t1 = new Task(); t1.setStatus(TaskStatus.TODO); t1.setCategory(new Category(){{
            setName("Cat1");
        }});
        Task t2 = new Task(); t2.setStatus(TaskStatus.DONE); t2.setCategory(null);

        when(taskRepository.findAll()).thenReturn(List.of(t1, t2));

        StatisticsDto stats = taskService.getTaskStatistics();

        assertThat(stats.getTotalTasks()).isEqualTo(2);
        assertThat(stats.getTasksByStatus().get("TODO")).isEqualTo(1);
        assertThat(stats.getTasksByCategory().get("Cat1")).isEqualTo(1);
        assertThat(stats.getTasksByCategory().get("Uncategorized")).isEqualTo(1);
    }

    @Test
    void getTaskStatistics_Empty_ShouldReturnZero() {
        when(taskRepository.findAll()).thenReturn(List.of());

        StatisticsDto stats = taskService.getTaskStatistics();

        assertThat(stats.getTotalTasks()).isZero();
        assertThat(stats.getTasksByStatus()).isEmpty();
        assertThat(stats.getTasksByCategory()).isEmpty();
    }

    @Test
    void getStatisticsForCsv_ShouldReturnRows() {
        Task t = new Task(); t.setStatus(TaskStatus.TODO); t.setCategory(new Category(){{
            setName("Cat1");
        }});
        when(taskRepository.findAll()).thenReturn(List.of(t));

        List<String[]> rows = taskService.getStatisticsForCsv();

        assertThat(rows).isNotEmpty();
    }

    // ---------------- JDBC ----------------
    @Test
    void getAllTasksJdbc_ShouldCallDao() {
        when(taskJdbcDao.findAllAsDtos()).thenReturn(List.of(new TaskDto()));

        List<TaskDto> list = taskService.getAllTasksJdbc();

        assertThat(list).hasSize(1);
        verify(taskJdbcDao).findAllAsDtos();
    }
    @Test
    void getTasksWithFilters_AllSortingOptions_ShouldUseJavaField() {
        Task task = new Task(); task.setId(1L);
        Page<Task> page = new PageImpl<>(List.of(task));
        when(taskRepository.searchTasks(any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);
        when(taskMapper.toDto(task)).thenReturn(new TaskDto(){{
            setId(1L);
        }});

        String[] sorts = {"dueDate,asc", "due_date,desc", "createdAt,asc", "updatedAt,desc", "invalidField,asc"};
        for (String s : sorts) {
            taskService.getTasksWithFilters(null, null, null, null, "", 0, 10, s);
        }

        verify(taskRepository, times(sorts.length)).searchTasks(any(), any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void createTaskJdbc_ShouldInsertAndReturn() {
        TaskCreateDto dto = new TaskCreateDto();
        dto.setTitle("JDBC Task");
        dto.setStatus(TaskStatus.TODO);

        TaskDto inserted = new TaskDto();
        inserted.setId(1L);
        when(taskJdbcDao.insert(any())).thenReturn(inserted);

        TaskDto result = taskService.createTaskJdbc(dto);

        assertThat(result.getId()).isEqualTo(1L);
        verify(taskJdbcDao).insert(any());
    }

    @Test
    void updateTaskJdbc_ShouldUpdateExistingTask() {
        TaskDto existing = new TaskDto();
        existing.setId(1L);
        existing.setTitle("Old");

        when(taskJdbcDao.findAllAsDtos()).thenReturn(List.of(existing));
        TaskCreateDto dto = new TaskCreateDto();
        dto.setTitle("Updated");

        TaskDto result = taskService.updateTaskJdbc(1L, dto);

        assertThat(result.getTitle()).isEqualTo("Updated");
        verify(taskJdbcDao).update(existing);
    }

    @Test
    void updateTaskJdbc_NotFound_ShouldThrow() {
        when(taskJdbcDao.findAllAsDtos()).thenReturn(List.of());
        TaskCreateDto dto = new TaskCreateDto();

        assertThrows(ResourceNotFoundException.class, () -> taskService.updateTaskJdbc(1L, dto));
    }

    @Test
    void deleteTaskJdbc_ShouldDelete() {
        when(taskJdbcDao.delete(1L)).thenReturn(1);

        taskService.deleteTaskJdbc(1L);

        verify(taskJdbcDao).delete(1L);
    }

    @Test
    void deleteTaskJdbc_NotFound_ShouldThrow() {
        when(taskJdbcDao.delete(1L)).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTaskJdbc(1L));
    }

    @Test
    void getStatisticsForCsv_ShouldHandleEmptyMaps() {
        StatisticsDto stats = new StatisticsDto(0L, Map.of(), Map.of());
        TaskService spyService = spy(taskService);
        doReturn(stats).when(spyService).getTaskStatistics();

        List<String[]> rows = spyService.getStatisticsForCsv();

        assertThat(rows).isNotEmpty();
        assertThat(rows.get(1)[1]).isEqualTo("0"); // total tasks
    }

    @Test
    void toCreateDto_ShouldMapFields() {
        TaskDto dto = new TaskDto();
        dto.setTitle("T");
        dto.setDescription("D");
        dto.setStatus(TaskStatus.TODO);
        dto.setDueDate(LocalDate.now());
        dto.setCategoryId(1L);

        TaskCreateDto createDto = taskService.toCreateDto(dto);

        assertThat(createDto.getTitle()).isEqualTo("T");
        assertThat(createDto.getDescription()).isEqualTo("D");
        assertThat(createDto.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(createDto.getDueDate()).isEqualTo(dto.getDueDate());
        assertThat(createDto.getCategoryId()).isEqualTo(1L);
    }

}
