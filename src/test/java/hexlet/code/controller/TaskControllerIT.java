package hexlet.code.controller;

import hexlet.code.dto.TaskDto;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.config.SpringConfigForIT;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultActions;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static hexlet.code.config.SpringConfigForIT.TEST_PROFILE;
import static hexlet.code.controller.TaskController.TASK_CONTROLLER_PATH;
import static hexlet.code.controller.TaskController.ID;
import static hexlet.code.utils.TestUtils.SIZE_OF_EMPTY_REPOSITORY;
import static hexlet.code.utils.TestUtils.SIZE_OF_ONE_ITEM_REPOSITORY;
import static hexlet.code.utils.TestUtils.asJson;
import static hexlet.code.utils.TestUtils.fromJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@AutoConfigureMockMvc
@ActiveProfiles(TEST_PROFILE)
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = SpringConfigForIT.class)

public class TaskControllerIT {

    @Autowired
    private TestUtils utils;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @BeforeEach
    public void before() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultStatus();
        utils.regDefaultLabel();
    }

    @AfterEach
    public void clear() {
        utils.tearDown();
    }

    @Test
    public void createTask() throws Exception {

        final TaskDto defaultTask = buildTaskDto();
        assertThat(taskRepository.count()).isEqualTo(SIZE_OF_EMPTY_REPOSITORY);

        utils.performAuthorizedRequest(
                        post(TASK_CONTROLLER_PATH)
                                .content(utils.asJson(defaultTask))
                                .contentType(APPLICATION_JSON))
                .andExpect(status().isCreated());

        assertThat(taskRepository.count()).isEqualTo(SIZE_OF_ONE_ITEM_REPOSITORY);
    }

    @Test
    public void getTaskById() throws Exception {

        final TaskDto defaultTask = buildTaskDto();
        getTaskRequest(defaultTask);

        final Task expectedTask = taskRepository.findAll().stream()
                .filter(Objects::nonNull)
                .findFirst()
                .get();

        final var response = utils.performAuthorizedRequest(
                        get(TASK_CONTROLLER_PATH + ID, expectedTask.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final Task task = fromJson(response.getContentAsString(), new TypeReference<>() { });

        assertThat(expectedTask.getId()).isEqualTo(task.getId());
        assertThat(expectedTask.getName()).isEqualTo(task.getName());
    }

    @Test
    public void getTaskByIdFails() throws Exception {

        final TaskDto defaultTask = buildTaskDto();
        getTaskRequest(defaultTask);

        final Task expectedTask = taskRepository.findAll().stream()
                .filter(Objects::nonNull)
                .findFirst()
                .get();

        utils.performAuthorizedRequest(get(TASK_CONTROLLER_PATH + ID,
                        expectedTask.getId() + 1))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getAllTasks() throws Exception {

        final TaskDto defaultTask = buildTaskDto();
        getTaskRequest(defaultTask);
        final String defaultName = taskRepository.findAll().get(0).getName();

        final var response = utils.performAuthorizedRequest(
                        get(TASK_CONTROLLER_PATH))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final List<Task> tasks = fromJson(response.getContentAsString(), new TypeReference<>() { });
        final String expectedName = tasks.get(0).getName();

        assertThat(expectedName).isEqualTo(defaultName);
    }

    @Test
    public void updateTask() throws Exception {

        final TaskDto taskDto = buildTaskDto();
        getTaskRequest(taskDto);

        final Task defaultTask = taskRepository.findAll().stream()
                .filter(Objects::nonNull)
                .findFirst()
                .get();

        final Long taskId = defaultTask.getId();
        final String oldTaskName = defaultTask.getName();

        taskDto.setName("Updated task title");
        taskDto.setDescription("Updated task description");

        var response = utils.performAuthorizedRequest(
                        put(TASK_CONTROLLER_PATH + ID, taskId)
                                .content(asJson(taskDto))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final Task updatedTask = fromJson(response.getContentAsString(), new TypeReference<>() { });
        final String updatedTaskName = updatedTask.getName();

        assertThat(taskRepository.existsById(taskId)).isTrue();
        assertThat(taskRepository.findById(taskId).get().getName()).isNotEqualTo(oldTaskName);
        assertThat(taskRepository.findById(taskId).get().getName()).isEqualTo(updatedTaskName);
        assertThat(taskRepository.findById(taskId).get().getDescription()).isEqualTo(updatedTask.getDescription());

    }

    @Test
    public void deleteTask() throws Exception {

        final TaskDto defaultTask = buildTaskDto();
        getTaskRequest(defaultTask);

        final Task task = taskRepository.findAll().stream()
                .filter(Objects::nonNull)
                .findFirst()
                .get();

        final Long taskId = task.getId();

        utils.performAuthorizedRequest(
                        delete(TASK_CONTROLLER_PATH + ID, taskId))
                .andExpect(status().isOk());

        assertFalse(taskRepository.existsById(taskId));
    }

    @Test
    public void deleteTaskFail() throws Exception {

        final TaskDto defaultTask = buildTaskDto();
        getTaskRequest(defaultTask);
        assertThat(taskRepository.count()).isEqualTo(SIZE_OF_ONE_ITEM_REPOSITORY);

        final Long defaultTaskId = taskRepository.findAll().get(0).getId();

        final String newUserUsername = "new user";

        utils.performAuthorizedRequest(
                        delete(TASK_CONTROLLER_PATH + ID, defaultTaskId), newUserUsername)
                .andExpect(status().isForbidden());
    }

    private TaskDto buildTaskDto() {

        User defaultUser = userRepository.findAll().stream().filter(Objects::nonNull).findFirst().get();
        TaskStatus defaultStatus = taskStatusRepository.findAll().stream().filter(Objects::nonNull).findFirst().get();
        Label defaultLabel = labelRepository.findAll().stream().filter(Objects::nonNull).findFirst().get();
        return  new TaskDto(
                "task",
                "task_description",
                defaultUser.getId(),
                defaultUser.getId(),
                defaultStatus.getId(),
                Set.of(defaultLabel.getId())
        );
    }

    private ResultActions getTaskRequest(TaskDto taskDto) throws Exception {
        return utils.performAuthorizedRequest(
                post(TASK_CONTROLLER_PATH)
                        .content(asJson(taskDto))
                        .contentType(APPLICATION_JSON));
    }
}
