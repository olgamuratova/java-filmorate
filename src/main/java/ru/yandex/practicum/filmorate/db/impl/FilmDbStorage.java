package ru.yandex.practicum.filmorate.db.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.db.FilmStorage;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        addExtraInfoToFilms(result);
        return result;
    }

    @Override
    public List<Film> getFilmsByQuery(String query, String type) {
        String sql = "SELECT * FROM films f " +
                "LEFT JOIN film_director fd ON f.film_id = fd.film_id " +
                "LEFT JOIN director d ON fd.director_id = d.director_id " +
                "WHERE " +
                "(? = 'title' AND LOWER(f.name) LIKE '%' || LOWER(?) || '%') OR " +
                "(? = 'director' AND d.director_name LIKE '%' || LOWER(?) || '%') OR " +
                "(? = 'title,director' AND (LOWER(f.name) LIKE '%' || LOWER(?) || '%' OR LOWER(d.director_name) LIKE '%' || LOWER(?) || '%')) OR " +
                "(? = 'director,title' AND (LOWER(f.name) LIKE '%' || LOWER(?) || '%' OR LOWER(d.director_name) LIKE '%' || LOWER(?) || '%'))";
        List<Film> result = jdbcTemplate.query(sql, new FilmMapper(), type, query, type, query, type, query, query, type, query, query)
                .stream()
                .sorted(Comparator.comparingInt(this::getLikesQuantity).reversed())
                .collect(Collectors.toList());
        addExtraInfoToFilms(result);
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
                    Objects.requireNonNull(result.getDate("release_date")).toLocalDate(),
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
                addExtraInfoToFilms(result);
                return result;
            }

            if (sortBy[0].equals("year")) {
                String query = "SELECT * " +
                        "FROM films AS f " +
                        "LEFT OUTER JOIN film_director AS fd ON f.film_id = fd.film_id " +
                        "WHERE fd.director_id = ? " +
                        "ORDER BY f.release_date";
                List<Film> result = jdbcTemplate.query(query, new FilmMapper(), directorId);
                addExtraInfoToFilms(result);
                return result;
            }
        }
        throw new ObjectNotFoundException("Фильмы этого режиссера не найдены");
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

    @Override
    public List<Film> getRecommendedFilms(int userId) {

        String query = "SELECT FILMS.*, RATING.*\n" + "FROM FILMS\n" + "         JOIN FILM_MPA AS RATING ON RATING.MPA_ID = FILMS.MPA_ID\n" + "WHERE FILMS.FILM_ID IN (SELECT DISTINCT FILM_ID\n" + "                        FROM LIKES\n" + "                        WHERE USER_ID IN (SELECT USER_ID\n" + "                                          FROM ( SELECT USER_ID, COUNT(*) MATCHES\n" + "                                                 FROM LIKES\n" + "                                                 WHERE NOT USER_ID = ?\n" + "                                                   AND FILM_ID IN (SELECT FILM_ID FROM LIKES WHERE USER_ID = ?)\n" + "                                                 GROUP BY USER_ID\n" + "                                                 ORDER BY COUNT(*) DESC ) as UIM\n" + "                                          GROUP BY USER_ID\n" + "                                          HAVING MATCHES = MAX(MATCHES))\n" + "                          AND FILM_ID NOT IN (SELECT FILM_ID FROM LIKES WHERE USER_ID = ?))";

        List<Film> result = jdbcTemplate.query(query, new FilmMapper(), userId, userId, userId);
        for (Film film : result) {
            Mpa mpa = mpaDbStorage.getById(film.getMpa().getId());
            film.setMpa(mpa);
            film.setGenres(getGenres(film.getId()));
        }
        return result;
    }

    @Override
    public void deleteFilm(Integer id) {
        String sql = "delete from films where film_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        String sqlQuery = "SELECT f.*, COUNT(l.user_id) AS likes_count " +
                "FROM films f " +
                "JOIN likes l ON f.film_id = l.film_id " +
                "JOIN ( " +
                "    SELECT DISTINCT f.film_id " +
                "    FROM films f " +
                "    JOIN likes l1 ON f.film_id = l1.film_id " +
                "    JOIN likes l2 ON l1.film_id = l2.film_id " +
                "    WHERE l1.user_id = ? AND l2.user_id = ? " +
                ") AS common_films ON f.film_id = common_films.film_id " +
                "GROUP BY f.film_id " +
                "ORDER BY likes_count DESC ";
        List<Film> result = jdbcTemplate.query(sqlQuery, new FilmMapper(), userId, friendId);
        addExtraInfoToFilms(result);
        return result;
    }

    private void addExtraInfoToFilms(List<Film> films) {
        for (Film film : films) {
            Mpa mpa = mpaDbStorage.getById(film.getMpa().getId());
            film.setMpa(mpa);
            film.setGenres(getGenres(film.getId()));
            film.setDirectors(getDirectors(film.getId()));
        }
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
