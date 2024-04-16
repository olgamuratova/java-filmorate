package ru.yandex.practicum.filmorate.db.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.db.GenreStorage;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> getGenres() {
        String query = "SELECT * from genre";
        return jdbcTemplate.query(query, new GenreMapper());
    }

    @Override
    public Genre getById(Integer id) {
        String query = "SELECT * from genre where genre_id = ?";
        SqlRowSet result = jdbcTemplate.queryForRowSet(query, id);
        if (result.next()) {
            Genre genre = new Genre(
                    result.getInt("genre_id"),
                    result.getString("genre_type")
            );
            return genre;
        }
        throw new ObjectNotFoundException("Жанр с id не найден");
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
