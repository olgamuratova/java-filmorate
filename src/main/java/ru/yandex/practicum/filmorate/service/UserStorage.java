package ru.yandex.practicum.filmorate.service;

import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

public interface UserStorage {

    User create(@Valid @RequestBody User user);

    User update(@Valid @RequestBody User user);

    List<User> getUsers();

    Map<Integer, User> getUsersMap();

    User getById(Integer id);
}
