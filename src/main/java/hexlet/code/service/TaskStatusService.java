package hexlet.code.service;

import hexlet.code.dto.TaskStatusDto;
import hexlet.code.model.TaskStatus;
import java.util.List;

public interface TaskStatusService {

    TaskStatus getStatus(long id);
    List<TaskStatus> getStatuses();
    TaskStatus createStatus(TaskStatusDto taskStatusDto);
    TaskStatus updateStatus(TaskStatusDto taskStatusDto, long id);
    void deleteStatus(long id);
}
