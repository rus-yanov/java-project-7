package hexlet.code.controller;

import hexlet.code.dto.LabelDto;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.config.SpringConfigForIT;
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
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static hexlet.code.config.SpringConfigForIT.TEST_PROFILE;
import static hexlet.code.controller.LabelController.LABEL_CONTROLLER_PATH;
import static hexlet.code.controller.TaskStatusController.ID;
import static hexlet.code.utils.TestUtils.asJson;
import static hexlet.code.utils.TestUtils.fromJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        utils.regDefaultLabel();
    }
    @AfterEach
    public void clear() {
        utils.tearDown();
    }

    @Test
    public void createLabel() throws Exception {

        final LabelDto expectedLabel = new LabelDto("New label");

        final var response = utils.performAuthorizedRequest(
                post(LABEL_CONTROLLER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(expectedLabel)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        final Label label = fromJson(response.getContentAsString(), new TypeReference<>() { });

        assertThat(labelRepository.getReferenceById(label.getId())).isNotNull();
        assertThat(label.getName()).isEqualTo(expectedLabel.getName());
    }

    @Test
    public void getLabel() throws Exception {

        final Label expectedLabel = labelRepository.findFirstByOrderById().get();

        final var response = utils.performAuthorizedRequest(
                        get(LABEL_CONTROLLER_PATH + ID, expectedLabel.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final Label label = fromJson(response.getContentAsString(), new TypeReference<>() { });

        assertThat(label.getId()).isEqualTo(expectedLabel.getId());
        assertThat(label.getName()).isEqualTo(expectedLabel.getName());
    }

    @Test
    public void getAllLabels() throws Exception {

        final LabelDto newLabel = new LabelDto("New label");
        utils.regNewInstance(LABEL_CONTROLLER_PATH, newLabel);

        final var response = utils.performAuthorizedRequest(
                        get(LABEL_CONTROLLER_PATH))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final List<Label> labels = fromJson(response.getContentAsString(), new TypeReference<>() { });
        final List<Label> expected = labelRepository.findAll();

        int i = 0;
        for (var label : labels) {
            assertThat(i < expected.size());
            assertEquals(label.getId(), expected.get(i).getId());
            assertEquals(label.getName(), expected.get(i).getName());
            i++;
        }
    }

    @Test
    public void updateLabel() throws Exception {

        final Label defaultLabel = labelRepository.findFirstByOrderById().get();
        final Long labelId = defaultLabel.getId();
        final String oldLabelName = defaultLabel.getName();

        final LabelDto newLabel = new LabelDto("Updated label");

        final var response = utils.performAuthorizedRequest(
                        put(LABEL_CONTROLLER_PATH + ID, labelId)
                                .content(asJson(newLabel))
                                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final Label updatedLabel = fromJson(response.getContentAsString(), new TypeReference<>() { });
        final String updatedLabelName = updatedLabel.getName();

        assertThat(labelRepository.existsById(labelId)).isTrue();
        assertThat(labelRepository.findById(labelId).get().getName()).isNotEqualTo(oldLabelName);
        assertThat(labelRepository.findById(labelId).get().getName()).isEqualTo(updatedLabelName);
    }

    @Test
    public void deleteLabel() throws Exception {

        final Long defaultLabelId = labelRepository.findFirstByOrderById().get().getId();

        utils.performAuthorizedRequest(
                        delete(LABEL_CONTROLLER_PATH + ID, defaultLabelId))
                .andExpect(status().isOk());

        assertFalse(labelRepository.existsById(defaultLabelId));
    }
}
