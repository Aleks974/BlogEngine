CREATE TABLE IF NOT EXISTS global_settings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code ENUM('MULTIUSER_MODE', 'POST_PREMODERATION', 'STATISTICS_IS_PUBLIC') NOT NULL,
    name VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL,
PRIMARY KEY (id),
UNIQUE (code));

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    is_moderator BIT(1) NOT NULL,
    reg_time DATETIME NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    code VARCHAR(255),
    photo TEXT,
PRIMARY KEY (id));

CREATE TABLE IF NOT EXISTS posts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    is_active BIT(1) NOT NULL,
    moderation_status ENUM('NEW', 'ACCEPTED', 'DECLINED') NOT NULL,
    moderator_id BIGINT,
    user_id BIGINT NOT NULL,
    time DATETIME NOT NULL,
    title VARCHAR(255) NOT NULL,
    text TEXT NOT NULL,
    view_count INTEGER NOT NULL,
PRIMARY KEY (id));

CREATE TABLE IF NOT EXISTS post_comments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    parent_id BIGINT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    time DATETIME NOT NULL,
    text TEXT NOT NULL,
PRIMARY KEY (id));

CREATE TABLE IF NOT EXISTS post_votes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    time DATETIME NOT NULL,
    value TINYINT NOT NULL,
PRIMARY KEY (id));

CREATE TABLE IF NOT EXISTS tags (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
PRIMARY KEY (id));

CREATE TABLE IF NOT EXISTS tag2post (
    post_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
PRIMARY KEY (post_id, tag_id));

CREATE TABLE IF NOT EXISTS captcha_codes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code TINYTEXT NOT NULL,
    secret_code TINYTEXT NOT NULL,
    time DATETIME(6) NOT NULL,
PRIMARY KEY (id));
