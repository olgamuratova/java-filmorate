package ru.yandex.practicum.filmorate.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.impl.MpaDbStorage;
import ru.yandex.practicum.filmorate.FilmorateApplication;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = FilmorateApplication.class)
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaTests {

    final MpaDbStorage mpaDbStorage;

    @Test
    void testFindNameMpa() {
        LinkedList<String> nameMpa = new LinkedList<>();
        nameMpa.add("G");
        nameMpa.add("PG");
        nameMpa.add("PG-13");
        nameMpa.add("R");
        nameMpa.add("NC-17");
        for (int i = 0; i < nameMpa.size(); i++) {
            assertEquals(mpaDbStorage.getById(i + 1).getName(), nameMpa.get(i), "Название не соответствует");
        }
    }

    @Test
    void testFindAll() {
        assertEquals(5, mpaDbStorage.getMpa().size(), "Размер не соответсвует");
    }
}
