package hexlet.code.service;

import com.querydsl.core.types.Predicate;
import hexlet.code.dto.TaskDto;
import hexlet.code.model.Task;

import java.util.List;

public interface TaskService {

    Task createTask(TaskDto taskDto);
    Task getTaskById(Long id);
    List<Task> getAllTasks(Predicate predicate);
    Task updateTask(TaskDto taskDto, Long id);
    void deleteTask(Long id);

}
