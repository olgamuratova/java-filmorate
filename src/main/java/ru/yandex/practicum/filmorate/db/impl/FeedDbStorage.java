package ru.yandex.practicum.filmorate.db.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.db.FeedStorage;
import ru.yandex.practicum.filmorate.model.Feed;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeedDbStorage implements FeedStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Feed> getFeedById(int id) {
        String sqlQuery = "SELECT * FROM feed WHERE USER_ID = ? ";
        log.info("Запрос формируется по id - {}", id);
        return jdbcTemplate.query(sqlQuery, this::feedRowToFilm, id);
    }

    @Override
    public void addFeed(String type, String operation, int userId, int entityId) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FEED")
                .usingGeneratedKeyColumns("EVENT_ID");
        Feed feed = Feed.builder()
                .timestamp(Timestamp.valueOf(LocalDateTime.now()).getTime())
                .userId(userId)
                .eventType(type)
                .operation(operation)
                .entityId(entityId)
                .build();
        insert.execute(feedToMap(feed));
    }

    public Map<String, Object> feedToMap(Feed feed) {
        return Map.of(
                "EVENT_ID", feed.getEventId(),
                "USER_ID", feed.getUserId(),
                "ENTITY_ID", feed.getEntityId(),
                "EVENT_TYPE", feed.getEventType(),
                "OPERATION", feed.getOperation(),
                "TIMESTAMP", feed.getTimestamp()
        );
    }

    private Feed feedRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        return Feed.builder()
                .eventId(resultSet.getInt("EVENT_ID"))
                .userId(resultSet.getInt("USER_ID"))
                .entityId(resultSet.getInt("ENTITY_ID"))
                .eventType(resultSet.getString("EVENT_TYPE"))
                .operation(resultSet.getString("OPERATION"))
                .timestamp(resultSet.getLong("TIMESTAMP"))
                .build();
    }
}
