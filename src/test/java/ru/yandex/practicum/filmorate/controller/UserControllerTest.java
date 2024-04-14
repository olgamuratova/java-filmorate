package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.service.impl.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.service.impl.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserControllerTest {

    private final InMemoryUserStorage inMemoryUserStorage = new InMemoryUserStorage();

    private final InMemoryFilmStorage inMemoryFilmStorage = new InMemoryFilmStorage();

    private final UserService userService = new UserService(inMemoryUserStorage, inMemoryFilmStorage);

    private final UserController userController = new UserController(inMemoryUserStorage, userService);

    private final User user = new User(1, "mail@mail.ru", "mail", "Name",
            LocalDate.of(2000, 1, 1), new HashSet<>());

    private final User updatedUser = new User(1, "yandex@yandex.ru", "yandex", "Name",
            LocalDate.of(2000, 1, 2), new HashSet<>());

    private final User emptyNameUser = new User(2, "mail@yandex.ru", "user", null,
            LocalDate.of(2000, 1, 1), new HashSet<>());

    private final User incorrectEmailUser = new User(2, "mail.ru",
            "login", "Name", LocalDate.of(2001, 5, 5), new HashSet<>());

    private final User emptyEmailUser = new User(1, "", "myLogin", null,
            LocalDate.of(2000, 1, 1), new HashSet<>());

    private final User commonFriend = new User(4, "friend@mail.ru", "loginFr", "NameFr",
            LocalDate.of(2001, 6, 6), new HashSet<>());

    @Test
    void whenAllGood_shouldCreateUser() {
        userController.create(user);
        assertEquals(1, userController.getUsers().size());
    }

    @Test
    void whenUpdate_shouldUpdateUserData() {
        userController.create(user);
        userController.update(updatedUser);
        assertEquals("yandex@yandex.ru", updatedUser.getEmail());
        assertEquals(user.getId(), updatedUser.getId());
        assertEquals(1, userController.getUsers().size());
    }

    @Test
    void whenEmailIsEmpty_shouldThrowException() {
        assertThrows(ValidationException.class, () -> userController.create(emptyEmailUser));
        assertEquals(0, userController.getUsers().size());
    }

    @Test
    void whenEmailIncorrect_shouldThrowException() {
        assertThrows(ValidationException.class, () -> userController.create(incorrectEmailUser));
        assertEquals(0, userController.getUsers().size());
    }

    @Test
    void whenLoginIsEmpty_shouldThrowException() {
        user.setLogin("");
        assertThrows(ValidationException.class, () -> userController.create(user));
        assertEquals(0, userController.getUsers().size());
    }

    @Test
    void whenNameIsEmpty_shouldCreateUser() {
        userController.create(emptyNameUser);
        assertEquals(1, emptyNameUser.getId());
        assertEquals("user", emptyNameUser.getName());
    }

    @Test
    void whenBirthdayIsInTheFuture_shouldReturnError() {
        user.setBirthday(LocalDate.now().plusDays(1L));
        ValidationException validationException = assertThrows(ValidationException.class, () -> userController.create(user));
        assertEquals(validationException.getMessage(), "Дата рождения не может быть в будущем");
    }

    @Test
    void whenDeleteFriend_shouldDeleteFriendFromOtherUsersSet() {
        userController.create(user);
        userController.create(emptyNameUser);
        userController.addFriend(user.getId(), emptyNameUser.getId());
        userController.removeFriend(user.getId(), emptyNameUser.getId());
        assertEquals(0, user.getFriendsQuantity());
        assertEquals(0, emptyNameUser.getFriendsQuantity());
    }

}
