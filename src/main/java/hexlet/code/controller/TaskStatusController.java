package hexlet.code.controller;

import hexlet.code.dto.TaskStatusDto;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.service.TaskStatusService;;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;

import static hexlet.code.controller.TaskStatusController.TASK_STATUS_CONTROLLER_PATH;
import static org.springframework.http.HttpStatus.CREATED;


@AllArgsConstructor
@RestController
@RequestMapping("${base-url}" + TASK_STATUS_CONTROLLER_PATH)
public class TaskStatusController {

    public static final String TASK_STATUS_CONTROLLER_PATH = "/statuses";
    public static final String ID = "/{id}";

    private final TaskStatusRepository taskStatusRepository;

    private final TaskStatusService taskStatusService;

    @PostMapping()
    @ResponseStatus(CREATED)
    public TaskStatus createTaskStatus(@RequestBody @Valid TaskStatusDto taskStatusDto) {
        return taskStatusService.createStatus(taskStatusDto);
    }

    @GetMapping
    public List<TaskStatus> getAll() {
        return taskStatusRepository.findAll().stream().toList();
    }

    @GetMapping(ID)
    public TaskStatus getById(@PathVariable Long id) {
        return taskStatusRepository.findById(id).get();
    }

    @PutMapping(ID)
    public TaskStatus updateTaskStatus(@PathVariable Long id, @RequestBody @Valid TaskStatusDto taskStatusDto) {
        return taskStatusService.updateStatus(taskStatusDto, id);
    }

    @DeleteMapping(ID)
    public void deleteTaskStatus(@PathVariable Long id) {
        taskStatusRepository.deleteById(id);
    }
}
