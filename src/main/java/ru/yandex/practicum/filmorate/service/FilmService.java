package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class FilmService {

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);
    private static final String RELEASE_DATE_ERROR = "Release date cannot be before " + CINEMA_BIRTHDAY;

    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreService genreService;
    private final MpaService mpaService;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       UserService userService,
                       GenreService genreService,
                       MpaService mpaService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.genreService = genreService;
        this.mpaService = mpaService;
    }

    public Collection<Film> findAll() {
        log.info("Getting all films");
        return filmStorage.findAll();
    }

    public Film findById(long id) {
        log.info("Getting film by id {}", id);
        return getFilmOrThrow(id);
    }

    public Film create(Film film) {
        log.info("Creating film");
        validateReleaseDate(film);
        resolveReferences(film);
        Film created = filmStorage.create(film);
        log.info("Film created: {}", created);
        return created;
    }

    public Film update(Film film) {
        log.info("Updating film with id {}", film.getId());
        if (film.getId() <= 0) {
            throw new ValidationException("Film id is required");
        }
        getFilmOrThrow(film.getId());
        validateReleaseDate(film);
        resolveReferences(film);
        Film updated = filmStorage.update(film);
        log.info("Film updated: {}", updated);
        return updated;
    }

    public void addLike(long filmId, long userId) {
        log.info("User {} adding like to film {}", userId, filmId);
        getFilmOrThrow(filmId);
        userService.findById(userId);
        filmStorage.addLike(filmId, userId);
        log.info("User {} liked film {}", userId, filmId);
    }

    public void removeLike(long filmId, long userId) {
        log.info("User {} removing like from film {}", userId, filmId);
        getFilmOrThrow(filmId);
        userService.findById(userId);
        filmStorage.removeLike(filmId, userId);
        log.info("User {} removed like from film {}", userId, filmId);
    }

    public List<Film> getPopular(int count) {
        log.info("Getting {} popular films", count);
        if (count <= 0) {
            throw new ValidationException("Count must be greater than zero");
        }
        return filmStorage.getPopular(count);
    }

    private Film getFilmOrThrow(long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Film with id " + id + " not found"));
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Invalid release date: {}", film.getReleaseDate());
            throw new ValidationException(RELEASE_DATE_ERROR);
        }
    }

    private void resolveReferences(Film film) {
        if (film.getMpa() != null) {
            Mpa mpa = mpaService.findById(film.getMpa().getId());
            film.setMpa(mpa);
        }
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            film.setGenres(new LinkedHashSet<>());
            return;
        }
        Set<Genre> genres = film.getGenres().stream()
                .map(genre -> genreService.findById(genre.getId()))
                .sorted(Comparator.comparingInt(Genre::getId))
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        film.setGenres(genres);
    }
}
