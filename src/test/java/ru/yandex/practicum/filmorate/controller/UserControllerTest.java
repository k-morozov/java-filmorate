package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private User validUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void createUser_validData_returns201() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void createUser_nullEmail_returns400() throws Exception {
        User user = validUser();
        user.setEmail(null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_emptyEmail_returns400() throws Exception {
        User user = validUser();
        user.setEmail("");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_emailWithoutAt_returns400() throws Exception {
        User user = validUser();
        user.setEmail("invalidemail.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_nullLogin_returns400() throws Exception {
        User user = validUser();
        user.setLogin(null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_emptyLogin_returns400() throws Exception {
        User user = validUser();
        user.setLogin("");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_loginAllSpaces_returns400() throws Exception {
        User user = validUser();
        user.setLogin("   ");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_loginWithSpaces_returns400() throws Exception {
        User user = validUser();
        user.setLogin("user login");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_emptyName_usesLoginAsName() throws Exception {
        User user = validUser();
        user.setName("");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("userlogin"));
    }

    @Test
    void createUser_nullName_usesLoginAsName() throws Exception {
        User user = validUser();
        user.setName(null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("userlogin"));
    }

    @Test
    void createUser_nullBirthday_returns400() throws Exception {
        User user = validUser();
        user.setBirthday(null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_futureBirthday_returns400() throws Exception {
        User user = validUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_todayBirthday_returns201() throws Exception {
        User user = validUser();
        user.setBirthday(LocalDate.now());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());
    }

    @Test
    void updateUser_validData_returns200() throws Exception {
        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser())))
                .andReturn().getResponse().getContentAsString();
        User created = objectMapper.readValue(response, User.class);

        created.setName("Updated Name");
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void updateUser_notFound_returns404() throws Exception {
        User user = validUser();
        user.setId(999L);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllUsers_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllUsers_withItems_returnsUsers() throws Exception {
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUser())));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("user@example.com"));
    }

    @Test
    void getUserById_exists_returns200() throws Exception {
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUser())));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void getUserById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addFriend_validUsers_returns204() throws Exception {
        createTwoUsers();

        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isNoContent());
    }

    @Test
    void addFriend_selfFriend_returns400() throws Exception {
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUser())));

        mockMvc.perform(put("/users/1/friends/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addFriend_userNotFound_returns404() throws Exception {
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUser())));

        mockMvc.perform(put("/users/999/friends/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addFriend_friendNotFound_returns404() throws Exception {
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUser())));

        mockMvc.perform(put("/users/1/friends/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addFriend_isMutual() throws Exception {
        createTwoUsers();
        mockMvc.perform(put("/users/1/friends/2"));

        mockMvc.perform(get("/users/2/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void removeFriend_validUsers_returns204() throws Exception {
        createTwoUsers();
        mockMvc.perform(put("/users/1/friends/2"));

        mockMvc.perform(delete("/users/1/friends/2"))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeFriend_isAlsoMutual() throws Exception {
        createTwoUsers();
        mockMvc.perform(put("/users/1/friends/2"));

        mockMvc.perform(delete("/users/1/friends/2"));

        mockMvc.perform(get("/users/2/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void removeFriend_userNotFound_returns404() throws Exception {
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUser())));

        mockMvc.perform(delete("/users/999/friends/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeFriend_friendNotFound_returns404() throws Exception {
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUser())));

        mockMvc.perform(delete("/users/1/friends/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFriends_noFriends_returnsEmptyList() throws Exception {
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUser())));

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getFriends_withFriend_returnsFriend() throws Exception {
        createTwoUsers();
        mockMvc.perform(put("/users/1/friends/2"));

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void getFriends_userNotFound_returns404() throws Exception {
        mockMvc.perform(get("/users/999/friends"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCommonFriends_noCommon_returnsEmptyList() throws Exception {
        createTwoUsers();

        mockMvc.perform(get("/users/1/friends/common/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getCommonFriends_withCommon_returnsCommonFriend() throws Exception {
        User third = new User();
        third.setEmail("third@example.com");
        third.setLogin("thirdlogin");
        third.setName("Third User");
        third.setBirthday(LocalDate.of(1992, 3, 3));

        createTwoUsers();
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(third)));

        mockMvc.perform(put("/users/1/friends/3"));
        mockMvc.perform(put("/users/2/friends/3"));

        mockMvc.perform(get("/users/1/friends/common/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3));
    }

    @Test
    void getCommonFriends_userNotFound_returns404() throws Exception {
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUser())));

        mockMvc.perform(get("/users/999/friends/common/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCommonFriends_otherUserNotFound_returns404() throws Exception {
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUser())));

        mockMvc.perform(get("/users/1/friends/common/999"))
                .andExpect(status().isNotFound());
    }

    private void createTwoUsers() throws Exception {
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUser())))
                .andExpect(status().isCreated());

        User second = new User();
        second.setEmail("second@example.com");
        second.setLogin("secondlogin");
        second.setName("Second User");
        second.setBirthday(LocalDate.of(1991, 2, 2));
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isCreated());
    }
}
