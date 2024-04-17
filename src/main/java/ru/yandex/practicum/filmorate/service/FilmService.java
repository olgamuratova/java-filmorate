package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.db.FilmStorage;
import ru.yandex.practicum.filmorate.db.impl.FeedDbStorage;
import ru.yandex.practicum.filmorate.model.Film;

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
        feedStorage.addFeed("LIKE", "ADD", (long) userId, (long) filmId);
        log.info("Информация успешно сохранена");
    }

    public void deleteLike(Integer filmId, Integer userId) {
        filmStorage.deleteLike(filmId, userId);
        log.info("Запись события в таблицу аудита");
        feedStorage.addFeed("LIKE", "REMOVE", (long) userId, (long) filmId);
        log.info("Информация успешно сохранена");
    }

    public List<Film> getPopularFilms(Integer count) {
        return filmStorage.getFilms().stream()
                .sorted(Comparator.comparingInt(filmStorage::getLikesQuantity).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<Film> getFilmsByQuery(String query, String type) {
        return filmStorage.getFilmsByQuery(query, type);
    }
}
