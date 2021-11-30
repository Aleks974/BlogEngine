-- roles

INSERT INTO roles (name)
VALUES ('ROLE_ADMIN'),
('ROLE_USER');

-- users

INSERT INTO users (id, is_moderator, reg_time, name, email, password)
VALUES (1, true, '2021-08-10 12:00:00', 'Aleksandr Ivanov', 'a@aa.ru', '$2a$10$hL.jlZnWZwRmoj3FY1QTQOdys3TozswyyS0QHuhf4hFqADb0FxEVe'),
(2, false, '2021-08-10 13:00:00', 'Petr Dmitriev', 'b@bb.ru', '$2a$10$pdHVkGoeVf.IjmXTkv3Bn.FKkrCdTDJMDOgLkZGEwcGZ.etAIO0UK'),
(3, false, '2021-08-10 14:00:00', 'Ivan Egorov', 'c@cc.ru', '$2a$10$5bPwiO2OSIoU.LIc8qRi1extHsYg.uzy3FfJjCnMYepdPu.ZiDo/m');

-- users_roles

INSERT INTO users_roles (user_id, role_id)
VALUES (1, 1),
(2, 2),
(3, 2);


-- posts

INSERT INTO posts (id, is_active, moderation_status, moderator_id, user_id, time, title, text, announce, view_count)
VALUES (1, true, 'NEW', NULL, 2, '2021-08-10 12:00:00', 'Пост1', 'Текст1', 'Анонс1', 0),
(2, true, 'ACCEPTED', 1, 3, '2021-08-11 13:00:00', 'Пост2', 'Текст для поиска2', 'Анонс2', 1),
(3, true, 'ACCEPTED', 1, 3, '2021-08-12 13:00:00', 'Пост3', 'Текст для поиска3', 'Анонс3', 0),
(4, true, 'ACCEPTED', 1, 1, '2021-08-12 13:05:00', 'Пост4', 'Текст4', 'Анонс4', 1),
(5, true, 'NEW', NULL, 3, '2021-08-13 13:00:00', 'Пост5', 'Текст5', 'Анонс5', 0),
(6, true, 'DECLINED', 1, 3, '2021-08-13 13:05:00', 'Пост6', 'Текст6', 'Анонс6', 0),
(7, false, 'ACCEPTED', 1, 3, '2021-08-13 13:05:00', 'Пост7', 'Текст7', 'Анонс7', 0);

-- post_comments

INSERT INTO post_comments (id, post_id, user_id, time, text)
VALUES (1, 2, 2, '2021-08-12 13:10:00', 'Комментарий1'),
(2, 2, 2, '2021-08-12 14:10:00', 'Комментарий2'),
(3, 3, 1, '2021-08-12 15:10:00', 'Комментарий3');

-- post_votes

INSERT INTO post_votes (id, user_id, post_id, time, value)
VALUES (1, 1, 2, '2021-08-12 13:20:00', -1),
(2, 2, 4, '2021-08-12 13:20:00', 1);

-- tags


INSERT INTO tags (id, name)
VALUES (1, 'Java'),
(2, 'Java Persistence API'),
(3, 'Spring'),
(4, 'JUnit');

-- tag2post

INSERT INTO tag2post (post_id, tag_id)
VALUES (2, 1),
(2, 2),
(3, 1),
(3, 2),
(3, 3),
(4, 1);



