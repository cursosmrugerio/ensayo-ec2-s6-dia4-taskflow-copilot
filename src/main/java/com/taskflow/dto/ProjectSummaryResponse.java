package com.taskflow.dto;

import java.util.Map;

/**
 * ProjectSummaryResponse — contrato de salida de GET /projects/{id}/summary.
 * Un record: Jackson lo serializa por sus componentes, en este orden.
 */
public record ProjectSummaryResponse(
        Long projectId,
        String projectName,
        long totalTasks,
        Map<String, Long> byStatus,
        long overdue
) {
}
