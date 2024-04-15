package ru.yandex.practicum.filmorate.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.UserStorage;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public User create(User user) {
        validateUser(user);
        String query = "INSERT INTO users(email, login, name, birthday)" + "VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(query, user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()));
        String selectQuery = "SELECT * from users where email = ?";
        SqlRowSet result = jdbcTemplate.queryForRowSet(selectQuery, user.getEmail());
        if (result.next()) {
            user = new User(
                    result.getInt("user_id"),
                    result.getString("email"),
                    result.getString("login"),
                    result.getString("name"),
                    result.getDate("birthday").toLocalDate()
            );
        }
        return user;
    }

    @Override
    public User update(User user) {
        getById(user.getId());
        String query = "UPDATE users set email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        jdbcTemplate.update(query, user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId());
        return user;
    }

    @Override
    public List<User> getUsers() {
        String query = "SELECT * from users";
        return jdbcTemplate.query(query, new UserMapper());
    }

    @Override
    public User getById(Integer id) {
        String query = "SELECT * from users where user_id = ?";
        SqlRowSet result = jdbcTemplate.queryForRowSet(query, id);
        if (result.next()) {
            User user = new User(
                    result.getInt("user_id"),
                    result.getString("email"),
                    result.getString("login"),
                    result.getString("name"),
                    result.getDate("birthday").toLocalDate()
            );
            return user;
        }
        throw new ObjectNotFoundException("Пользователь с id не найден");
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        getById(userId);
        getById(friendId);
        String query = "INSERT INTO friends(user_id, friend_id, is_friend) VALUES (?, ?, ?)";
        jdbcTemplate.update(query, userId, friendId, false);
    }

    @Override
    public void deleteFriend(Integer userId, Integer friendId) {
        getById(userId);
        getById(friendId);
        String query = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(query, userId, friendId);
    }

    @Override
    public Set<Integer> getFriends(Integer userId) {
        getById(userId);
        String query = "SELECT friend_id FROM friends WHERE user_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(query, Integer.class, userId));
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