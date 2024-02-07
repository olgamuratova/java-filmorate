package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * Film.
 */
@Data
@Builder
public class Film {

    private int id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    private String description;

    private LocalDate releaseDate;

    @Min(value = 0, message = "Продолжительность фильма должна быть положительной")
    private int duration;
}
