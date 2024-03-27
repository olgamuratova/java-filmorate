package ru.yandex.practicum.filmorate.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.impl.GenreDbStorage;
import ru.yandex.practicum.filmorate.FilmorateApplication;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = FilmorateApplication.class)
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreTests {

    final GenreDbStorage genreDbStorage;

    @Test
    void testFindNameGenre() {
        LinkedList<String> nameGenre = new LinkedList<>();
        nameGenre.add("Комедия");
        nameGenre.add("Драма");
        nameGenre.add("Мультфильм");
        nameGenre.add("Триллер");
        nameGenre.add("Документальный");
        nameGenre.add("Боевик");
        for (int i = 0; i < nameGenre.size(); i++) {
            assertEquals(genreDbStorage.getById(i + 1).getName(), nameGenre.get(i), "Название не соответствует");
        }
    }

    @Test
    void testFindAll() {
        assertEquals(6, genreDbStorage.getGenres().size(), "Размер не соответствует");
    }
}
