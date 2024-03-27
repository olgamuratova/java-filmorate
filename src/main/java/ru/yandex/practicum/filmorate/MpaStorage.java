package ru.yandex.practicum.filmorate;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

public interface MpaStorage {

    List<Mpa> getMpa();

    Mpa getById(Integer id);

    boolean isContains(Integer id);
}
