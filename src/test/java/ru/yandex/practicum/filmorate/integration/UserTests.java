package ru.yandex.practicum.filmorate.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.impl.UserDbStorage;
import ru.yandex.practicum.filmorate.FilmorateApplication;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FilmorateApplication.class)
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserTests {

    final UserDbStorage userStorage;

    @BeforeEach
    void createdUserForDB() {
        if (userStorage.getUsers().size() != 2) {
            User firstTestUser = new User("test1@yandex.ru", "User1", "Tester1", LocalDate.parse("1999-01-01"));
            userStorage.create(firstTestUser);
            User secondTestUser = new User("test2@yandex.ru", "User2", "Tester2", LocalDate.parse("2000-01-01"));
            userStorage.create(secondTestUser);
        }
        userStorage.deleteFriend(1, 2);
    }

    @Test
    void testCreatedUser() {
        checkFindUserById(1);
        checkFindUserById(2);
    }

    @Test
    void testFindAll() {
        List<User> currentList = userStorage.getUsers();
        assertEquals(2, currentList.size(), "Некорректное количество пользователей");
    }

    @Test
    void testUpgradeUser() {
        User updateUser = new User("updateUser@yandex.ru",
                "updateUser",
                "UpdateName",
                LocalDate.parse("2000-10-10"));
        updateUser.setId(1);
        userStorage.update(updateUser);
        Optional<User> userStorageUser = Optional.ofNullable(userStorage.getById(1));
        Map<String, Object> mapForCheck = new HashMap<>();
        mapForCheck.put("id", updateUser.getId());
        mapForCheck.put("email", updateUser.getEmail());
        mapForCheck.put("login", updateUser.getLogin());
        mapForCheck.put("name", updateUser.getName());
        mapForCheck.put("birthday", updateUser.getBirthday());
        for (Map.Entry<String, Object> entry : mapForCheck.entrySet()) {
            assertThat(userStorageUser)
                    .isPresent()
                    .hasValueSatisfying(user ->
                            assertThat(user).hasFieldOrPropertyWithValue(entry.getKey(), entry.getValue())
                    );
        }
    }

    @Test
    void testFindUserById() {
        checkFindUserById(1);
    }

    @Test
    void testFindAllFriends() {
        userStorage.addFriend(1, 2);
        Set<Integer> listFriendIdOne = userStorage.getFriends(1);
        assertEquals(1, listFriendIdOne.size(), "В списке друзей должен быть 1 друг");
        assertTrue(listFriendIdOne.contains(2), "Значение ID друга должно равнятся 2");
        Set<Integer> listFriendIdTwo = userStorage.getFriends(2);
        assertEquals(0, listFriendIdTwo.size(), "Список друзей должен быть пуст");
    }

    void checkFindUserById(Integer idUser) {
        Optional<User> userStorageById = Optional.ofNullable(userStorage.getById(idUser));
        assertThat(userStorageById)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", idUser)
                );
    }
}
