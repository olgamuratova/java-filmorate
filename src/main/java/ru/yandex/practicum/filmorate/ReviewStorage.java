package ru.yandex.practicum.filmorate;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {

    Review addReview(Review review);

    Review updateReview(Review review);

    Review getReviewById(long id);

    List<Review> getAll();

    void deleteReviewById(long id);

    void addReviewLike(long reviewId, long userId);

    void deleteReviewLike(long reviewId, long userId);

    void addReviewDislike(long reviewId, long userId);

    void deleteReviewDislike(long reviewId, long userId);

    List<Review> getReviewsByFilmId(long filmId);
}
