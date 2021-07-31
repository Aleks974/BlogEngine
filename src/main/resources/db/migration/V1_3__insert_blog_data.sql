-- users

INSERT INTO users (id, is_moderator, reg_time, name, email, password)
VALUES (1, true, DATE_SUB(NOW(), INTERVAL 4 DAY), "moder", "admin@blogengine.ru", "pyLGPbjshiWvbPccuMLZOQ=="),
(2, false, DATE_SUB(NOW(), INTERVAL 3 DAY), "user1", "user1@blogengine.ru", "wVctBUJNDssqZexqgq6svw=="),
(3, false, DATE_SUB(NOW(), INTERVAL 2 DAY), "user2", "user2@blogengine.ru", "Ovx5tZf4inJSjoZM+BhW0g==");

-- posts

INSERT INTO posts (id, is_active, moderation_status, moderator_id, user_id, time, title, text, view_count)
VALUES (1, true, "NEW", 1, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), "Заголовок поста1", "Текст поста1. Просто текст1", 0),
(2, true, "ACCEPTED", 1, 3, DATE_SUB(NOW(), INTERVAL 2 DAY), "Заголовок поста2", "Текст поста2. Просто текст2", 0),
(3, true, "ACCEPTED", 1, 3, NOW(), "Заголовок поста3", "Текст поста3. Просто текст3", 0);

-- post_comments

INSERT INTO post_comments (id, post_id, user_id, time, text)
VALUES (1, 2, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), "Комментарий 1"),
(2, 3, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), "Комментарий 2");

-- post_votes

INSERT INTO post_votes (id, user_id, post_id, time, value)
VALUES (1, 3, 2, DATE_SUB(NOW(), INTERVAL 2 DAY), 1);

-- tags

INSERT INTO tags (id, name)
VALUES (1, "тэг1"),
(2, "тэг2");

-- tag2post

INSERT INTO tag2post (post_id, tag_id)
VALUES (2, 1),
(3, 1),
(3, 2);