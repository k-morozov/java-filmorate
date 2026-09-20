package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;

@Slf4j
@Service
public class MpaService {

    private final MpaStorage mpaStorage;

    public MpaService(@Qualifier("mpaDbStorage") MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public List<Mpa> findAll() {
        log.info("Getting all MPA ratings");
        return mpaStorage.findAll();
    }

    public Mpa findById(int id) {
        log.info("Getting MPA rating by id {}", id);
        return mpaStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("MPA rating with id " + id + " not found"));
    }
}
