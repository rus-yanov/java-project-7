package hexlet.code.controller;

import hexlet.code.dto.LoginDto;
import hexlet.code.dto.UserDto;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.config.SpringConfigForIT;
import hexlet.code.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

import static hexlet.code.config.SpringConfigForIT.TEST_PROFILE;
import static hexlet.code.utils.TestUtils.LOGIN;
import static hexlet.code.utils.TestUtils.TEST_USERNAME_1;
import static hexlet.code.utils.TestUtils.TEST_USERNAME_2;
import static hexlet.code.utils.TestUtils.SIZE_OF_EMPTY_REPOSITORY;
import static hexlet.code.utils.TestUtils.SIZE_OF_ONE_ITEM_REPOSITORY;
import static hexlet.code.utils.TestUtils.asJson;
import static hexlet.code.utils.TestUtils.fromJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static hexlet.code.controller.UserController.ID;
import static hexlet.code.controller.UserController.USER_CONTROLLER_PATH;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles(TEST_PROFILE)
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = SpringConfigForIT.class)

public class UserControllerIT {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestUtils utils;

    @AfterEach
    public void clear() {
        utils.tearDown();
    }

    @Test
    public void registration() throws Exception {

        assertThat(userRepository.count()).isEqualTo(SIZE_OF_EMPTY_REPOSITORY);

        final var response = utils.regDefaultUser()
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        final List<User> users = fromJson(response.getContentAsString(), new TypeReference<>() { });
        final List<User> expected = userRepository.findAll();

        assertThat(users).containsAll(expected);
        assertThat(userRepository.count()).isEqualTo(SIZE_OF_ONE_ITEM_REPOSITORY);
    }

    @Test
    public void getUserById() throws Exception {
        utils.regDefaultUser();

        final User expectedUser = userRepository.findAll().get(0);

        final var response = utils.performAuthorizedRequest(
                        get(USER_CONTROLLER_PATH + ID, expectedUser.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final User user = fromJson(response.getContentAsString(), new TypeReference<>() {
        });
        assertThat(expectedUser.getId()).isEqualTo(user.getId());
        assertThat(expectedUser.getEmail()).isEqualTo(user.getEmail());
        assertThat(expectedUser.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(expectedUser.getLastName()).isEqualTo(user.getLastName());
    }

    @Test
    public void getUserByIdFails() throws Exception {
        utils.regDefaultUser();
        final User expectedUser = userRepository.findAll().get(0);
        utils.performAuthorizedRequest(
                        get(USER_CONTROLLER_PATH + ID, expectedUser.getId() + 1))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getAllUsers() throws Exception {
        utils.regDefaultUser();
        final var response = utils.perform(get(USER_CONTROLLER_PATH))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final List<User> users = fromJson(response.getContentAsString(), new TypeReference<>() { });
        final List<User> expected = userRepository.findAll();

        assertThat(users).containsAll(expected);
        assertThat(users).hasSize(SIZE_OF_ONE_ITEM_REPOSITORY);
    }

    @Test
    public void twiceRegTheSameUserFail() throws Exception {
        utils.regDefaultUser().andExpect(status().isCreated());
        utils.regDefaultUser().andExpect(status().isUnprocessableEntity());
        assertThat(userRepository.count()).isEqualTo(SIZE_OF_ONE_ITEM_REPOSITORY);
    }

    @Test
    public void login() throws Exception {
        utils.regDefaultUser();
        final LoginDto rightCredentials = new LoginDto(TEST_USERNAME_1, "0987");

        final var loginRequest = post(LOGIN).content(asJson(rightCredentials)).contentType(APPLICATION_JSON);
        utils.perform(loginRequest)
                .andExpect(status().isOk());
    }

    @Test
    public void loginFails() throws Exception {
        utils.regDefaultUser();
        final LoginDto wrongCredentials = new LoginDto(TEST_USERNAME_2, "password");

        final var loginRequest = post(LOGIN).content(asJson(wrongCredentials)).contentType(APPLICATION_JSON);
        utils.perform(loginRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void updateUser() throws Exception {
        utils.regDefaultUser();
        final UserDto newUserDto = new UserDto(
                TEST_USERNAME_2,
                "new name",
                "new last name",
                "new pwd");

        final Long userId = userRepository.findByEmail(TEST_USERNAME_1).get().getId();

        utils.performAuthorizedRequest(
                        put(USER_CONTROLLER_PATH + ID, userId)
                                .content(asJson(newUserDto))
                                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        assertThat(userRepository.existsById(userId)).isTrue();
        assertNull(userRepository.findByEmail(TEST_USERNAME_1).orElse(null));
        assertNotNull(userRepository.findByEmail(TEST_USERNAME_2).orElse(null));
    }

    @Test
    public void deleteUser() throws Exception {
        utils.regDefaultUser();

        final Long userId = userRepository.findByEmail(TEST_USERNAME_1).get().getId();

        utils.performAuthorizedRequest(
                        delete(USER_CONTROLLER_PATH + ID, userId))
                .andExpect(status().isOk());

        assertFalse(userRepository.existsById(userId));

    }

    @Test
    public void deleteUserFails() throws Exception {
        utils.regDefaultUser();
        final UserDto newUserDto = new UserDto(
                TEST_USERNAME_2,
                "fname",
                "lname",
                "pwd");

        utils.regNewInstance(USER_CONTROLLER_PATH, newUserDto);

        final Long defaultUserId = userRepository.findByEmail(TEST_USERNAME_1).get().getId();
        final Long newUserId = userRepository.findByEmail(TEST_USERNAME_2).get().getId();

        utils.performAuthorizedRequest(
                        delete(USER_CONTROLLER_PATH + ID, defaultUserId))
                .andExpect(status().isOk());

        assertThat(userRepository.count()).isEqualTo(SIZE_OF_ONE_ITEM_REPOSITORY);

        utils.performAuthorizedRequest(
                        delete(USER_CONTROLLER_PATH + ID, newUserId))
                .andExpect(status().isForbidden());
    }
}
