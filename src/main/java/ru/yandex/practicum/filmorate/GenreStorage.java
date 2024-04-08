package ru.yandex.practicum.filmorate;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreStorage {

    List<Genre> getGenres();

    Genre getById(Integer id);

    boolean isContains(Integer id);
}
