package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Component("mpaDbStorage")
public class MpaDbStorage implements MpaStorage {

    private static final String SELECT_ALL = "SELECT id, name FROM mpa ORDER BY id";
    private static final String SELECT_BY_ID = "SELECT id, name FROM mpa WHERE id = ?";

    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Mpa> findAll() {
        return jdbcTemplate.query(SELECT_ALL, mpaMapper());
    }

    @Override
    public Optional<Mpa> findById(int id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(SELECT_BY_ID, mpaMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private RowMapper<Mpa> mpaMapper() {
        return (rs, rowNum) -> new Mpa(rs.getInt("id"), rs.getString("name"));
    }
}
