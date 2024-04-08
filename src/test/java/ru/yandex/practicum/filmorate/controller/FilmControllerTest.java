package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.FilmStorage;
import ru.yandex.practicum.filmorate.UserStorage;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.service.impl.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.service.impl.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FilmControllerTest {

    private final FilmStorage filmStorage = new InMemoryFilmStorage();

    private final UserStorage userStorage = new InMemoryUserStorage();

    private final UserService userService = new UserService(userStorage);

    private final FilmService filmService = new FilmService(filmStorage);

    private final FilmController filmController = new FilmController(filmStorage, filmService);

    private final Film film = new Film(1, "Film", "description",
            LocalDate.of(2022, 1, 1), 98, new HashSet<>());

    private final Film updatedFilm = new Film(1, "Film",
            "newDescription",
            LocalDate.of(2022, 1, 1), 98, new HashSet<>());

    private final Film noNamedFilm = new Film(1, "", "description",
            LocalDate.of(2022, 1, 1), 98, new HashSet<>());

    private final Film longDescpriptionFilm = new Film(1, "Film",
            "yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy" +
                    "yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy" +
                    "yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy",
            LocalDate.of(2022, 1, 1), 98, new HashSet<>());
    private final User user = new User(1, "mail@mail.ru", "mail", "name",
            LocalDate.of(1999, 1, 1));

    @Test
    void whenAllGood_shouldAddFilm() {
        filmController.create(film);
        assertEquals(1, filmController.getFilms().size());
    }

    @Test
    void whenUpdate_shouldUpdateFilmData() {
        filmController.create(film);
        filmController.update(updatedFilm);
        assertEquals("newDescription", updatedFilm.getDescription());
        assertEquals(1, filmController.getFilms().size());
    }

    @Test
    void whenGetFilmById_shouldReturnAFilmWithIdOne() {
        filmController.create(film);
        Film thisFilm = filmController.getFilmById(film.getId());
        assertEquals(1, thisFilm.getId());
    }

    @Test
    void whenMovieWithAnEmptyName_shouldNotAdded() {
        assertThrows(ValidationException.class, () -> filmController.create(noNamedFilm));
        assertEquals(0, filmController.getFilms().size());
    }

    @Test
    void whenMovieWithDescriptionMoreThan200_shouldNotAdded() {
        assertThrows(ValidationException.class, () -> filmController.create(longDescpriptionFilm));
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

    @Test
    void whenRemoveLike_shouldRemoveLikeFromAFilm() {
        userStorage.create(user);
        filmController.create(film);
        filmController.likeAFilm(film.getId(), user.getId());
        filmController.removeLike(film.getId(), user.getId());
        assertEquals(0, film.getLikesQuantity());
    }

    @Test
    void whenGetPopularFilms_shouldReturnListOfPopularFilms() {
        userStorage.create(user);
        filmController.create(film);
        filmController.likeAFilm(film.getId(), user.getId());
        List<Film> popularMoviesList = filmService.getPopularFilms(1);
        assertEquals(1, popularMoviesList.size());
    }
}
