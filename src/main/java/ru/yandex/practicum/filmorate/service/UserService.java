package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.db.FeedStorage;
import ru.yandex.practicum.filmorate.db.FilmStorage;
import ru.yandex.practicum.filmorate.db.UserStorage;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    private final FilmStorage filmStorage;

    private final FeedStorage feedStorage;


    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage, @Qualifier("filmDbStorage") FilmStorage filmStorage, FeedStorage feedStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.feedStorage = feedStorage;
    }

    public void addFriend(Integer userId, Integer friendId) {
        if (checkUserExist(userId) && checkUserExist(friendId)) {
            userStorage.addFriend(userId, friendId);
            log.info("Запись события в таблицу аудита");
            feedStorage.addFeed("FRIEND", "ADD", userId, friendId);
            log.info("Информация успешно сохранена");
        }
    }

    public void deleteFriend(Integer userId, Integer friendId) {
        userStorage.deleteFriend(userId, friendId);
        log.info("Запись события в таблицу аудита");
        feedStorage.addFeed("FRIEND", "REMOVE", userId, friendId);
        log.info("Информация успешно сохранена");
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

    public List<Film> getFilmRecommendations(Integer id) {
        return filmStorage.getRecommendedFilms(id);
    }

    public List<Feed> getFeed(int userId) {
        log.info("Выполнение метода getFeed. Проверка на существование пользователя id = {}", userId);
        userStorage.getById((int) userId);
        return feedStorage.getFeedById(userId);
    }

    private boolean checkUserExist(Integer userId) {
        return userStorage.getById(userId) != null;
    }
}
