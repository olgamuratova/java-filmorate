package ru.yandex.practicum.filmorate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@SpringBootTest
public class UserControllerTest {

    private final UserController userController = new UserController();
    private final User user = User.builder()
            .id(1)
            .email("name@gmail.com")
            .login("user")
            .name("User")
            .birthday(LocalDate.of(2002, 1, 1))
            .build();

    @Test
    void whenAllGood_shouldCreateUser() {
        User newUser = User.builder()
                .id(1)
                .email("name@gmail.com")
                .login("user")
                .name("User")
                .birthday(LocalDate.of(2002, 1, 1))
                .build();

        userController.create(newUser);

        assertEquals(newUser, user);
        assertEquals(1, userController.getUsers().size());
    }

    @Test
    void whenUpdate_shouldUpdateUserData() {
        User newUser = User.builder()
                .email("name@mail.com")
                .login("user")
                .name("User")
                .birthday(LocalDate.of(2000, 1, 21))
                .build();

        User createdUser = userController.create(newUser);

        User updateUser = User.builder()
                .id(createdUser.getId())
                .email("email@mail.com")
                .login("user")
                .name("User")
                .birthday(LocalDate.of(2000, 1, 21))
                .build();

        User updatedUser = userController.update(updateUser);

        assertEquals("email@mail.com", updatedUser.getEmail());
        assertEquals(createdUser.getId(), updatedUser.getId());
        assertEquals(1, userController.getUsers().size());
    }

    @Test
    void whenEmailIsEmpty_shouldThrowException() {
        user.setEmail("");
        assertThrows(ValidationException.class, () -> userController.create(user));
        assertEquals(0, userController.getUsers().size());
    }

    @Test
    void whenEmailIncorrect_shouldThrowException() {
        user.setEmail("name.mail.com");
        assertThrows(ValidationException.class, () -> userController.create(user));
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

        User newUser = User.builder()
                .email("name@mail.com")
                .login("user")
                .name(null)
                .birthday(LocalDate.of(2002, 1, 21))
                .build();
        User createdUser = userController.create(newUser);

        assertEquals("user", createdUser.getName());
    }

    @Test
    void whenBirthdayIsInTheFuture_shouldReturnError() {
        user.setBirthday(LocalDate.now().plusDays(1L));
        ValidationException validationException = assertThrows(ValidationException.class, () -> userController.create(user));
        assertEquals(validationException.getMessage(), "Дата рождения не может быть в будущем");
    }
}
