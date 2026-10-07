package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component("genreDbStorage")
public class GenreDbStorage implements GenreStorage {

    private static final String SELECT_ALL = "SELECT id, name FROM genres ORDER BY id";
    private static final String SELECT_BY_ID = "SELECT id, name FROM genres WHERE id = ?";
    private static final String SELECT_BY_IDS = "SELECT id, name FROM genres WHERE id IN (%s) ORDER BY id";

    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genre> findAll() {
        return jdbcTemplate.query(SELECT_ALL, genreMapper());
    }

    @Override
    public Optional<Genre> findById(int id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(SELECT_BY_ID, genreMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Genre> findAllByIds(Collection<Integer> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return jdbcTemplate.query(String.format(SELECT_BY_IDS, SqlUtils.placeholders(ids.size())),
                genreMapper(), ids.toArray());
    }

    private RowMapper<Genre> genreMapper() {
        return (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name"));
    }
}
