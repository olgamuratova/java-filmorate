package ru.yandex.practicum.filmorate.db;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface FilmStorage {

    Film create(Film film);

    Film update(Film film);

    List<Film> getFilms();

    List<Film> getFilmsByQuery(String query, String type);

    Film getById(Integer id);

    void addLike(Integer filmId, Integer userId);

    void deleteLike(Integer filmId, Integer userId);

    Integer getLikesQuantity(Film film);

    void addGenres(int filmId, List<Genre> genres);

    void updateGenres(int filmId, List<Genre> genres);

    List<Genre> getGenres(int filmId);

    void deleteGenres(int filmId);

    List<Film> getFilmsOfDirector(Integer directorId, String[] sortBy);

    void addDirectors(int filmId, List<Director> directors);

    void updateDirectors(int filmId, List<Director> directors);

    List<Director> getDirectors(int filmId);

    void deleteDirectors(int filmId);

    List<Film> getRecommendedFilms(int userId);
}
