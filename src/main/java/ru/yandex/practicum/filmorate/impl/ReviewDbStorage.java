package ru.yandex.practicum.filmorate.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.ReviewStorage;
import ru.yandex.practicum.filmorate.exception.InternalServiceException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private static final String SQL_GET_ALL_REVIEWS = "SELECT * FROM reviews;";

    private static final String SQL_GET_REVIEW_BY_ID = "SELECT * FROM reviews WHERE review_id = ?;";

    private static final String SQL_ADD_REVIEW = "INSERT INTO reviews (content, is_positive, user_id, film_id) " +
            "VALUES (?, ?, ?, ?);";

    private static final String SQL_UPDATE_REVIEW = "UPDATE reviews SET content = ?, is_positive = ? " +
            "WHERE review_id = ?;";

    private static final String SQL_DELETE_REVIEW_BY_ID = "DELETE FROM reviews WHERE review_id = ?;";

    private static final String SQL_ADD_REVIEW_LIKE = "INSERT INTO review_likes (review_id, user_id) VALUES (?, ?);";

    private static final String SQL_REMOVE_REVIEW_LIKE = "DELETE FROM review_likes WHERE review_id = ? " +
            "AND user_id = ?;";

    private static final String SQL_ADD_REVIEW_DISLIKE = "INSERT INTO review_dislikes (review_id, user_id) " +
            "VALUES (?, ?);";

    private static final String SQL_REMOVE_REVIEW_DISLIKE = "DELETE FROM review_dislikes WHERE review_dislike_id = ? " +
            "AND user_id = ?;";

    private static final String SQL_GET_REVIEW_BY_FILM_ID = "SELECT * FROM reviews WHERE film_id = ?;";

    private static final String SQL_REVIEW_LIKES_FROM_TABLE = "SELECT user_id FROM review_likes WHERE review_id = ?;";

    private static final String SQL_REVIEW_DISLIKES_FROM_TABLE = "SELECT user_id FROM review_dislikes " +
            "WHERE review_id = ?;";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Review> getAll() {
        return jdbcTemplate.query(SQL_GET_ALL_REVIEWS, this::makeReview);
    }

    @Override
    public Review getReviewById(long id) throws InternalServiceException {
        return jdbcTemplate.query(SQL_GET_REVIEW_BY_ID, this::makeReview, id)
                .stream()
                .findAny()
                .orElseThrow(() -> new InternalServiceException("Отзыв с id " + id + " не найден."));
    }

    @Override
    public Review addReview(Review review) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement statement = con.prepareStatement(SQL_ADD_REVIEW, new String[]{"review_id"});
            statement.setString(1, review.getContent());
            statement.setBoolean(2, review.getIsPositive());
            statement.setLong(3, review.getUserId());
            statement.setLong(4, review.getFilmId());
            return statement;
        }, keyHolder);

        review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        log.trace("Отзыв сохранен: {}", review);
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        jdbcTemplate.update(
                SQL_UPDATE_REVIEW,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());
        log.trace("Отзыв обновлен: {}", review);
        return getReviewById(review.getReviewId());
    }

    @Override
    public void deleteReviewById(long id) {
        Review review = getReviewById(id);
        jdbcTemplate.update(SQL_DELETE_REVIEW_BY_ID, id);
        log.trace("Отзыв удален: {}", review);
    }

    @Override
    public void addReviewLike(long reviewId, long userId) {
        if (getReviewDisLikesFromTable(reviewId).contains(userId)) {
            deleteReviewDislike(reviewId, userId);
        }
        jdbcTemplate.update(SQL_ADD_REVIEW_LIKE, reviewId, userId);
    }

    @Override
    public void deleteReviewLike(long reviewId, long userId) {
        jdbcTemplate.update(SQL_REMOVE_REVIEW_LIKE, reviewId, userId);
    }

    @Override
    public void addReviewDislike(long reviewId, long userId) {
        if (getReviewLikesFromTable(reviewId).contains(userId)) {
            deleteReviewLike(reviewId, userId);
        }
        jdbcTemplate.update(SQL_ADD_REVIEW_DISLIKE, reviewId, userId);
    }

    @Override
    public void deleteReviewDislike(long reviewId, long userId) {
        jdbcTemplate.update(SQL_REMOVE_REVIEW_DISLIKE, reviewId, userId);
    }

    @Override
    public List<Review> getReviewsByFilmId(long filmId) {
        return jdbcTemplate.query(SQL_GET_REVIEW_BY_FILM_ID, this::makeReview, filmId);
    }

    private Review makeReview(ResultSet rs, int rowNum) throws SQLException {
        Review review = new Review(
                rs.getLong("review_id"),
                rs.getString("content"),
                rs.getBoolean("is_positive"),
                rs.getLong("user_id"),
                rs.getLong("film_id"));
        review.setLikes(getReviewLikesFromTable(review.getReviewId()));
        review.setDislikes(getReviewDisLikesFromTable(review.getReviewId()));
        review.setUseful(usefulCalculate(review));
        return review;
    }

    private long usefulCalculate(Review review) {
        long useful = 0;
        useful += review.getLikes().size();
        useful -= review.getDislikes().size();
        return useful;
    }

    private List<Long> getReviewLikesFromTable(Long reviewId) {
        return jdbcTemplate.query(SQL_REVIEW_LIKES_FROM_TABLE, this::getUserIdFRomTableLikeReview, reviewId);
    }

    private long getUserIdFRomTableLikeReview(ResultSet rs, int rowNum) throws SQLException {
        return rs.getLong("user_id");
    }

    private List<Long> getReviewDisLikesFromTable(Long reviewId) {
        return jdbcTemplate.query(SQL_REVIEW_DISLIKES_FROM_TABLE, this::getUserIdFRomTableDislikeReview, reviewId);
    }

    private long getUserIdFRomTableDislikeReview(ResultSet rs, int rowNum) throws SQLException {
        return rs.getLong("user_id");
    }

}