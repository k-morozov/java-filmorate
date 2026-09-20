package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    private Film newFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description of " + name);
        film.setReleaseDate(LocalDate.of(2000, 5, 20));
        film.setDuration(100);
        film.setMpa(new Mpa(3, "PG-13"));
        return film;
    }

    private User newUser(String login) {
        User user = new User();
        user.setEmail(login + "@example.com");
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    private Set<Genre> genres(int... ids) {
        Set<Genre> genres = new LinkedHashSet<>();
        for (int id : ids) {
            genres.add(new Genre(id, null));
        }
        return genres;
    }

    @Test
    void createAssignsIdAndFindByIdReturnsFilm() {
        Film created = filmStorage.create(newFilm("Film"));

        assertThat(created.getId()).isPositive();

        assertThat(filmStorage.findById(created.getId()))
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film).hasFieldOrPropertyWithValue("id", created.getId());
                    assertThat(film).hasFieldOrPropertyWithValue("name", "Film");
                    assertThat(film).hasFieldOrPropertyWithValue("duration", 100);
                    assertThat(film.getReleaseDate()).isEqualTo(LocalDate.of(2000, 5, 20));
                    assertThat(film.getMpa()).isEqualTo(new Mpa(3, "PG-13"));
                    assertThat(film.getGenres()).isEmpty();
                });
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertThat(filmStorage.findById(9999)).isEmpty();
    }

    @Test
    void createStoresGenresWithoutDuplicates() {
        Film film = newFilm("Film");
        film.setGenres(genres(2, 1, 2));

        Film created = filmStorage.create(film);

        assertThat(created.getGenres())
                .extracting(Genre::getId, Genre::getName)
                .containsExactly(tuple(1, "Комедия"), tuple(2, "Драма"));
    }

    @Test
    void findAllReturnsAllFilmsWithGenres() {
        Film first = newFilm("First");
        first.setGenres(genres(1));
        filmStorage.create(first);
        filmStorage.create(newFilm("Second"));

        Collection<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(2)
                .extracting(Film::getName)
                .containsExactly("First", "Second");
        assertThat(films.iterator().next().getGenres()).extracting(Genre::getId).containsExactly(1);
    }

    @Test
    void updateChangesStoredFilmAndGenres() {
        Film film = newFilm("Film");
        film.setGenres(genres(1));
        Film created = filmStorage.create(film);

        created.setName("Updated");
        created.setDuration(190);
        created.setMpa(new Mpa(5, "NC-17"));
        created.setGenres(genres(3));
        filmStorage.update(created);

        assertThat(filmStorage.findById(created.getId()))
                .isPresent()
                .hasValueSatisfying(updated -> {
                    assertThat(updated).hasFieldOrPropertyWithValue("name", "Updated");
                    assertThat(updated).hasFieldOrPropertyWithValue("duration", 190);
                    assertThat(updated.getMpa().getId()).isEqualTo(5);
                    assertThat(updated.getGenres()).extracting(Genre::getId).containsExactly(3);
                });
    }

    @Test
    void addLikeAndRemoveLikeChangeLikes() {
        Film film = filmStorage.create(newFilm("Film"));
        User user = userStorage.create(newUser("liker"));

        filmStorage.addLike(film.getId(), user.getId());

        assertThat(filmStorage.findById(film.getId()))
                .isPresent()
                .hasValueSatisfying(found -> assertThat(found.getLikes()).containsExactly(user.getId()));

        filmStorage.removeLike(film.getId(), user.getId());

        assertThat(filmStorage.findById(film.getId()))
                .isPresent()
                .hasValueSatisfying(found -> assertThat(found.getLikes()).isEmpty());
    }

    @Test
    void addLikeTwiceKeepsSingleLike() {
        Film film = filmStorage.create(newFilm("Film"));
        User user = userStorage.create(newUser("liker"));

        filmStorage.addLike(film.getId(), user.getId());
        filmStorage.addLike(film.getId(), user.getId());

        assertThat(filmStorage.findById(film.getId()))
                .isPresent()
                .hasValueSatisfying(found -> assertThat(found.getLikes()).hasSize(1));
    }

    @Test
    void getPopularSortsByLikesAndRespectsCount() {
        Film unpopular = filmStorage.create(newFilm("Unpopular"));
        Film popular = filmStorage.create(newFilm("Popular"));
        User first = userStorage.create(newUser("first"));
        User second = userStorage.create(newUser("second"));

        filmStorage.addLike(popular.getId(), first.getId());
        filmStorage.addLike(popular.getId(), second.getId());
        filmStorage.addLike(unpopular.getId(), first.getId());

        List<Film> top = filmStorage.getPopular(10);

        assertThat(top).extracting(Film::getId).containsExactly(popular.getId(), unpopular.getId());
        assertThat(filmStorage.getPopular(1)).extracting(Film::getId).containsExactly(popular.getId());
    }
}
