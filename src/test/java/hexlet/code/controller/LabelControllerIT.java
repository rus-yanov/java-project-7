package hexlet.code.controller;

import hexlet.code.dto.LabelDto;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.config.SpringConfigForIT;
import hexlet.code.utils.TestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static hexlet.code.config.SpringConfigForIT.TEST_PROFILE;
import static hexlet.code.controller.LabelController.LABEL_CONTROLLER_PATH;
import static hexlet.code.controller.TaskStatusController.ID;
import static hexlet.code.utils.TestUtils.SIZE_OF_EMPTY_REPOSITORY;
import static hexlet.code.utils.TestUtils.SIZE_OF_ONE_ITEM_REPOSITORY;
import static hexlet.code.utils.TestUtils.asJson;
import static hexlet.code.utils.TestUtils.fromJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@AutoConfigureMockMvc
@ActiveProfiles(TEST_PROFILE)
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = SpringConfigForIT.class)

public class LabelControllerIT {

    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private TestUtils utils;

    @BeforeEach
    public void before() throws Exception {
        utils.regDefaultUser();
        utils.regDefaultStatus();
    }
    @AfterEach
    public void clear() {
        utils.tearDown();
    }

    @Test
    public void createLabel() throws Exception {

        assertThat(labelRepository.count()).isEqualTo(SIZE_OF_EMPTY_REPOSITORY);

        final LabelDto defaultLabelDto = new LabelDto("Default label");

        final var response = utils.performAuthorizedRequest(
                        post(LABEL_CONTROLLER_PATH)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJson(defaultLabelDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        final Label label = TestUtils.fromJson(response.getContentAsString(), new TypeReference<>() { });
        assertThat(label.getName()).isEqualTo(defaultLabelDto.getName());
        assertThat(labelRepository.count()).isEqualTo(SIZE_OF_ONE_ITEM_REPOSITORY);
    }

    @Test
    public void getLabel() throws Exception {
        utils.regDefaultLabel();
        final Label expectedLabel = labelRepository.findAll().get(0);

        final var response = utils.performAuthorizedRequest(
                        get(LABEL_CONTROLLER_PATH + ID, expectedLabel.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final Label label = fromJson(response.getContentAsString(), new TypeReference<>() { });

        Assertions.assertThat(label.getId()).isEqualTo(expectedLabel.getId());
        Assertions.assertThat(label.getName()).isEqualTo(expectedLabel.getName());
    }

    @Test
    public void getAllLabels() throws Exception {
        utils.regDefaultLabel();
        assertThat(labelRepository.count()).isEqualTo(SIZE_OF_ONE_ITEM_REPOSITORY);

        final LabelDto newLabel = new LabelDto("New label");
        utils.regNewInstance(LABEL_CONTROLLER_PATH, newLabel);

        assertThat(labelRepository.count()).isEqualTo(
                SIZE_OF_ONE_ITEM_REPOSITORY + SIZE_OF_ONE_ITEM_REPOSITORY);

        final var response = utils.performAuthorizedRequest(
                        get(LABEL_CONTROLLER_PATH))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final List<Label> labels = fromJson(response.getContentAsString(), new TypeReference<>() { });

        Assertions.assertThat(labels.get(0).getName()).isEqualTo("Default label");
        Assertions.assertThat(labels.get(1).getName()).isEqualTo("New label");
    }

    @Test
    public void updateLabel() throws Exception {
        utils.regDefaultLabel();
        final LabelDto updatedLabel = new LabelDto("Updated label");

        final Long labelId = labelRepository.findAll().get(0).getId();

        final var response = utils.performAuthorizedRequest(
                        put(LABEL_CONTROLLER_PATH + ID, labelId)
                                .content(asJson(updatedLabel))
                                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final Label label = fromJson(response.getContentAsString(), new TypeReference<>() { });

        Assertions.assertThat(label.getName()).isEqualTo(updatedLabel.getName());
    }

    @Test
    public void deleteLabel() throws Exception {
        utils.regDefaultLabel();

        final Long defaultLabelId = labelRepository.findAll().get(0).getId();

        utils.performAuthorizedRequest(
                        delete(LABEL_CONTROLLER_PATH + ID, defaultLabelId))
                .andExpect(status().isOk());

        utils.performAuthorizedRequest(
                        get(LABEL_CONTROLLER_PATH + ID, defaultLabelId))
                .andExpect(status().isNotFound());

    }
}
