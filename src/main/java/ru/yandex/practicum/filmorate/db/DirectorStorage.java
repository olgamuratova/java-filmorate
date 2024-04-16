package ru.yandex.practicum.filmorate.db;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorStorage {

    List<Director> getDirectors();

    Director getDirectorById(Integer id);

    Director createDirector(Director director);

    Director updateDirector(Director director);

    void removeDirectorById(Integer id);
}
