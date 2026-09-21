package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JdbcTest
@AutoConfigureTestDatabase
@Import(GenreDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDbStorageTest {

    private final GenreDbStorage genreStorage;

    @Test
    void findAllReturnsSixGenresOrderedById() {
        assertThat(genreStorage.findAll())
                .extracting(Genre::getId, Genre::getName)
                .containsExactly(
                        tuple(1, "Комедия"),
                        tuple(2, "Драма"),
                        tuple(3, "Мультфильм"),
                        tuple(4, "Триллер"),
                        tuple(5, "Документальный"),
                        tuple(6, "Боевик"));
    }

    @Test
    void findByIdReturnsGenre() {
        assertThat(genreStorage.findById(1))
                .isPresent()
                .hasValueSatisfying(genre -> {
                    assertThat(genre).hasFieldOrPropertyWithValue("id", 1);
                    assertThat(genre).hasFieldOrPropertyWithValue("name", "Комедия");
                });
    }

    @Test
    void findAllByIdsReturnsRequestedGenresOrderedById() {
        assertThat(genreStorage.findAllByIds(List.of(3, 1)))
                .extracting(Genre::getId, Genre::getName)
                .containsExactly(tuple(1, "Комедия"), tuple(3, "Мультфильм"));
    }

    @Test
    void findAllByIdsSkipsUnknownIds() {
        assertThat(genreStorage.findAllByIds(List.of(1, 9999)))
                .extracting(Genre::getId)
                .containsExactly(1);
    }

    @Test
    void findAllByIdsReturnsEmptyListForEmptyInput() {
        assertThat(genreStorage.findAllByIds(List.of())).isEmpty();
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertThat(genreStorage.findById(9999)).isEmpty();
    }
}
