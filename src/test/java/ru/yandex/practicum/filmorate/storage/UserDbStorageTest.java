package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    private User newUser(String login) {
        User user = new User();
        user.setEmail(login + "@example.com");
        user.setLogin(login);
        user.setName("Name of " + login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void createAssignsIdAndFindByIdReturnsUser() {
        User created = userStorage.create(newUser("first"));

        assertThat(created.getId()).isPositive();

        Optional<User> found = userStorage.findById(created.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("id", created.getId());
                    assertThat(user).hasFieldOrPropertyWithValue("login", "first");
                    assertThat(user).hasFieldOrPropertyWithValue("email", "first@example.com");
                    assertThat(user.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
                });
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertThat(userStorage.findById(9999)).isEmpty();
    }

    @Test
    void findAllReturnsAllUsers() {
        userStorage.create(newUser("first"));
        userStorage.create(newUser("second"));

        Collection<User> users = userStorage.findAll();

        assertThat(users).hasSize(2)
                .extracting(User::getLogin)
                .containsExactly("first", "second");
    }

    @Test
    void updateChangesStoredUser() {
        User created = userStorage.create(newUser("first"));
        created.setName("Updated");
        created.setEmail("updated@example.com");

        userStorage.update(created);

        assertThat(userStorage.findById(created.getId()))
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("name", "Updated");
                    assertThat(user).hasFieldOrPropertyWithValue("email", "updated@example.com");
                });
    }

    @Test
    void addFriendIsOneDirectional() {
        User user = userStorage.create(newUser("first"));
        User friend = userStorage.create(newUser("second"));

        userStorage.addFriend(user.getId(), friend.getId());

        assertThat(userStorage.getFriends(user.getId()))
                .extracting(User::getId)
                .containsExactly(friend.getId());
        assertThat(userStorage.getFriends(friend.getId())).isEmpty();
    }

    @Test
    void addFriendTwiceKeepsSingleFriendship() {
        User user = userStorage.create(newUser("first"));
        User friend = userStorage.create(newUser("second"));

        userStorage.addFriend(user.getId(), friend.getId());
        userStorage.addFriend(user.getId(), friend.getId());

        assertThat(userStorage.getFriends(user.getId())).hasSize(1);
    }

    @Test
    void removeFriendDeletesOnlyOneDirection() {
        User user = userStorage.create(newUser("first"));
        User friend = userStorage.create(newUser("second"));
        userStorage.addFriend(user.getId(), friend.getId());
        userStorage.addFriend(friend.getId(), user.getId());

        userStorage.removeFriend(user.getId(), friend.getId());

        assertThat(userStorage.getFriends(user.getId())).isEmpty();
        assertThat(userStorage.getFriends(friend.getId()))
                .extracting(User::getId)
                .containsExactly(user.getId());
    }

    @Test
    void removeFriendDoesNothingWhenUsersAreNotFriends() {
        User user = userStorage.create(newUser("first"));
        User other = userStorage.create(newUser("second"));

        userStorage.removeFriend(user.getId(), other.getId());

        assertThat(userStorage.getFriends(user.getId())).isEmpty();
        assertThat(userStorage.getFriends(other.getId())).isEmpty();
    }

    @Test
    void getFriendsReturnsEmptyListForUserWithoutFriends() {
        User user = userStorage.create(newUser("first"));

        assertThat(userStorage.getFriends(user.getId())).isEmpty();
    }

    @Test
    void getCommonFriendsReturnsSharedFriendsOnly() {
        User user = userStorage.create(newUser("first"));
        User other = userStorage.create(newUser("second"));
        User common = userStorage.create(newUser("common"));
        User personal = userStorage.create(newUser("personal"));

        userStorage.addFriend(user.getId(), common.getId());
        userStorage.addFriend(user.getId(), personal.getId());
        userStorage.addFriend(other.getId(), common.getId());

        List<User> commonFriends = userStorage.getCommonFriends(user.getId(), other.getId());

        assertThat(commonFriends)
                .extracting(User::getId)
                .containsExactly(common.getId());
    }

    @Test
    void findByIdLoadsFriendIds() {
        User user = userStorage.create(newUser("first"));
        User friend = userStorage.create(newUser("second"));
        userStorage.addFriend(user.getId(), friend.getId());

        assertThat(userStorage.findById(user.getId()))
                .isPresent()
                .hasValueSatisfying(found -> assertThat(found.getFriends()).containsExactly(friend.getId()));
    }
}
