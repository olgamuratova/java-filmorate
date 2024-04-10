package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.FilmStorage;
import ru.yandex.practicum.filmorate.UserStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserStorage userStorage;

    private final FilmStorage filmStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage, FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public void addFriend(Integer userId, Integer friendId) {
        if (checkUserExist(userId) && checkUserExist(friendId)) {
            userStorage.addFriend(userId, friendId);
        }
    }

    public void deleteFriend(Integer userId, Integer friendId) {
        userStorage.deleteFriend(userId, friendId);
    }

    public List<User> getCommonFriend(Integer userId, Integer friendId) {
        Set<Integer> set1 = userStorage.getFriends(userId);
        Set<Integer> set2 = userStorage.getFriends(friendId);
        return set1.stream()
                .filter(set2::contains)
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    public List<User> getFriends(Integer userId) {
        Set<Integer> friends = userStorage.getFriends(userId);
        return friends.stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    public UserStorage getUsersStorage() {
        return userStorage;
    }

    public List<Film> getFilmRecommendations(Integer id){
//        User user = userStorage.getById(id);////
        return filmStorage.getRecommendedFilms(id);

    }

    private boolean checkUserExist(Integer userId) {
        return userStorage.getById(userId) != null;
    }
}
