package ru.yandex.practicum.filmorate.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.FilmorateApplication;
import ru.yandex.practicum.filmorate.impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.impl.GenreDbStorage;
import ru.yandex.practicum.filmorate.impl.UserDbStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = FilmorateApplication.class)
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmTests {

    final FilmDbStorage filmDbStorage;
    final GenreDbStorage genreDbStorage;
    final UserDbStorage userStorage;

    @BeforeEach
    void createdFilmForDB() {
        if (filmDbStorage.getFilms().size() != 2) {
            List<Genre> genres = new ArrayList<>();
            genres.add(new Genre(2, genreDbStorage.getById(2).getName()));
            Film film = new Film(new Mpa(1, "G"),"Film1", "Description film1", LocalDate.parse("1999-01-01"),
                    87, genres);
            filmDbStorage.create(film);
            filmDbStorage.addGenres(1, genres);
            Film filmNext = new Film(new Mpa(2, "PG"),"Film2", "Description film2", LocalDate.parse("2020-01-01"),
                    75, genres);
            filmDbStorage.create(filmNext);
            filmDbStorage.addGenres(2, genres);
        }
        if (userStorage.getUsers().size() != 2) {
            User firstTestUser = new User("test1@yandex.ru", "User1", "Tester1", LocalDate.parse("1999-01-01"));
            userStorage.create(firstTestUser);
            User secondTestUser = new User("test2@yandex.ru", "User2", "Tester2", LocalDate.parse("2000-01-01"));
            userStorage.create(secondTestUser);
        }
    }

    @Test
    void testAddFilm() {
        checkFindFilmById(1);
        checkFindFilmById(2);
    }

    @Test
    void testUpgradeFilm() {
        List<Genre> genres = new ArrayList<>();
        genres.add(new Genre(2, genreDbStorage.getById(2).getName()));
        Film updateFilm = new Film(new Mpa(1, "G"), "Film1", "updateTest", LocalDate.parse("1999-01-01"), 87, genres);
        updateFilm.setId(1);
        filmDbStorage.update(updateFilm);
        Optional<Film> filmDbStorageFilm = Optional.ofNullable(filmDbStorage.getById(1));
        assertThat(filmDbStorageFilm)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("description", "updateTest")
                );
    }

    @Test
    void testFindFilm() {
        checkFindFilmById(1);
    }

    @Test
    void testFindAll() {
        List<Film> current = filmDbStorage.getFilms();
        Assertions.assertEquals(2, current.size(), "Некорректное количество фильмов");
    }

    void checkFindFilmById(Integer idFilm) {
        Optional<Film> filmOptional = Optional.ofNullable(filmDbStorage.getById(idFilm));
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", idFilm)
                );
    }
}
