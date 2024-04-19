package ru.yandex.practicum.filmorate.db;

import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

public interface UserStorage {

    User create(@Valid @RequestBody User user);

    User update(@Valid @RequestBody User user);

    List<User> getUsers();

    User getById(Integer id);

    void addFriend(Integer userId, Integer friendId);

    void deleteFriend(Integer userId, Integer friendId);

    Set<Integer> getFriends(Integer userId);

    void deleteUser(Integer id);
}
