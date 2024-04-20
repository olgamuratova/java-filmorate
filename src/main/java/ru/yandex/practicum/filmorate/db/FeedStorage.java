package ru.yandex.practicum.filmorate.db;

import ru.yandex.practicum.filmorate.model.Feed;

import java.util.List;

public interface FeedStorage {

    List<Feed> getFeedById(int id);

    void addFeed(String type, String operation, int entityId, int userId);
}
