package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.db.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public void addLike(Integer filmId, Integer userId) {
        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(Integer filmId, Integer userId) {
        filmStorage.deleteLike(filmId, userId);
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
        return finalResult;
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
