package ru.yandex.practicum.filmorate.db.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.db.MpaStorage;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Mpa> getMpa() {
        String query = "SELECT * from film_mpa";
        return jdbcTemplate.query(query, new MpaMapper());
    }

    @Override
    public Mpa getById(Integer id) {
        String query = "SELECT * from film_mpa where mpa_id = ?";
        SqlRowSet result = jdbcTemplate.queryForRowSet(query, id);
        if (result.next()) {
            Mpa mpa = new Mpa(
                    result.getInt("mpa_id"),
                    result.getString("mpa_rating")
            );
            return mpa;
        }
        throw new ObjectNotFoundException("Mpa с id не найден");
    }

    @Override
    public boolean isContains(Integer id) {
        try {
            getById(id);
            return true;
        } catch (ObjectNotFoundException exception) {
            return false;
        }
    }
}
