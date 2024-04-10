INSERT INTO film_mpa (mpa_id, mpa_rating)
VALUES (1, 'G');
INSERT INTO film_mpa (mpa_id, mpa_rating)
VALUES (2, 'PG');
INSERT INTO film_mpa (mpa_id, mpa_rating)
VALUES (3, 'PG-13');
INSERT INTO film_mpa (mpa_id, mpa_rating)
VALUES (4, 'R');
INSERT INTO film_mpa (mpa_id, mpa_rating)
VALUES (5, 'NC-17');

INSERT INTO genre (genre_type) VALUES ('Комедия');
INSERT INTO genre (genre_type) VALUES ('Драма');
INSERT INTO genre (genre_type) VALUES ('Мультфильм');
INSERT INTO genre (genre_type) VALUES ('Триллер');
INSERT INTO genre (genre_type) VALUES ('Документальный');
INSERT INTO genre (genre_type) VALUES ('Боевик');
--MERGE INTO genre (genre_id, genre_type)
--    VALUES (1, 'Комедия'), (2, 'Драма'), (3, 'Мультфильм'),(4, 'Триллер'),
--    (5, 'Документальный'), (6, 'Боевик');
--
--MERGE INTO film_mpa (mpa_id, mpa_rating)
--    VALUES (1, 'G'), (2, 'PG'), (3, 'PG-13'), (4, 'R'), (5, 'NC-17');