package ru.yandex.practicum.filmorate.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.db.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();

    private int id = 1;

    @Override
    public User create(User user) {
        validateUser(user);
        user.setId(id++);
        users.put(user.getId(), user);
        log.info("Создание пользователя {} с идентификатором {}", user.getEmail(), user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new ObjectNotFoundException("Неизвестный пользователь");
        }
        validateUser(user);
        users.put(user.getId(), user);
        log.info("Обновление пользователя {} с идентификатором {}", user.getLogin(), user.getId());
        return user;
    }

    @Override
    public List<User> getUsers() {
        log.info("Текущее количество пользователей: {}", users.size());
        return new ArrayList<>(users.values());
    }

    @Override
    public User getById(Integer id) {
        User user = users.get(id);
        if (user == null) {
            throw new ObjectNotFoundException("Пользователь с id не найден");
        }
        return user;
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {

    }

    @Override
    public void deleteFriend(Integer userId, Integer friendId) {

    }

    @Override
    public Set<Integer> getFriends(Integer userId) {
        return null;
    }

    @Override
    public void deleteUser(Integer id) {

    }

    private void validateUser(User user) {
        if (user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
