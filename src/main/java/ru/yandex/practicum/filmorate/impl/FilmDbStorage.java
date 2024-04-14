package ru.yandex.practicum.filmorate.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.FilmStorage;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    private final MpaDbStorage mpaDbStorage;

    private final GenreDbStorage genreDbStorage;

    private final DirectorDbStorage directorDbStorage;

    @Override
    public Film create(Film film) {
        checkIfExists(film, true);
        validateFilm(film);

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FILMS")
                .usingGeneratedKeyColumns("film_id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", film.getName());
        parameters.put("description", film.getDescription());
        parameters.put("release_date", film.getReleaseDate());
        parameters.put("duration", film.getDuration());
        parameters.put("mpa_id", film.getMpa().getId());

        int id = simpleJdbcInsert.executeAndReturnKey(parameters).intValue();
        film.setId(id);

        Mpa mpa = mpaDbStorage.getById(film.getMpa().getId());
        film.setMpa(mpa);
        List<Genre> genres = film.getGenres();
        if (genres != null && !genres.isEmpty()) {
            addGenres(film.getId(), genres);
            film.setGenres(getGenres(film.getId()));
        }

        List<Director> directors = film.getDirectors();
        if (directors != null && !directors.isEmpty()) {
            addDirectors(film.getId(), directors);
            film.setDirectors(getDirectors(film.getId()));
        }
        return film;
    }

    @Override
    public Film update(Film film) {
        checkIfExists(film, false);
        validateFilm(film);
        String query = "UPDATE films set name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                "WHERE film_id = ?";
        jdbcTemplate.update(query, film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        List<Genre> genres = film.getGenres();
        if (genres != null && !genres.isEmpty()) {
            updateGenres(film.getId(), genres);
            film.setGenres(getGenres(film.getId()));
        } else {
            deleteGenres(film.getId());
            film.setGenres(new ArrayList<>());
        }

        List<Director> directors = film.getDirectors();
        if (directors != null && !directors.isEmpty()) {
            updateDirectors(film.getId(), directors);
            film.setDirectors(getDirectors(film.getId()));
        } else {
            deleteDirectors(film.getId());
            film.setDirectors(new ArrayList<>());
        }

        return film;
    }

    @Override
    public List<Film> getFilms() {
        String query = "SELECT * from films";
        List<Film> result = jdbcTemplate.query(query, new FilmMapper());
        for (Film film : result) {
            Mpa mpa = mpaDbStorage.getById(film.getMpa().getId());
            film.setMpa(mpa);
            film.setGenres(getGenres(film.getId()));
            film.setDirectors(getDirectors(film.getId()));
        }
        return result;
    }

    @Override
    public Film getById(Integer id) {
        String query = "SELECT * from films where film_id = ?";
        SqlRowSet result = jdbcTemplate.queryForRowSet(query, id);
        if (result.next()) {
            Mpa mpa = mpaDbStorage.getById(result.getInt("mpa_id"));
            Film film = new Film(
                    result.getInt("film_id"),
                    mpa,
                    result.getString("name"),
                    result.getString("description"),
                    result.getDate("release_date").toLocalDate(),
                    result.getInt("duration")
            );
            film.setGenres(getGenres(id));
            film.setDirectors(getDirectors(id));
            return film;
        }
        throw new ObjectNotFoundException("Фильм с id не найден");
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        String query = "INSERT INTO likes(film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(query, filmId, userId);
    }

    @Override
    public void deleteLike(Integer filmId, Integer userId) {
        String query = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

        if (jdbcTemplate.update(query, filmId, userId) == 0) {
            throw new ObjectNotFoundException("Не найдено");
        }

    }

    @Override
    public Integer getLikesQuantity(Film film) {
        String query = "SELECT COUNT(*) FROM likes WHERE film_id = ?";
        return jdbcTemplate.queryForObject(query, Integer.class, film.getId());
    }

    @Override
    public void addGenres(int filmId, List<Genre> genres) {
        for (Genre genre : genres) {
            jdbcTemplate.update("INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)", filmId, genre.getId());
        }
    }

    @Override
    public void updateGenres(int filmId, List<Genre> genres) {
        deleteGenres(filmId);
        addGenres(filmId, genres);
    }

    @Override
    public List<Genre> getGenres(int filmId) {
        List<Genre> genres = new ArrayList<>(jdbcTemplate.query(
                "SELECT f.genre_id, g.genre_type FROM film_genre AS f " +
                        "LEFT OUTER JOIN genre AS g ON f.genre_id = g.genre_id WHERE f.film_id = ?",
                new GenreMapper(), filmId));
        return genres.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public void deleteGenres(int filmId) {
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id=?", filmId);
    }

    @Override
    public List<Film> getFilmsOfDirector(Integer directorId, String[] sortBy) {
        if (directorDbStorage.isContains(directorId)) {
            if (sortBy[0].equals("likes")) {
                String query = "SELECT f.* " +
                        "FROM films AS f " +
                        "LEFT OUTER JOIN (SELECT COUNT(user_id), film_id FROM likes GROUP BY film_id " +
                        "ORDER BY COUNT(user_id)) AS l ON f.film_id = l.film_id " +
                        "LEFT OUTER JOIN film_director AS fd ON f.film_id = fd.film_id " +
                        "LEFT OUTER JOIN director AS d ON fd.director_id = d.director_id " +
                        "WHERE fd.director_id = ? ";

                List<Film> result = jdbcTemplate.query(query, new FilmMapper(), directorId);
                for (Film film : result) {
                    Mpa mpa = mpaDbStorage.getById(film.getMpa().getId());
                    film.setMpa(mpa);
                    film.setGenres(getGenres(film.getId()));
                    film.setDirectors(getDirectors(film.getId()));
                }
                return result;
            }

            if (sortBy[0].equals("year")) {
                String query = "SELECT * " +
                        "FROM films AS f " +
                        "LEFT OUTER JOIN film_director AS fd ON f.film_id = fd.film_id " +
                        "WHERE fd.director_id = ? " +
                        "ORDER BY f.release_date";
                List<Film> result = jdbcTemplate.query(query, new FilmMapper(), directorId);
                for (Film film : result) {
                    Mpa mpa = mpaDbStorage.getById(film.getMpa().getId());
                    film.setMpa(mpa);
                    film.setGenres(getGenres(film.getId()));
                    film.setDirectors(getDirectors(film.getId()));
                }
                return result;
            }
        }
        throw new ObjectNotFoundException("Ошибка");
    }

    @Override
    public void addDirectors(int filmId, List<Director> directors) {
        for (Director director : directors) {
            jdbcTemplate.update("INSERT INTO film_director (film_id, director_id) VALUES (?, ?)", filmId,
                    director.getId());
        }
    }

    @Override
    public void updateDirectors(int filmId, List<Director> directors) {
        deleteDirectors(filmId);
        addDirectors(filmId, directors);
    }

    @Override
    public List<Director> getDirectors(int filmId) {
        List<Director> directors = new ArrayList<>(jdbcTemplate.query(
                "SELECT f.director_id, d.director_name FROM film_director AS f " +
                        "LEFT OUTER JOIN director AS d ON f.director_id = d.director_id WHERE f.film_id = ?",
                new DirectorMapper(), filmId));
        return directors.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public void deleteDirectors(int filmId) {
        jdbcTemplate.update("DELETE FROM film_director WHERE film_id=?", filmId);
    }

    public boolean isContains(Integer id) {
        try {
            getById(id);
            return true;
        } catch (ObjectNotFoundException exception) {
            return false;
        }
    }

    private void checkIfExists(Film film, boolean isCreate) {
        if (film.getId() != null && isCreate) {
            if (isContains(film.getId())) {
                throw new ValidationException("Фильм уже существует");
            }
        } else if (film.getId() != null && !isContains(film.getId())) {
            throw new ObjectNotFoundException("Фильм не найден");
        }
        if (film.getMpa() != null) {
            if (!mpaDbStorage.isContains(film.getMpa().getId())) {
                throw new ValidationException("MPA не найден");
            }
        }
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                if (!genreDbStorage.isContains(genre.getId())) {
                    throw new ValidationException("Жанр не найден");
                }
            }
        }
        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                if (!directorDbStorage.isContains(director.getId())) {
                    throw new ValidationException("Режиссер не найден");
                }
            }
        }

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
