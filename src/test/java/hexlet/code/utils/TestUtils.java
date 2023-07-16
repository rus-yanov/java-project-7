package hexlet.code.utils;

import hexlet.code.component.JWTHelper;
import hexlet.code.dto.LabelDto;
import hexlet.code.dto.TaskStatusDto;
import hexlet.code.dto.UserDto;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;

import static hexlet.code.controller.LabelController.LABEL_CONTROLLER_PATH;
import static hexlet.code.controller.TaskStatusController.TASK_STATUS_CONTROLLER_PATH;
import static hexlet.code.controller.UserController.USER_CONTROLLER_PATH;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Component
public class TestUtils {

    public static final int SIZE_OF_EMPTY_REPOSITORY = 0;
    public static final int SIZE_OF_ONE_ITEM_REPOSITORY = 1;
    public static final String TEST_USERNAME_1 = "blabla@gmail.com";
    public static final String TEST_USERNAME_2 = "badabudu@gmail.com";
    public static final String LOGIN = "/login";

    private final UserDto defaultUser = new UserDto(
            TEST_USERNAME_1,
            "Ivan",
            "Petrov",
            "password");

    private final TaskStatusDto defaultStatus = new TaskStatusDto("Default Status");

    private final LabelDto defaultLabel = new LabelDto("Default label");


    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private JWTHelper jwtHelper;

    public void tearDown() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
        taskStatusRepository.deleteAll();
        labelRepository.deleteAll();
    }

    public ResultActions regDefaultUser() throws Exception {
        return regNewInstance(USER_CONTROLLER_PATH, defaultUser);
    }

    public ResultActions regDefaultStatus() throws Exception {
        return regNewInstance(TASK_STATUS_CONTROLLER_PATH, defaultStatus);
    }

    public void regDefaultLabel() throws Exception {
        regNewInstance(LABEL_CONTROLLER_PATH, defaultLabel);
    }

    public ResultActions regNewInstance(String path, Object userDto) throws Exception {
        return performAuthorizedRequest(post(path)
                .content(asJson(userDto))
                .contentType(MediaType.APPLICATION_JSON));
    }

    public ResultActions performAuthorizedRequest(final MockHttpServletRequestBuilder request) throws Exception {
        final String token = jwtHelper.expiring(Map.of(SPRING_SECURITY_FORM_USERNAME_KEY, TEST_USERNAME_1));
        request.header(AUTHORIZATION, token);

        return perform(request);
    }

    public ResultActions performAuthorizedRequest(
            final MockHttpServletRequestBuilder request, String newUser) throws Exception {
        final String token = jwtHelper.expiring(Map.of(SPRING_SECURITY_FORM_USERNAME_KEY, newUser));
        request.header(AUTHORIZATION, token);

        return perform(request);
    }

    public ResultActions perform(final MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request);
    }

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    public static String asJson(final Object object) throws JsonProcessingException {
        return MAPPER.writeValueAsString(object);
    }

    public static <T> T fromJson(final String json, final TypeReference<T> to) throws JsonProcessingException {
        return MAPPER.readValue(json, to);
    }
}
