package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class FilmService {

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final UserService userService;

    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
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
        Film updated = filmStorage.update(film);
        log.info("Film updated: {}", updated);
        return updated;
    }

    public void addLike(long filmId, long userId) {
        log.info("User {} adding like to film {}", userId, filmId);
        Film film = getFilmOrThrow(filmId);
        userService.findById(userId);
        film.getLikes().add(userId);
        filmStorage.update(film);
        log.info("User {} liked film {}", userId, filmId);
    }

    public void removeLike(long filmId, long userId) {
        log.info("User {} removing like from film {}", userId, filmId);
        Film film = getFilmOrThrow(filmId);
        userService.findById(userId);
        film.getLikes().remove(userId);
        filmStorage.update(film);
        log.info("User {} removed like from film {}", userId, filmId);
    }

    public List<Film> getPopular(int count) {
        log.info("Getting {} popular films", count);
        if (count <= 0) {
            throw new ValidationException("Count must be greater than zero");
        }
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .toList();
    }

    private Film getFilmOrThrow(long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Film with id " + id + " not found"));
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Invalid release date: {}", film.getReleaseDate());
            throw new ValidationException("Release date cannot be before December 28, 1895");
        }
    }
}
