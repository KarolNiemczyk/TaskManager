package com.example.task.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class StatisticsDto {
    private long totalTasks;
    private Map<String, Long> tasksByStatus;
    private Map<String, Long> tasksByCategory;
}
