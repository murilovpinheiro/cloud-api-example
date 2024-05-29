CREATE TABLE movies (
                        id SERIAL PRIMARY KEY,
                        title VARCHAR(150) NOT NULL,
                        release_year integer NOT NULL,
                        description TEXT NOT NULL,
                        image VARCHAR(250) NOT NULL
);