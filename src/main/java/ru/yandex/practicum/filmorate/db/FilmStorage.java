package ru.yandex.practicum.filmorate.db;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Set;

public interface FilmStorage {

    Film create(Film film);

    Film update(Film film);

    List<Film> getFilms();

    Film getById(Integer id);

    void addLike(Integer filmId, Integer userId);

    void deleteLike(Integer filmId, Integer userId);

    Integer getLikesQuantity(Film film);

    void addGenres(int filmId, Set<Genre> genres);

    void updateGenres(int filmId, Set<Genre> genres);

    Set<Genre> getGenres(int filmId);

    void deleteGenres(int filmId);
}
