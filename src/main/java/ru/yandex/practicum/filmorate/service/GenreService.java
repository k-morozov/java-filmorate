package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class GenreService {

    private final GenreStorage genreStorage;

    public GenreService(@Qualifier("genreDbStorage") GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public List<Genre> findAll() {
        log.info("Getting all genres");
        return genreStorage.findAll();
    }

    public List<Genre> findAllByIds(Collection<Integer> ids) {
        log.info("Getting genres by ids {}", ids);
        Set<Integer> requested = new LinkedHashSet<>(ids);
        List<Genre> genres = genreStorage.findAllByIds(requested);
        if (genres.size() < requested.size()) {
            Set<Integer> missing = new LinkedHashSet<>(requested);
            genres.forEach(genre -> missing.remove(genre.getId()));
            throw new NotFoundException("Genres with ids " + missing + " not found");
        }
        return genres;
    }

    public Genre findById(int id) {
        log.info("Getting genre by id {}", id);
        return genreStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Genre with id " + id + " not found"));
    }
}
