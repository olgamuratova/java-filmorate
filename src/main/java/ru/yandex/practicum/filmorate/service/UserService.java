package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public void addFriend(Integer userId, Integer friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    public void deleteFriend(Integer userId, Integer friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    public List<User> getCommonFriend(Integer userId, Integer friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);
        Set<Integer> set1 = user.getFriends();
        Set<Integer> set2 = friend.getFriends();
        return set1.stream()
                .filter(set2::contains)
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    public List<User> getFriends(Integer userId) {
        User user = userStorage.getById(userId);
        Set<Integer> friends = user.getFriends();
        return friends.stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    public UserStorage getUsersStorage() {
        return userStorage;
    }
}
