package ru.yandex.practicum.filmorate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

@SpringBootTest
public class FilmControllerTest {

    private final FilmController filmController = new FilmController();
    private final Film film = Film.builder()
            .id(1)
            .name("Фильм")
            .description("Описание")
            .releaseDate(LocalDate.of(2023, 02, 14))
            .duration(95)
            .build();

    @Test
    void whenAllGood_shouldAddFilm() {
        Film newFilm = Film.builder()
                .id(1)
                .name("Фильм")
                .description("Описание")
                .releaseDate(LocalDate.of(2023, 02, 14))
                .duration(95)
                .build();

        filmController.create(newFilm);

        assertEquals(newFilm, film);
        assertEquals(1, filmController.getFilms().size());
    }

    @Test
    void whenUpdate_shouldUpdateFilmData() {
        Film newFilm = Film.builder()
                .name("Фильм")
                .description("Описание")
                .releaseDate(LocalDate.of(2023, 02, 14))
                .duration(95)
                .build();

        Film addedFilm = filmController.create(newFilm);

        Film updateFilm = Film.builder()
                .id(addedFilm.getId())
                .name("Фильм1")
                .description("Описание1")
                .releaseDate(LocalDate.of(2022, 2, 1))
                .duration(95)
                .build();

        Film updatedFilm = filmController.update(updateFilm);

        assertEquals("Описание1", updatedFilm.getDescription());
        assertEquals(addedFilm.getId(), updatedFilm.getId());
        assertEquals(1, filmController.getFilms().size());
    }

    @Test
    void whenMovieWithAnEmptyName_shouldNotAdded() {
        film.setName("");

        assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals(0, filmController.getFilms().size());
    }

    @Test
    void whenMovieWithDescriptionMoreThan200_shouldNotAdded() {
        film.setDescription("yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy" +
                "yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy" +
                "yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy");

        assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals(0, filmController.getFilms().size());
    }

    @Test
    void whenMovieWithDateReleaseMoreThan1895_shouldNotAdded() {
        film.setReleaseDate(LocalDate.of(1890, 1, 1));

        assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals(0, filmController.getFilms().size());
    }

    @Test
    void whenMoviesDurationIsLessThan0_shouldNotAdded() {
        film.setDuration(-92);

        assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals(0, filmController.getFilms().size());
    }
}
