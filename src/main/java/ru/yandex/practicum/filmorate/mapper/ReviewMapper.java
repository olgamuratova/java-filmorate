package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.model.Review;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReviewMapper implements RowMapper<Review> {
    @Override
    public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Review(rs.getLong("review_id"),
                rs.getLong("film_id"),
                rs.getLong("user_id"),
                rs.getBoolean("is_positive"),
                rs.getString("content"),
                rs.getInt("useful"));
    }
}
