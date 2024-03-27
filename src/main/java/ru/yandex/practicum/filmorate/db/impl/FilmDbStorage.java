package ru.yandex.practicum.filmorate.db.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.db.FilmStorage;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film create(Film film) {
        validateFilm(film);
        String query = "INSERT INTO films(name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(query, film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId());

        String querySelect = "SELECT * FROM films WHERE name = ? AND description = ? AND release_date = ? AND duration = ? AND mpa_id = ?";
        SqlRowSet result = jdbcTemplate.queryForRowSet(querySelect, film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId());
        if (result.next()) {
            Mpa mpa = new Mpa();
            mpa.setId(result.getInt("mpa_id"));
            Set<Genre> genres = film.getGenres();
            film = new Film(
                    result.getInt("film_id"),
                    mpa,
                    result.getString("name"),
                    result.getString("description"),
                    result.getDate("release_date").toLocalDate(),
                    result.getInt("duration")
            );
            addGenres(film.getId(), genres);
            film.setGenres(getGenres(film.getId()));
        }
        return film;
    }

    @Override
    public Film update(Film film) {
        getById(film.getId());
        String query = "UPDATE films set name = ?, description = ?, releaseDate = ?, duration = ?, mpa_id = ? WHERE fild_id = ?";
        jdbcTemplate.update(query, film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa(),
                film.getId());
        Set<Genre> genres = film.getGenres();
        updateGenres(film.getId(), genres);
        film.setGenres(getGenres(film.getId()));
        return film;
    }

    @Override
    public List<Film> getFilms() {
        String query = "SELECT * from films";
        List<Film> result = jdbcTemplate.query(query, new FilmMapper());
        for (Film film : result) {
            film.setGenres(getGenres(film.getId()));
        }
        return result;
    }

    @Override
    public Film getById(Integer id) {
        String query = "SELECT * from films where film_id = ?";
        SqlRowSet result = jdbcTemplate.queryForRowSet(query, id);
        if (result.next()) {
            Mpa mpa = new Mpa();
            mpa.setId(result.getInt("mpa_id"));
            Film film = new Film(
                    result.getInt("film_id"),
                    mpa,
                    result.getString("name"),
                    result.getString("description"),
                    result.getDate("release_date").toLocalDate(),
                    result.getInt("duration")
            );
            film.setGenres(getGenres(id));
            return film;
        }
        throw new ObjectNotFoundException("Фильм с id не найден");
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        String query = "INSERT INTO likes(filmId, userId) VALUES (?, ?)";
        jdbcTemplate.update(query, filmId, userId);
    }

    @Override
    public void deleteLike(Integer filmId, Integer userId) {
        String query = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(query, filmId, userId);
    }

    @Override
    public Integer getLikesQuantity(Film film) {
        String query = "SELECT COUNT(*) FROM likes WHERE film_id = ?";
        return jdbcTemplate.queryForObject(query, Integer.class, film.getId());
    }

    @Override
    public void addGenres(int filmId, Set<Genre> genres) {
        for (Genre genre : genres) {
            jdbcTemplate.update("INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)", filmId, genre.getId());
        }
    }

    @Override
    public void updateGenres(int filmId, Set<Genre> genres) {
        deleteGenres(filmId);
        addGenres(filmId, genres);
    }

    @Override
    public Set<Genre> getGenres(int filmId) {
        Set<Genre> genres = new HashSet<>(jdbcTemplate.query(
                "SELECT f.genre_id, g.genre_type FROM film_genre AS f " +
                        "LEFT OUTER JOIN genre AS g ON f.genre_id = g.genre_id WHERE f.film_id=? ORDER BY g.genre_id",
                new GenreMapper(), filmId));
        return genres;
    }

    @Override
    public void deleteGenres(int filmId) {
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id=?", filmId);
    }

    private void validateFilm(Film film) {
        if (film.getName().isEmpty()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }
}
