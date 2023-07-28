package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.config.SpringConfigForIT;
import hexlet.code.dto.TaskStatusDto;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static hexlet.code.controller.TaskStatusController.ID;
import static hexlet.code.controller.TaskStatusController.TASK_STATUS_CONTROLLER_PATH;
import static hexlet.code.utils.TestUtils.SIZE_OF_EMPTY_REPOSITORY;
import static hexlet.code.utils.TestUtils.SIZE_OF_ONE_ITEM_REPOSITORY;
import static hexlet.code.utils.TestUtils.asJson;
import static hexlet.code.utils.TestUtils.fromJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles(SpringConfigForIT.TEST_PROFILE)
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = SpringConfigForIT.class)

public class TaskStatusControllerIT {

    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private TestUtils utils;

    @BeforeEach
    public void before() throws Exception {
        utils.regDefaultUser();
    }

    @AfterEach
    public void clear() {
        utils.tearDown();
    }

    @Test
    public void createStatus() throws Exception {

        assertThat(taskStatusRepository.count()).isEqualTo(SIZE_OF_EMPTY_REPOSITORY);

        final TaskStatusDto statusDto = new TaskStatusDto("Some status");

        final var response = utils.performAuthorizedRequest(
                        post(TASK_STATUS_CONTROLLER_PATH)
                                .content(asJson(statusDto))
                                .contentType(APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        final TaskStatus status = TestUtils.fromJson(response.getContentAsString(), new TypeReference<>() {
        });

        assertThat(status.getName()).isEqualTo(statusDto.getName());
        assertThat(taskStatusRepository.count()).isEqualTo(SIZE_OF_ONE_ITEM_REPOSITORY);
    }

    @Test
    public void getStatusById() throws Exception {

        utils.regDefaultStatus();
        final TaskStatus expectedStatus = taskStatusRepository.findAll().get(0);

        final var response = utils.performAuthorizedRequest(
                        get(TASK_STATUS_CONTROLLER_PATH + ID, expectedStatus.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final TaskStatus taskStatus = fromJson(response.getContentAsString(), new TypeReference<>() { });

        assertThat(expectedStatus.getId()).isEqualTo(taskStatus.getId());
        assertThat(expectedStatus.getName()).isEqualTo(taskStatus.getName());

    }

    @Test
    public void getStatusByIdFails() throws Exception {

        utils.regDefaultStatus();
        final TaskStatus expectedStatus = taskStatusRepository.findAll().get(0);

        utils.performAuthorizedRequest(get(TASK_STATUS_CONTROLLER_PATH + ID,
                        expectedStatus.getId() + 1))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getAllStatuses() throws Exception {

        utils.regDefaultStatus();
        final var response = utils.performAuthorizedRequest(
                        get(TASK_STATUS_CONTROLLER_PATH))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final List<TaskStatus> taskStatuses = fromJson(response.getContentAsString(), new TypeReference<>() { });
        assertThat(taskStatuses).hasSize(SIZE_OF_ONE_ITEM_REPOSITORY);
    }

    @Test
    public void updateStatus() throws Exception {

        final var response = utils.regDefaultStatus()
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        final TaskStatus oldStatus = fromJson(response.getContentAsString(), new TypeReference<>() { });
        final Long statusId = oldStatus.getId();

        final TaskStatusDto newStatus = new TaskStatusDto("Another Status");

        utils.performAuthorizedRequest(
                        put(TASK_STATUS_CONTROLLER_PATH + ID, statusId)
                                .content(asJson(newStatus))
                                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        final var anotherResponse = utils.performAuthorizedRequest(
                        get(TASK_STATUS_CONTROLLER_PATH + ID, statusId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final TaskStatus expectedStatus = fromJson(anotherResponse.getContentAsString(), new TypeReference<>() { });

        assertThat(statusId).isEqualTo(expectedStatus.getId());
        assertThat(newStatus.getName()).isEqualTo(expectedStatus.getName());

    }

    @Test
    public void deleteStatus() throws Exception {

        var response = utils.regDefaultStatus()
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        final TaskStatus status = fromJson(response.getContentAsString(), new TypeReference<>() { });
        final Long statusId = status.getId();

        utils.performAuthorizedRequest(
                        delete(TASK_STATUS_CONTROLLER_PATH + ID, statusId))
                .andExpect(status().isOk());

        utils.performAuthorizedRequest(
                        get(TASK_STATUS_CONTROLLER_PATH + ID, statusId))
                .andExpect(status().isNotFound());

    }
}
