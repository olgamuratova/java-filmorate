package ru.yandex.practicum.filmorate.db;

import ru.yandex.practicum.filmorate.model.Feed;

import java.util.List;

public interface FeedStorage {
    List<Feed> getFeedById(long id);

    void addFeed(String type, String operation, long entityId, long userId);
}
