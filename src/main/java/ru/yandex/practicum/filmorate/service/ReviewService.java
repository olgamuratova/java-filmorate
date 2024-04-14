package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.ReviewStorage;
import ru.yandex.practicum.filmorate.impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.impl.UserDbStorage;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final FilmDbStorage inMemoryFilmStorage;
    private final UserDbStorage inMemoryUserStorage;

    public Review getReviewById(long id) {
        return reviewStorage.getReviewById(id);
    }

    public Review addReview(Review review) {
        inMemoryFilmStorage.getById(Math.toIntExact(review.getFilmId()));
        inMemoryUserStorage.getById(Math.toIntExact(review.getUserId()));
        return reviewStorage.addReview(review);
    }

    public Review updateReview(Review review) {
        getReviewById(review.getReviewId());
        return reviewStorage.updateReview(review);
    }

    public void deleteReviewById(long id) {
        reviewStorage.deleteReviewById(id);
    }

    public void addReviewLike(long reviewId, long userId) {
        getReviewById(reviewId);
        inMemoryUserStorage.getById((int) userId);
        reviewStorage.addReviewLike(reviewId, userId);
    }

    public void deleteReviewLike(long reviewId, long userId) {
        getReviewById(reviewId);
        inMemoryUserStorage.getById((int) userId);
        reviewStorage.deleteReviewLike(reviewId, userId);
    }

    public void addReviewDislike(long reviewId, long userId) {
        getReviewById(reviewId);
        inMemoryUserStorage.getById((int) userId);
        reviewStorage.addReviewDislike(reviewId, userId);
    }

    public void deleteReviewDislike(long reviewId, long userId) {
        getReviewById(reviewId);
        inMemoryUserStorage.getById((int) userId);
        reviewStorage.deleteReviewDislike(reviewId, userId);
    }

    public List<Review> getAllReviews() {
        return reviewStorage.getAll()
                .stream()
                .sorted(Comparator.comparingLong(Review::getUseful).reversed())
                .collect(Collectors.toList());
    }

    public List<Review> getReviewsByFilmId(long filmId, int limit) {
        return reviewStorage.getReviewsByFilmId(filmId)
                .stream()
                .sorted(Comparator.comparingLong(Review::getUseful).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
