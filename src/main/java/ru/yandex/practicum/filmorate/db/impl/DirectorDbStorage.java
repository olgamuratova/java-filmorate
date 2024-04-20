package ru.yandex.practicum.filmorate.db.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.db.DirectorStorage;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Director> getDirectors() {
        String query = "SELECT * from director";
        return jdbcTemplate.query(query, new DirectorMapper());
    }

    @Override
    public Director getDirectorById(Integer id) {
        String query = "SELECT * from director where director_id = ? ";

        List<Director> directorById = jdbcTemplate.query(query, new DirectorMapper(), id);

        if (!directorById.isEmpty()) {
            return directorById.get(0);
        } else {
            throw new ObjectNotFoundException("Режиссёр не найден");
        }
    }

    @Override
    public Director createDirector(Director director) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("director")
                .usingGeneratedKeyColumns("director_id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("director_name", director.getName());

        int id = simpleJdbcInsert.executeAndReturnKey(parameters).intValue();
        director.setId(id);

        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        getDirectorById(director.getId());
        String query = "update DIRECTOR set DIRECTOR_NAME=? where DIRECTOR_ID = ?";

        jdbcTemplate.update(query, director.getName(), director.getId());

        return director;
    }

    @Override
    public void removeDirectorById(Integer id) {
        getDirectorById(id);

        jdbcTemplate.update("DELETE FROM director WHERE director_id=?", id);

    }

    public boolean isContains(Integer id) {
        try {
            getDirectorById(id);
            return true;
        } catch (ObjectNotFoundException exception) {
            return false;
        }
    }
}
