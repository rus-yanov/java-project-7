package hexlet.code.service.implementation;

import com.querydsl.core.types.Predicate;
import hexlet.code.dto.TaskDto;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.service.LabelService;
import hexlet.code.service.TaskService;
import hexlet.code.service.TaskStatusService;
import hexlet.code.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final LabelService labelService;
    private final TaskStatusService taskStatusService;
    private final UserService userService;


    @Override
    public Task createTask(TaskDto taskDto) {
        return taskRepository.save(buildTask(taskDto));
    }

    @Override
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow();
    }

    @Override
    public List<Task> getAllTasks(Predicate predicate) {
        return (List<Task>) taskRepository.findAll(predicate);
    }

    @Override
    public Task updateTask(TaskDto taskDto, Long id) {
        Task temporaryTask = buildTask(taskDto);
        final Task task = taskRepository.findById(id)
                .orElseThrow();
        task.setName(temporaryTask.getName());
        task.setDescription(temporaryTask.getDescription());
        task.setExecutor(temporaryTask.getExecutor());
        task.setTaskStatus(temporaryTask.getTaskStatus());
        task.setLabels(temporaryTask.getLabels());
        return taskRepository.save(task);
    }

    @Override
    public void deleteTask(Long id) {
        final Task task = taskRepository.findById(id)
                .orElseThrow();
        taskRepository.delete(task);
    }

    private Task buildTask(TaskDto taskDto) {
        final User author = userService.getCurrentUser();
        final User executor = Optional.ofNullable(taskDto.getExecutorId())
                .map(userService::getUserById)
                .orElse(null);

        final TaskStatus status = Optional.ofNullable(taskDto.getTaskStatusId())
                .map(taskStatusService::getStatus)
                .orElse(null);


        final Set<Label> labels = Optional.ofNullable(taskDto.getLabelIds())
                .orElse(Set.of())
                .stream()
                .filter(Objects::nonNull)
                .map(labelService::getLabelById)
                .collect(Collectors.toSet());

        return Task.builder()
                .author(author)
                .executor(executor)
                .taskStatus(status)
                .labels(labels)
                .name(taskDto.getName())
                .description(taskDto.getDescription())
                .build();
    }
}
