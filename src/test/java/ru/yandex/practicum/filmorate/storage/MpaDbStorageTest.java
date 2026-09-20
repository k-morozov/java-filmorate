package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDbStorageTest {

    private final MpaDbStorage mpaStorage;

    @Test
    void findAllReturnsFiveRatingsOrderedById() {
        assertThat(mpaStorage.findAll())
                .extracting(Mpa::getId, Mpa::getName)
                .containsExactly(
                        tuple(1, "G"),
                        tuple(2, "PG"),
                        tuple(3, "PG-13"),
                        tuple(4, "R"),
                        tuple(5, "NC-17"));
    }

    @Test
    void findByIdReturnsRating() {
        assertThat(mpaStorage.findById(5))
                .isPresent()
                .hasValueSatisfying(mpa -> {
                    assertThat(mpa).hasFieldOrPropertyWithValue("id", 5);
                    assertThat(mpa).hasFieldOrPropertyWithValue("name", "NC-17");
                });
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertThat(mpaStorage.findById(9999)).isEmpty();
    }
}
