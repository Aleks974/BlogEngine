-- roles

INSERT INTO roles (name)
VALUES ('ROLE_ADMIN'),
('ROLE_USER');

-- users

INSERT INTO users (id, is_moderator, reg_time, name, email, password)
VALUES (1, true, '2021-08-10 12:00:00', 'moderator', 'admin@blogengine.ru', 'pyLGPbjshiWvbPccuMLZOQ=='),
(2, false, '2021-08-10 13:00:00', 'user1', 'user1@blogengine.ru', 'wVctBUJNDssqZexqgq6svw=='),
(3, false, '2021-08-10 14:00:00', 'user2', 'user2@blogengine.ru', 'Ovx5tZf4inJSjoZM+BhW0g==');

-- users_roles

INSERT INTO users_roles (user_id, role_id)
VALUES (1, 1),
(2, 2),
(3, 2);


-- posts

INSERT INTO posts (id, is_active, moderation_status, moderator_id, user_id, time, title, text, announce, view_count)
VALUES (1, true, 'NEW', 1, 2, '2021-08-10 12:00:00', 'Пост1', 'Текст1', 'Анонс1', 0),
(2, true, 'ACCEPTED', 1, 3, '2021-08-11 13:00:00', 'Пост2', 'Текст для поиска2', 'Анонс2', 1),
(3, true, 'ACCEPTED', 1, 3, '2021-08-12 13:00:00', 'Пост3', 'Текст для поиска3', 'Анонс3', 0),
(4, true, 'ACCEPTED', 1, 1, '2021-08-12 13:05:00', 'Пост4', 'Текст4', 'Анонс4', 0);

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



