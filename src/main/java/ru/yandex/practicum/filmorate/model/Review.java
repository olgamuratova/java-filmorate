package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Review {

    private long reviewId;
    @NotBlank
    private String content;
    @NotNull
    private Boolean isPositive;
    @NotNull
    private Long filmId;
    @NotNull
    private Long userId;
    private long useful;
    private List<Long> likes;
    private List<Long> dislikes;

    public Review(long id, String content, Boolean isPositive, Long userId, Long filmId) {
        this.reviewId = id;
        this.content = content;
        this.isPositive = isPositive;
        this.userId = userId;
        this.filmId = filmId;
    }

    public Review(long reviewId, String content, boolean isPositive, long filmId, long userId, int useful) {
    }
}