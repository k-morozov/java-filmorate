package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> findAll() {
        log.info("Getting all users");
        return userStorage.findAll();
    }

    public User findById(long id) {
        log.info("Getting user by id {}", id);
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
    }

    public User create(User user) {
        log.info("Creating user");
        normalizeUser(user);
        User created = userStorage.create(user);
        log.info("User created: {}", created);
        return created;
    }

    public User update(User user) {
        log.info("Updating user with id {}", user.getId());
        if (user.getId() <= 0) {
            throw new ValidationException("User id is required");
        }
        userStorage.findById(user.getId())
                .orElseThrow(() -> new NotFoundException("User with id " + user.getId() + " not found"));
        normalizeUser(user);
        User updated = userStorage.update(user);
        log.info("User updated: {}", updated);
        return updated;
    }

    public void addFriend(long userId, long friendId) {
        log.info("User {} adding friend {}", userId, friendId);
        if (userId == friendId) {
            throw new ValidationException("User cannot add themselves as a friend");
        }
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("User with id " + friendId + " not found"));
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        userStorage.update(user);
        userStorage.update(friend);
        log.info("User {} and user {} are now friends", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        log.info("User {} removing friend {}", userId, friendId);
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("User with id " + friendId + " not found"));
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        userStorage.update(user);
        userStorage.update(friend);
        log.info("User {} and user {} are no longer friends", userId, friendId);
    }

    public List<User> getFriends(long userId) {
        log.info("Getting friends of user {}", userId);
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
        return user.getFriends().stream()
                .map(id -> userStorage.findById(id)
                        .orElseThrow(() -> new NotFoundException("User with id " + id + " not found")))
                .toList();
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        log.info("Getting common friends of users {} and {}", userId, otherId);
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
        User other = userStorage.findById(otherId)
                .orElseThrow(() -> new NotFoundException("User with id " + otherId + " not found"));
        return user.getFriends().stream()
                .filter(id -> other.getFriends().contains(id))
                .map(id -> userStorage.findById(id)
                        .orElseThrow(() -> new NotFoundException("User with id " + id + " not found")))
                .toList();
    }

    private void normalizeUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
