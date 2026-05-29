package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        normalizeUser(user);
        User created = userStorage.create(user);
        log.info("User created: {}", created);
        return created;
    }

    public User update(User user) {
        if (user.getId() <= 0) {
            throw new ValidationException("User id is required");
        }
        normalizeUser(user);
        User updated = userStorage.update(user);
        log.info("User updated: {}", updated);
        return updated;
    }

    private void normalizeUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
