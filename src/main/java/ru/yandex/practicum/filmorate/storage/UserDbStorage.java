package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component("userDbStorage")
public class UserDbStorage implements UserStorage {

    private static final String USERS_TABLE = "users";
    private static final String ID_COLUMN = "id";

    private static final String SELECT_USERS = "SELECT id, email, login, name, birthday FROM users";
    private static final String SELECT_ALL_USERS = SELECT_USERS + " ORDER BY id";
    private static final String SELECT_USER_BY_ID = SELECT_USERS + " WHERE id = ?";
    private static final String SELECT_FRIENDS = SELECT_USERS
            + " WHERE id IN (SELECT friend_id FROM friendships WHERE user_id = ?) ORDER BY id";
    private static final String SELECT_COMMON_FRIENDS = SELECT_USERS
            + " WHERE id IN (SELECT f.friend_id FROM friendships AS f "
            + "JOIN friendships AS o ON f.friend_id = o.friend_id "
            + "WHERE f.user_id = ? AND o.user_id = ?) ORDER BY id";
    private static final String UPDATE_USER =
            "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    private static final String INSERT_FRIENDSHIP = "MERGE INTO friendships (user_id, friend_id, confirmed) "
            + "KEY (user_id, friend_id) VALUES (?, ?, FALSE)";
    private static final String CONFIRM_FRIENDSHIP = "UPDATE friendships SET confirmed = TRUE "
            + "WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
    private static final String DELETE_FRIENDSHIP = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
    private static final String RESET_CONFIRMATION =
            "UPDATE friendships SET confirmed = FALSE WHERE user_id = ? AND friend_id = ?";
    private static final String COUNT_FRIENDSHIP =
            "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?";
    private static final String SELECT_FRIEND_IDS = "SELECT friend_id FROM friendships WHERE user_id = ?";
    private static final String SELECT_ALL_FRIENDSHIPS = "SELECT user_id, friend_id FROM friendships";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert userInsert;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.userInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(USERS_TABLE)
                .usingGeneratedKeyColumns(ID_COLUMN);
    }

    @Override
    public Collection<User> findAll() {
        List<User> users = jdbcTemplate.query(SELECT_ALL_USERS, userMapper());
        loadFriends(users);
        return users;
    }

    @Override
    public User create(User user) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", user.getEmail());
        parameters.put("login", user.getLogin());
        parameters.put("name", user.getName());
        parameters.put("birthday", Date.valueOf(user.getBirthday()));
        long id = userInsert.executeAndReturnKey(parameters).longValue();
        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) {
        jdbcTemplate.update(UPDATE_USER, user.getEmail(), user.getLogin(), user.getName(),
                Date.valueOf(user.getBirthday()), user.getId());
        user.setFriends(readFriendIds(user.getId()));
        return user;
    }

    @Override
    public Optional<User> findById(long id) {
        try {
            User user = jdbcTemplate.queryForObject(SELECT_USER_BY_ID, userMapper(), id);
            if (user != null) {
                user.setFriends(readFriendIds(id));
            }
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void addFriend(long userId, long friendId) {
        jdbcTemplate.update(INSERT_FRIENDSHIP, userId, friendId);
        if (isFriend(friendId, userId)) {
            jdbcTemplate.update(CONFIRM_FRIENDSHIP, userId, friendId, friendId, userId);
        }
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        jdbcTemplate.update(DELETE_FRIENDSHIP, userId, friendId);
        jdbcTemplate.update(RESET_CONFIRMATION, friendId, userId);
    }

    @Override
    public List<User> getFriends(long userId) {
        List<User> friends = jdbcTemplate.query(SELECT_FRIENDS, userMapper(), userId);
        loadFriends(friends);
        return friends;
    }

    @Override
    public List<User> getCommonFriends(long userId, long otherId) {
        List<User> friends = jdbcTemplate.query(SELECT_COMMON_FRIENDS, userMapper(), userId, otherId);
        loadFriends(friends);
        return friends;
    }

    private boolean isFriend(long userId, long friendId) {
        Integer count = jdbcTemplate.queryForObject(COUNT_FRIENDSHIP, Integer.class, userId, friendId);
        return count != null && count > 0;
    }

    private Set<Long> readFriendIds(long userId) {
        return new HashSet<>(jdbcTemplate.queryForList(SELECT_FRIEND_IDS, Long.class, userId));
    }

    private void loadFriends(List<User> users) {
        if (users.isEmpty()) {
            return;
        }
        Map<Long, Set<Long>> friendsByUser = new HashMap<>();
        jdbcTemplate.query(SELECT_ALL_FRIENDSHIPS, rs -> {
            friendsByUser.computeIfAbsent(rs.getLong("user_id"), key -> new HashSet<>())
                    .add(rs.getLong("friend_id"));
        });
        for (User user : users) {
            user.setFriends(friendsByUser.getOrDefault(user.getId(), new HashSet<>()));
        }
    }

    private RowMapper<User> userMapper() {
        return (ResultSet rs, int rowNum) -> {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        };
    }
}
