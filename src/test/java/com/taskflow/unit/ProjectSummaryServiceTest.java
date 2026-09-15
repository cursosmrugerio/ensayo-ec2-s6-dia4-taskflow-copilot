package com.taskflow.unit;

import com.taskflow.dto.ProjectSummaryResponse;
import com.taskflow.model.Priority;
import com.taskflow.model.Project;
import com.taskflow.model.Task;
import com.taskflow.model.TaskStatus;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.ProjectService;
import com.taskflow.exception.TaskValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectSummaryServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService service;

    private final Project proyecto = new Project(2L, "App Móvil", "d", 2L, null);

    @Test
    void summary_cuentaEstadosYVencidas() throws TaskValidationException {
        // Tareas: TODO x1, IN_PROGRESS x2, DONE x1; una de IN_PROGRESS vencida
        Task t1 = new Task(1L, "T01", "d", TaskStatus.TODO, Priority.MED, 2L, null, null);
        Task t2 = new Task(2L, "T02", "d", TaskStatus.IN_PROGRESS, Priority.HIGH, 2L, 1L, LocalDate.now().minusDays(2));
        Task t3 = new Task(3L, "T03", "d", TaskStatus.IN_PROGRESS, Priority.LOW, 2L, 1L, null);
        Task t4 = new Task(4L, "T04", "d", TaskStatus.DONE, Priority.MED, 2L, 1L, LocalDate.now().minusDays(10));

        when(taskRepository.findByProjectId(2L)).thenReturn(List.of(t1, t2, t3, t4));

        ProjectSummaryResponse expected = new ProjectSummaryResponse(2L, "App Móvil", 4,
                java.util.Map.of("TODO", 1L, "IN_PROGRESS", 2L, "DONE", 1L), 1L);

        assertEquals(expected, service.summary(proyecto));
    }

    @Test
    void summary_proyectoSinTareas_todoEnCero() {
        when(taskRepository.findByProjectId(3L)).thenReturn(List.of());
        Project p = new Project(3L, "Vacio", "d", 1L, null);

        ProjectSummaryResponse expected = new ProjectSummaryResponse(3L, "Vacio", 0,
                java.util.Map.of("TODO", 0L, "IN_PROGRESS", 0L, "DONE", 0L), 0L);

        assertEquals(expected, service.summary(p));
    }
}
