-- 테이블 순서는 관계를 고려하여 한 번에 실행해도 에러가 발생하지 않게 정렬되었습니다.

-- locations Table Create SQL
-- 테이블 생성 SQL - locations
CREATE TABLE locations
(
    id           uuid            NOT NULL,
    latitude     double precision          NOT NULL,
    longitude    double precision          NOT NULL,
    x            integer         NOT NULL,
    y            integer         NOT NULL,
    name         varchar(255)    NOT NULL,
    PRIMARY KEY (id)
);

-- -- Unique Index 설정 SQL - locations(latitude, longitude)
-- CREATE UNIQUE INDEX UQ_locations_1
--     ON locations(latitude, longitude);

-- 새로 추가!!!
-- CREATE TYPE role AS ENUM ('USER', 'ADMIN');
-- CREATE TYPE gender AS ENUM ('FEMALE', 'MALE', 'OTHER');

-- users Table Create SQL
-- 테이블 생성 SQL - users
CREATE TABLE users
(
    id                         uuid            NOT NULL,
    created_at                 TIMESTAMP WITH TIME ZONE    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at                 TIMESTAMP WITH TIME ZONE    NULL,
    email                      varchar(255)    NOT NULL,
    gender                     varchar(20),
    role                       varchar(20)     NOT NULL DEFAULT 'USER',
    name                       varchar(255)    NOT NULL,
    password                   varchar(255)    NOT NULL,
    locked                     boolean         DEFAULT FALSE NOT NULL,
    birth_date                 date            NULL,
    temperature_sensitivity    integer         NULL,
    profile_image_url          varchar(512)    NULL,
    linked_oauth_providers     jsonb           NULL,
    location_id                uuid            NULL,
    temp_password_expiration_time TIMESTAMP WITH TIME ZONE NULL, -- 0702 새로 추가
    PRIMARY KEY (id)
);

-- Unique Index 설정 SQL - users(name)
-- CREATE UNIQUE INDEX UQ_users_2
--     ON users(name);

-- Foreign Key 설정 SQL - users(location_id) -> locations(id)
ALTER TABLE users
    ADD CONSTRAINT FK_users_location_id_locations_id FOREIGN KEY (location_id)
        REFERENCES locations (id) ON DELETE RESTRICT ON UPDATE RESTRICT;

-- weather Table Create SQL
-- 테이블 생성 SQL - weather
CREATE TABLE weather
(
    id                                    uuid                        NOT NULL,
    location_id                           uuid                        NOT NULL,
    forecasted_at                         timestamp with time zone    NOT NULL,
    forecast_at                           timestamp with time zone    NOT NULL,
    sky_status                            varchar(20)                 ,
    precipitation_type                    varchar(10)                 ,
    precipitation_amount                  double precision            ,
    precipitation_probability             double precision            ,
    humidity_current                      double precision            ,
    humidity_compared_to_day_before       double precision            ,
    temperature_current                   double precision            ,
    temperature_compared_to_day_before    double precision            ,
    temperature_min                       double precision            ,
    temperature_max                       double precision            ,
    wind_speed                            double precision            ,
    wind_speed_as_word                    varchar(10)                 ,
    PRIMARY KEY (id)
);

-- Foreign Key 설정 SQL - weather(location_id) -> locations(id)
ALTER TABLE weather
    ADD CONSTRAINT FK_weather_location_id_locations_id FOREIGN KEY (location_id)
        REFERENCES locations (id) ON DELETE RESTRICT ON UPDATE RESTRICT;


-- clothes_attribute_def Table Create SQL
-- 테이블 생성 SQL - clothes_attribute_def
CREATE TABLE clothes_attribute_def
(
    id                   uuid                        NOT NULL,
    created_at           timestamp with time zone    NOT NULL,
    updated_at           timestamp with time zone    NULL,
    name                 varchar(50)                 NOT NULL,
    selectable_values    jsonb               NOT NULL,
    PRIMARY KEY (id)
);


-- clothes Table Create SQL
-- 테이블 생성 SQL - clothes
CREATE TABLE clothes (
                         id            uuid                        NOT NULL,
                         created_at    timestamp with time zone    NOT NULL,
                         updated_at    timestamp with time zone    NULL,
                         name          varchar(255)                NOT NULL,
                         image_url     varchar(512)                NULL,
                         type          varchar(40)                 NOT NULL,
                         owner_id      uuid                        NOT NULL,
                         PRIMARY KEY (id)
);

-- Foreign Key 설정 SQL - clothes(owner_id) -> users(id)
ALTER TABLE clothes
    ADD CONSTRAINT FK_clothes_owner_id_users_id FOREIGN KEY (owner_id)
        REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT;


-- feed Table Create SQL
-- 테이블 생성 SQL - feed
CREATE TABLE feed
(
    id               uuid                        NOT NULL,
    created_at       timestamp with time zone    NOT NULL,
    updated_at       timestamp with time zone    NULL,
    author_id        uuid                        NOT NULL,
    like_count       integer                     DEFAULT 0 NOT NULL,
    comment_count    integer                     DEFAULT 0 NOT NULL,
    content          text                        NOT NULL,
    weather_id       uuid                        NULL,
    PRIMARY KEY (id)
);

-- Foreign Key 설정 SQL - feed(author_id) -> users(id)
ALTER TABLE feed
    ADD CONSTRAINT FK_feed_author_id_users_id FOREIGN KEY (author_id)
        REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT;

-- Foreign Key 설정 SQL - feed(weather_id) -> weather(id)
ALTER TABLE feed
    ADD CONSTRAINT FK_feed_weather_id_weather_id FOREIGN KEY (weather_id)
        REFERENCES weather (id) ON DELETE RESTRICT ON UPDATE RESTRICT;


-- weather_api_data Table Create SQL
-- 테이블 생성 SQL - weather_api_data
CREATE TABLE weather_api_data
(
    base_date     varchar(10)    NOT NULL,
    base_time     varchar(6)     NOT NULL,
    category      varchar(6)     NOT NULL,
    fcst_date     varchar(10)    NOT NULL,
    fcst_time     varchar(6)     NOT NULL,
    fcst_value    varchar(50)    NOT NULL,
    nx            integer        NOT NULL,
    ny            integer        NOT NULL,
    PRIMARY KEY (base_date, base_time, category)
);


-- notifications Table Create SQL
-- 테이블 생성 SQL - notifications
CREATE TABLE notifications
(
    id             uuid                        NOT NULL,
    created_at     timestamp with time zone    NOT NULL,
    receiver_id    uuid                        NOT NULL,
    title          varchar(255)                NOT NULL,
    content        varchar(255)                NOT NULL,
    level          varchar(16)                 NOT NULL,
    PRIMARY KEY (id)
);

-- follows Table Create SQL
-- 테이블 생성 SQL - follows
CREATE TABLE follows
(
    id             uuid    NOT NULL,
    created_at     timestamp with time zone  NOT NULL,
    followee_id    uuid    NOT NULL,
    follower_id    uuid    NOT NULL,
    PRIMARY KEY (id)
);

-- Foreign Key 설정 SQL - follows(followee_id) -> users(id)
ALTER TABLE follows
    ADD CONSTRAINT FK_FOLLOWS_USERS_1 FOREIGN KEY (followee_id)
        REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT;


-- Foreign Key 설정 SQL - follows(follower_id) -> users(id)
ALTER TABLE follows
    ADD CONSTRAINT FK_FOLLOWS_USERS_2 FOREIGN KEY (follower_id)
        REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT;


-- direct_messages Table Create SQL
-- 테이블 생성 SQL - direct_messages
CREATE TABLE direct_messages
(
    id             uuid                        NOT NULL,
    created_at     timestamp with time zone    NOT NULL,
    sender_id      uuid                        NOT NULL,
    receiver_id    uuid                        NOT NULL,
    content        varchar(512)                NOT NULL,
    PRIMARY KEY (id)
);


-- Foreign Key 설정 SQL - direct_messages(sender_id) -> users(id)
ALTER TABLE direct_messages
    ADD CONSTRAINT FK_DIRECT_MESSAGE_USERS_1 FOREIGN KEY (sender_id)
        REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT;


-- Foreign Key 설정 SQL - direct_messages(receiver_id) -> users(id)
ALTER TABLE direct_messages
    ADD CONSTRAINT FK_DIRECT_MESSAGE_USERS_2 FOREIGN KEY (receiver_id)
        REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT;


-- clothes_attribute Table Create SQL
-- 테이블 생성 SQL - clothes_attribute
CREATE TABLE clothes_attribute
(
    id               uuid                        NOT NULL,
    created_at       timestamp with time zone    NOT NULL,
    updated_at       timestamp with time zone    NULL,
    clothes_id       uuid                        NULL,
    definition_id    uuid                        NULL,
    value            varchar(50)                 NULL,
    PRIMARY KEY (id)
);

-- Foreign Key 설정 SQL - clothes_attribute(definition_id) -> clothes_attribute_def(id)
ALTER TABLE clothes_attribute
    ADD CONSTRAINT FK_clothes_attribute_definition_id_clothes_attribute_def_id FOREIGN KEY (definition_id)
        REFERENCES clothes_attribute_def (id) ON DELETE RESTRICT ON UPDATE RESTRICT;


-- Foreign Key 설정 SQL - clothes_attribute(clothes_id) -> clothes(id)
ALTER TABLE clothes_attribute
    ADD CONSTRAINT FK_clothes_attribute_clothes_id_clothes_id FOREIGN KEY (clothes_id)
        REFERENCES clothes (id) ON DELETE RESTRICT ON UPDATE RESTRICT;



-- jwt_sessions Table Create SQL
-- 테이블 생성 SQL - jwt_sessions
CREATE TABLE jwt_sessions
(
    id                 uuid                        NOT NULL,
    created_at         timestamp with time zone    NOT NULL,
    updated_at         timestamp with time zone    NULL,
    expiration_time    timestamp with time zone    NULL,
    user_id            uuid                        NOT NULL,
    access_token       varchar(512)                NOT NULL,
    refresh_token      varchar(512)                NULL,
    PRIMARY KEY (id)
);


-- ootd Table Create SQL
-- 테이블 생성 SQL - ootd
CREATE TABLE ootd
(
    id            uuid                        NOT NULL,
    created_at    timestamp with time zone    NOT NULL,
    updated_at    timestamp with time zone    NOT NULL,
    clothes_id    uuid                        NOT NULL,
    feed_id       uuid                        NOT NULL,
    PRIMARY KEY (id)
);

-- Foreign Key 설정 SQL - ootd(clothes_id) -> clothes(id)
ALTER TABLE ootd
    ADD CONSTRAINT FK_ootd_clothes_id_clothes_id FOREIGN KEY (clothes_id)
        REFERENCES clothes (id) ON DELETE RESTRICT ON UPDATE RESTRICT;

-- Foreign Key 설정 SQL - ootd(feed_id) -> feed(id)
ALTER TABLE ootd
    ADD CONSTRAINT FK_ootd_feed_id_feed_id FOREIGN KEY (feed_id)
        REFERENCES feed (id) ON DELETE RESTRICT ON UPDATE RESTRICT;



-- feed_comment Table Create SQL
-- 테이블 생성 SQL - feed_comment
CREATE TABLE feed_comment
(
    id            uuid                        NOT NULL,
    created_at    timestamp with time zone    NOT NULL,
    feed_id       uuid                        NOT NULL,
    author_id     uuid                        NOT NULL,
    content       text                        NOT NULL,
    PRIMARY KEY (id)
);


-- Foreign Key 설정 SQL - feed_comment(feed_id) -> feed(id)
ALTER TABLE feed_comment
    ADD CONSTRAINT FK_feed_comment_feed_id_feed_id FOREIGN KEY (feed_id)
        REFERENCES feed (id) ON DELETE RESTRICT ON UPDATE RESTRICT;


-- Foreign Key 설정 SQL - feed_comment(author_id) -> users(id)
ALTER TABLE feed_comment
    ADD CONSTRAINT FK_feed_comment_author_id_users_id FOREIGN KEY (author_id)
        REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT;


-- feed_like Table Create SQL
-- 테이블 생성 SQL - feed_like
CREATE TABLE feed_like
(
    id            uuid                        NOT NULL,
    created_at    timestamp with time zone    NOT NULL,
    updated_at    timestamp with time zone    NULL,
    feed_id       uuid                        NOT NULL,
    user_id       uuid                        NOT NULL,
    PRIMARY KEY (id)
);


-- Foreign Key 설정 SQL - feed_like(feed_id) -> feed(id)
ALTER TABLE feed_like
    ADD CONSTRAINT FK_feed_like_feed_id_feed_id FOREIGN KEY (feed_id)
        REFERENCES feed (id) ON DELETE RESTRICT ON UPDATE RESTRICT;


-- Foreign Key 설정 SQL - feed_like(user_id) -> users(id)
ALTER TABLE feed_like
    ADD CONSTRAINT FK_feed_like_user_id_users_id FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT;
