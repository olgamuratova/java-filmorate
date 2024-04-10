package ru.yandex.practicum.filmorate.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.FilmStorage;
import ru.yandex.practicum.filmorate.exception.InternalServiceException;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Integer, Film> films = new HashMap<>();

    private int id = 1;

    @Override
    public Film create(Film film) {
        validateFilm(film);
        film.setId(id++);
        films.put(film.getId(), film);
        log.info("Добавление фильма {} с идентификатором {}", film.getName(), film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new InternalServiceException("Неизвестный фильм");
        }

        validateFilm(film);
        films.put(film.getId(), film);
        log.info("Обновление фильма {} с идентификатором {}", film.getName(), film.getId());
        return film;
    }

    @Override
    public List<Film> getFilms() {
        log.info("Текущее количество фильмов: {}", films.size());
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getById(Integer id) {
        Film film = films.get(id);
        if (film == null) {
            throw new ObjectNotFoundException("Фильм с id не найден");
        }
        return film;
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {

    }

    @Override
    public void deleteLike(Integer filmId, Integer userId) {

    }

    @Override
    public Integer getLikesQuantity(Film film) {
        return film.getLikesQuantity();
    }

    @Override
    public void addGenres(int filmId, List<Genre> genres) {

    }

    @Override
    public void updateGenres(int filmId, List<Genre> genres) {

    }

    @Override
    public List<Genre> getGenres(int filmId) {
        return null;
    }

    @Override
    public void deleteGenres(int filmId) {

    }

    @Override
    public List<Film> getRecommendedFilms(int userId) {
        return null;
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
