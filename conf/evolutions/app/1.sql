-- !Ups
CREATE SCHEMA users AUTHORIZATION app

CREATE TABLE IF NOT EXISTS users.users(
    id serial primary key,
    username varchar(255) NOT NULL,
    email varchar(255) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT False,
    email_verification_code varchar(255),
    hash varchar(255) NOT NULL,
    salt varchar(255) NOT NULL,
    password_reset_code varchar(255) NULL,
    profile_image_id INT,
    created_on date NOT NULL DEFAULT now(),
    last_seen date NOT NULL DEFAULT now(),
    gender varchar(16),
    bio TEXT,
    bio_updated date NULL
);

ALTER TABLE users.users ADD CONSTRAINT unique_username UNIQUE (username);
ALTER TABLE users.users ADD CONSTRAINT unique_email UNIQUE (email);

CREATE TABLE IF NOT EXISTS users.roles(
    id serial primary key,
    role varchar(16) NOT NULL
);

ALTER TABLE users.roles ADD CONSTRAINT unique_role UNIQUE (role);
--
INSERT INTO users.roles(role) VALUES
('ADMIN'),('MODERATOR');

CREATE TABLE IF NOT EXISTS users.user_roles(
    user_id int references users.users(id),
    role_id int references users.roles(id),
    PRIMARY KEY(user_id, role_id)
);


CREATE TABLE IF NOT EXISTS users.user_ips(
    user_id int references users.users(id),
    ip  varchar(39),
    date date,
    PRIMARY KEY(user_id, ip)
);

CREATE TABLE IF NOT EXISTS users.banns(
     user_id int primary key references users.users(id),
     reason TEXT,
     until date null
);

CREATE TABLE IF NOT EXISTS users.circles(
    id serial primary key,
    user_id int references users.users(id),
    name varchar(255),
    colour VARCHAR(7) null
);
ALTER TABLE users.circles ADD CONSTRAINT color_hex_constraint
        CHECK (colour is null or colour ~* '^#[a-f0-9]{6}$');


CREATE TABLE IF NOT EXISTS users.user_circles(
                                            id serial primary key,
                                            user_id_a int not null references users.users(id),
                                            user_id_b int not null references users.users(id),
                                            bilateral boolean not null,
                                            user_a_circle_id int not null references users.circles(id),
                                            user_b_circle_id int not null references users.circles(id)
);
ALTER TABLE users.user_circles ADD CONSTRAINT self_friend_constraint
    CHECK (user_id_a !=  user_id_b);


CREATE SCHEMA images AUTHORIZATION app
CREATE TABLE IF NOT EXISTS images.images(
                                         id serial primary key,
                                         user_id int not null references users.users(id),
                                         path TEXT,
                                         caption TEXT,
                                         public BOOLEAN,
                                         hidden BOOLEAN,
                                         uploaded date
);

CREATE TABLE IF NOT EXISTS images.albums(
                                            id serial primary key,
                                            user_id int not null references users.users(id),
                                            name TEXT
);

CREATE TABLE IF NOT EXISTS images.image_permissions(
                                            image_id int not null references images.images(id),
                                            circle   int not null references users.circles(id),
                                            allow    boolean
);

CREATE TABLE IF NOT EXISTS images.album_permissions(
                                                 album_id int not null references images.albums(id),
                                                 circle   int not null references users.circles(id),
                                                 allow    boolean
);


CREATE SCHEMA messages AUTHORIZATION app;
CREATE TABLE IF NOT EXISTS messages.messages(
    from_user_id int references users.users(id),
    to_user_id int references users.users(id),
    date_sent date,
    subject TEXT,
    body TEXT,
    state TEXT,
    deleted_by_sender boolean,
    deleted_by_recipient boolean
);

-- !Downs

DROP SCHEMA users CASCADE;
DROP SCHEMA messages CASCADE;
DROP SCHEMA images CASCADE;

