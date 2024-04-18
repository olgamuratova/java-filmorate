package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.db.ReviewStorage;
import ru.yandex.practicum.filmorate.db.impl.FeedDbStorage;
import ru.yandex.practicum.filmorate.db.impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.db.impl.UserDbStorage;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;
    private final FeedDbStorage feedStorage;

    public Review getReviewById(long id) {
        return reviewStorage.getReviewById(id);
    }

    public Review addReview(Review review) {
        filmDbStorage.getById(Math.toIntExact(review.getFilmId()));
        userDbStorage.getById(Math.toIntExact(review.getUserId()));
        Review rev = reviewStorage.addReview(review);
        log.info("Запись события в таблицу аудита");
        feedStorage.addFeed("REVIEW", "ADD", review.getUserId(), review.getReviewId());
        log.info("Информация успешно сохранена");
        return rev;
    }

    public Review updateReview(Review review) {
        long userId = reviewStorage.getReviewById(review.getReviewId()).getUserId();
        Review newRev = reviewStorage.updateReview(review);
        log.info("Запись события в таблицу аудита");
        feedStorage.addFeed("REVIEW", "UPDATE", userId, review.getReviewId());
        log.info("Информация успешно сохранена");
        return newRev;
    }

    public void deleteReviewById(long id) {
        long userId = reviewStorage.getReviewById(id).getUserId();
        reviewStorage.deleteReviewById(id);
        log.info("Запись события в таблицу аудита");
        feedStorage.addFeed("REVIEW", "REMOVE", userId, id);
        log.info("Информация успешно сохранена");
    }

    public void addReviewLike(long reviewId, long userId) {
        getReviewById(reviewId);
        userDbStorage.getById((int) userId);
        reviewStorage.addReviewLike(reviewId, userId);
    }

    public void deleteReviewLike(long reviewId, long userId) {
        getReviewById(reviewId);
        userDbStorage.getById((int) userId);
        reviewStorage.deleteReviewLike(reviewId, userId);
    }

    public void addReviewDislike(long reviewId, long userId) {
        getReviewById(reviewId);
        userDbStorage.getById((int) userId);
        reviewStorage.addReviewDislike(reviewId, userId);
    }

    public void deleteReviewDislike(long reviewId, long userId) {
        getReviewById(reviewId);
        userDbStorage.getById((int) userId);
        reviewStorage.deleteReviewDislike(reviewId, userId);
    }

    public List<Review> getAllReviews() {
        return reviewStorage.getAll()
                .stream()
                .sorted(Comparator.comparingLong(Review::getUseful).reversed())
                .collect(Collectors.toList());
    }

    public List<Review> getReviewsByFilmId(long filmId, int limit) {
        if (filmDbStorage.isContains(Math.toIntExact(filmId))) {
            return reviewStorage.getReviewsByFilmId(filmId)
                    .stream()
                    .sorted(Comparator.comparingLong(Review::getUseful).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());
        }
        throw new ObjectNotFoundException("Фильм не найден");
    }
}
