CREATE TABLE movies (
                        id SERIAL PRIMARY KEY,
                        title TEXT NOT NULL,
                        release_year integer NOT NULL,
                        description TEXT NOT NULL,
                        image TEXT NOT NULL
);