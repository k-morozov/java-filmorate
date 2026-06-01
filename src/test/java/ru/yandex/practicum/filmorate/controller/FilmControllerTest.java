package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;

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
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Film validFilm() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("A mind-bending thriller");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);
        return film;
    }

    @Test
    void createFilm_validData_returns201() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Inception"));
    }

    @Test
    void createFilm_nullName_returns400() throws Exception {
        Film film = validFilm();
        film.setName(null);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_emptyName_returns400() throws Exception {
        Film film = validFilm();
        film.setName("");

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_blankName_returns400() throws Exception {
        Film film = validFilm();
        film.setName("   ");

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_descriptionExactly200Chars_returns201() throws Exception {
        Film film = validFilm();
        film.setDescription("A".repeat(200));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isCreated());
    }

    @Test
    void createFilm_description201Chars_returns400() throws Exception {
        Film film = validFilm();
        film.setDescription("A".repeat(201));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_releaseDateExactlyCinemaBirthday_returns201() throws Exception {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isCreated());
    }

    @Test
    void createFilm_releaseDateBeforeCinemaBirthday_returns400() throws Exception {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_nullReleaseDate_returns400() throws Exception {
        Film film = validFilm();
        film.setReleaseDate(null);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_zeroDuration_returns400() throws Exception {
        Film film = validFilm();
        film.setDuration(0);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_negativeDuration_returns400() throws Exception {
        Film film = validFilm();
        film.setDuration(-1);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateFilm_validData_returns200() throws Exception {
        String response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm())))
                .andReturn().getResponse().getContentAsString();
        Film created = objectMapper.readValue(response, Film.class);

        created.setName("Updated Name");
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void updateFilm_invalidReleaseDate_returns400() throws Exception {
        String response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm())))
                .andReturn().getResponse().getContentAsString();
        Film created = objectMapper.readValue(response, Film.class);

        created.setReleaseDate(LocalDate.of(1800, 1, 1));
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateFilm_notFound_returns404() throws Exception {
        Film film = validFilm();
        film.setId(999L);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllFilms_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllFilms_withItems_returnsFilms() throws Exception {
        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFilm())));

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Inception"));
    }

    @Test
    void getFilmById_exists_returns200() throws Exception {
        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFilm())));

        mockMvc.perform(get("/films/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Inception"));
    }

    @Test
    void getFilmById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/films/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addLike_validFilmAndUser_returns204() throws Exception {
        createUser();
        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFilm())));

        mockMvc.perform(put("/films/1/like/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void addLike_duplicate_isIdempotent() throws Exception {
        createUser();
        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFilm())));

        mockMvc.perform(put("/films/1/like/1"));
        mockMvc.perform(put("/films/1/like/1"));

        mockMvc.perform(get("/films/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likes.length()").value(1));
    }

    @Test
    void addLike_filmNotFound_returns404() throws Exception {
        createUser();

        mockMvc.perform(put("/films/999/like/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addLike_userNotFound_returns404() throws Exception {
        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFilm())));

        mockMvc.perform(put("/films/1/like/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeLike_validFilmAndUser_returns204() throws Exception {
        createUser();
        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFilm())));
        mockMvc.perform(put("/films/1/like/1"));

        mockMvc.perform(delete("/films/1/like/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeLike_filmNotFound_returns404() throws Exception {
        createUser();

        mockMvc.perform(delete("/films/999/like/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeLike_userNotFound_returns404() throws Exception {
        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFilm())));

        mockMvc.perform(delete("/films/1/like/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPopular_defaultCount_returns200WithList() throws Exception {
        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFilm())));

        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getPopular_zeroCount_returns400() throws Exception {
        mockMvc.perform(get("/films/popular?count=0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPopular_negativeCount_returns400() throws Exception {
        mockMvc.perform(get("/films/popular?count=-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPopular_customCount_limitsResult() throws Exception {
        for (int i = 0; i < 3; i++) {
            Film film = validFilm();
            film.setName("Film " + i);
            mockMvc.perform(post("/films")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(film)));
        }

        mockMvc.perform(get("/films/popular?count=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getPopular_sortedByLikes() throws Exception {
        createUser();
        Film film1 = validFilm();
        film1.setName("LessPopular");
        Film film2 = validFilm();
        film2.setName("MorePopular");
        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film1)));
        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film2)));

        mockMvc.perform(put("/films/2/like/1"));

        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("MorePopular"));
    }

    private void createUser() throws Exception {
        ru.yandex.practicum.filmorate.model.User user = new ru.yandex.practicum.filmorate.model.User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(java.time.LocalDate.of(1990, 1, 1));
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());
    }
}
