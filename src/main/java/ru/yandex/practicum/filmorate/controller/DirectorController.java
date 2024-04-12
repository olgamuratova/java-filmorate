package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.DirectorStorage;
import ru.yandex.practicum.filmorate.model.Director;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorStorage directorStorage;

    @Autowired
    public DirectorController(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    @GetMapping
    public List<Director> getDirectors() {
        return directorStorage.getDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable Integer id) {
        return directorStorage.getDirectorById(id);
    }

    @PostMapping
    public Director createDirector(@Valid @RequestBody Director director) {
        return directorStorage.createDirector(director);
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director) {
        return directorStorage.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void removeDirectorById(@PathVariable Integer id) {
        directorStorage.removeDirectorById(id);
    }
}
