package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;

@Slf4j
@Service
public class FilmService {

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;

    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        validateReleaseDate(film);
        Film created = filmStorage.create(film);
        log.info("Film created: {}", created);
        return created;
    }

    public Film update(Film film) {
        if (film.getId() <= 0) {
            throw new ValidationException("Film id is required");
        }
        validateReleaseDate(film);
        Film updated = filmStorage.update(film);
        log.info("Film updated: {}", updated);
        return updated;
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Invalid release date: {}", film.getReleaseDate());
            throw new ValidationException("Release date cannot be before December 28, 1895");
        }
    }
}
