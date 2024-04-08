package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Film.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Film {

    private Integer id;

    private Mpa mpa;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    private String description;

    private LocalDate releaseDate;

    @Min(value = 0, message = "Продолжительность фильма должна быть положительной")
    private int duration;

    private List<Genre> genres;

    private Set<Integer> likes = new HashSet<>();

    public Integer getLikesQuantity() {
        return likes.size();
    }

    public Film(Integer id, String name, String description, LocalDate releaseDate, int duration, Set<Integer> likes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.likes = likes;
    }

    public Film(Integer id, Mpa mpa, String name, String description, LocalDate releaseDate, int duration) {
        this.id = id;
        this.mpa = mpa;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }

    public Film(Mpa mpa, String name, String description, LocalDate releaseDate, int duration, List<Genre> genres) {
        this.mpa = mpa;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.genres = genres;
    }
}
