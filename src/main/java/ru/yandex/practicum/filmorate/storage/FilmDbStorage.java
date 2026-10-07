package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private static final String FILMS_TABLE = "films";
    private static final String ID_COLUMN = "id";

    private static final String SELECT_FILMS =
            "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name "
                    + "FROM films AS f LEFT JOIN mpa AS m ON f.mpa_id = m.id";
    private static final String SELECT_ALL_FILMS = SELECT_FILMS + " ORDER BY f.id";
    private static final String SELECT_FILM_BY_ID = SELECT_FILMS + " WHERE f.id = ?";
    private static final String SELECT_POPULAR_FILMS = SELECT_FILMS
            + " ORDER BY (SELECT COUNT(*) FROM film_likes AS l WHERE l.film_id = f.id) DESC, f.id LIMIT ?";
    private static final String UPDATE_FILM = "UPDATE films SET name = ?, description = ?, release_date = ?, "
            + "duration = ?, mpa_id = ? WHERE id = ?";
    private static final String INSERT_FILM_GENRE = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_FILM_GENRES = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String SELECT_FILM_GENRES =
            "SELECT g.id, g.name FROM film_genres AS fg JOIN genres AS g ON fg.genre_id = g.id "
                    + "WHERE fg.film_id = ? ORDER BY g.id";
    private static final String SELECT_GENRES_BY_FILM_IDS = "SELECT fg.film_id, g.id, g.name FROM film_genres AS fg "
            + "JOIN genres AS g ON fg.genre_id = g.id WHERE fg.film_id IN (%s) ORDER BY g.id";
    private static final String INSERT_LIKE =
            "MERGE INTO film_likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String SELECT_FILM_LIKES = "SELECT user_id FROM film_likes WHERE film_id = ?";
    private static final String SELECT_LIKES_BY_FILM_IDS =
            "SELECT film_id, user_id FROM film_likes WHERE film_id IN (%s)";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert filmInsert;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(FILMS_TABLE)
                .usingGeneratedKeyColumns(ID_COLUMN);
    }

    @Override
    public Collection<Film> findAll() {
        List<Film> films = jdbcTemplate.query(SELECT_ALL_FILMS, filmMapper());
        loadGenres(films);
        loadLikes(films);
        return films;
    }

    @Override
    @Transactional
    public Film create(Film film) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", film.getName());
        parameters.put("description", film.getDescription());
        parameters.put("release_date", Date.valueOf(film.getReleaseDate()));
        parameters.put("duration", film.getDuration());
        parameters.put("mpa_id", film.getMpa().getId());
        long id = filmInsert.executeAndReturnKey(parameters).longValue();
        film.setId(id);
        saveGenres(film);
        film.setGenres(readGenres(id));
        film.setLikes(readLikes(id));
        return film;
    }

    @Override
    @Transactional
    public Film update(Film film) {
        jdbcTemplate.update(UPDATE_FILM, film.getName(), film.getDescription(),
                Date.valueOf(film.getReleaseDate()), film.getDuration(),
                film.getMpa().getId(), film.getId());
        saveGenres(film);
        film.setGenres(readGenres(film.getId()));
        film.setLikes(readLikes(film.getId()));
        return film;
    }

    @Override
    public Optional<Film> findById(long id) {
        try {
            Film film = jdbcTemplate.queryForObject(SELECT_FILM_BY_ID, filmMapper(), id);
            if (film != null) {
                film.setGenres(readGenres(id));
                film.setLikes(readLikes(id));
            }
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void addLike(long filmId, long userId) {
        jdbcTemplate.update(INSERT_LIKE, filmId, userId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        jdbcTemplate.update(DELETE_LIKE, filmId, userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        List<Film> films = jdbcTemplate.query(SELECT_POPULAR_FILMS, filmMapper(), count);
        loadGenres(films);
        loadLikes(films);
        return films;
    }

    private void saveGenres(Film film) {
        jdbcTemplate.update(DELETE_FILM_GENRES, film.getId());
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        List<Object[]> batch = new ArrayList<>();
        Set<Integer> genreIds = new LinkedHashSet<>();
        for (Genre genre : film.getGenres()) {
            if (genreIds.add(genre.getId())) {
                batch.add(new Object[]{film.getId(), genre.getId()});
            }
        }
        jdbcTemplate.batchUpdate(INSERT_FILM_GENRE, batch);
    }

    private Set<Genre> readGenres(long filmId) {
        List<Genre> genres = jdbcTemplate.query(SELECT_FILM_GENRES, genreMapper(), filmId);
        return new LinkedHashSet<>(genres);
    }

    private Set<Long> readLikes(long filmId) {
        return new HashSet<>(jdbcTemplate.queryForList(SELECT_FILM_LIKES, Long.class, filmId));
    }

    private void loadGenres(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }
        List<Long> filmIds = films.stream().map(Film::getId).toList();
        Map<Long, Set<Genre>> genresByFilm = new HashMap<>();
        jdbcTemplate.query(String.format(SELECT_GENRES_BY_FILM_IDS, SqlUtils.placeholders(filmIds.size())), rs -> {
            genresByFilm.computeIfAbsent(rs.getLong("film_id"), key -> new LinkedHashSet<>())
                    .add(new Genre(rs.getInt("id"), rs.getString("name")));
        }, filmIds.toArray());
        for (Film film : films) {
            film.setGenres(genresByFilm.getOrDefault(film.getId(), new LinkedHashSet<>()));
        }
    }

    private void loadLikes(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }
        List<Long> filmIds = films.stream().map(Film::getId).toList();
        Map<Long, Set<Long>> likesByFilm = new HashMap<>();
        jdbcTemplate.query(String.format(SELECT_LIKES_BY_FILM_IDS, SqlUtils.placeholders(filmIds.size())), rs -> {
            likesByFilm.computeIfAbsent(rs.getLong("film_id"), key -> new HashSet<>())
                    .add(rs.getLong("user_id"));
        }, filmIds.toArray());
        for (Film film : films) {
            film.setLikes(likesByFilm.getOrDefault(film.getId(), new HashSet<>()));
        }
    }

    private RowMapper<Genre> genreMapper() {
        return (ResultSet rs, int rowNum) -> new Genre(rs.getInt("id"), rs.getString("name"));
    }

    private RowMapper<Film> filmMapper() {
        return (ResultSet rs, int rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));
            int mpaId = rs.getInt("mpa_id");
            if (!rs.wasNull()) {
                film.setMpa(new Mpa(mpaId, rs.getString("mpa_name")));
            }
            return film;
        };
    }
}
