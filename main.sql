-- Create tables
CREATE TABLE favorite_lecture (
    id_favorite_lecture SERIAL PRIMARY KEY,
    lecture_id INT NOT NULL,
    user_id INT NOT NULL
);

CREATE TABLE lecture (
    id_lecture SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    object_id INT NOT NULL,
    file_pdf BYTEA NOT NULL,
    course INT NOT NULL,
    user_id INT NOT NULL,
    count_view INT DEFAULT 0 NOT NULL
);

CREATE TABLE object (
    id_object SERIAL PRIMARY KEY,
    object_name VARCHAR(100) NOT NULL
);

CREATE TABLE specialization (
    id_specialization SERIAL PRIMARY KEY,
    specialization_name VARCHAR(150) NOT NULL
);

CREATE TABLE "user" (
    id_user SERIAL PRIMARY KEY,
    surname VARCHAR(50) NOT NULL,
    name VARCHAR(50) NOT NULL,
    patronymic VARCHAR(50),
    type_user_id INT NOT NULL,
    specialization_id INT NOT NULL,
    course INT NOT NULL
);

CREATE TABLE user_type (
    id_type_user SERIAL PRIMARY KEY,
    type_user_name VARCHAR(50) NOT NULL
);

-- Add foreign key constraints
ALTER TABLE favorite_lecture
    ADD CONSTRAINT fk_favorite_lecture_lecture FOREIGN KEY (lecture_id) REFERENCES lecture (id_lecture);

ALTER TABLE favorite_lecture
    ADD CONSTRAINT fk_favorite_lecture_user FOREIGN KEY (user_id) REFERENCES "user" (id_user);

ALTER TABLE lecture
    ADD CONSTRAINT fk_lecture_object FOREIGN KEY (object_id) REFERENCES object (id_object);

ALTER TABLE lecture
    ADD CONSTRAINT fk_lecture_user FOREIGN KEY (user_id) REFERENCES "user" (id_user);

ALTER TABLE "user"
    ADD CONSTRAINT fk_user_specialization FOREIGN KEY (specialization_id) REFERENCES specialization (id_specialization);

ALTER TABLE "user"
    ADD CONSTRAINT fk_user_user_type FOREIGN KEY (type_user_id) REFERENCES user_type (id_type_user);
