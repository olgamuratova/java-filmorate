package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.db.FilmStorage;
import ru.yandex.practicum.filmorate.db.impl.FeedDbStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final FeedDbStorage feedStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, FeedDbStorage feedStorage) {
        this.filmStorage = filmStorage;
        this.feedStorage = feedStorage;
    }

    public void addLike(Integer filmId, Integer userId) {
        filmStorage.addLike(filmId, userId);
        log.info("Запись события в таблицу аудита");
        feedStorage.addFeed("LIKE", "ADD", userId, filmId);
        log.info("Информация успешно сохранена");
    }

    public void deleteLike(Integer filmId, Integer userId) {
        filmStorage.deleteLike(filmId, userId);
        log.info("Запись события в таблицу аудита");
        feedStorage.addFeed("LIKE", "REMOVE", userId, filmId);
        log.info("Информация успешно сохранена");
    }

    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        List<Film> result = filmStorage.getFilms().stream()
                .sorted(Comparator.comparingInt(filmStorage::getLikesQuantity).reversed())
                .limit(count)
                .collect(Collectors.toList());
        List<Film> finalResult = new ArrayList<>();
        if (genreId == null && year == null) {
            return result;
        } else if (genreId != null && year != null) {
            Integer temp = 0;
            for (Film film : result) {
                if (film.getReleaseDate().getYear() == year && getStatus(film, genreId) && temp < count) {
                    finalResult.add(film);
                    temp++;
                }
            }
        } else if (genreId == null) {
            Integer temp = 0;
            for (Film film : result) {
                if (film.getReleaseDate().getYear() == year && temp < count) {
                    finalResult.add(film);
                    temp++;
                }
            }
        } else if (year == null) {
            Integer temp = 0;
            for (Film film : result) {
                if (getStatus(film, genreId) && temp < count) {
                    finalResult.add(film);
                    temp++;
                }
            }
        }
        return result;
    }

    public List<Film> getFilmsByQuery(String query, String type) {
        return filmStorage.getFilmsByQuery(query, type);
    }

    private Boolean getStatus(Film film, Integer genreId) {
        for (Genre genre : film.getGenres()) {
            if (genre.getId() == genreId) {
                return true;
            }
        }
        return false;
    }
}
