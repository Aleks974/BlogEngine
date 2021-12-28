# BlogEngine

<a href="https://blogengine-skillbox.herokuapp.com" target="_blank"><img src="https://blogengine-skillbox.herokuapp.com/img/screen.png" width="100%" /></a>

<h2 align="center"><a href="https://blogengine-skillbox.herokuapp.com"  target="_blank">Live Demo</a></h2>

## Description
The blog's backend was developed as a part of my diplom work after completing a Java course at the Skillbox educational company. This Spring Boot application uses REST API points to process frontend requests and provides responses in JSON format. Flyway library was used as database migration version control tool. The number of SQL queries and amount of fetched data from DB have been optimized to improve perfomance. This was achived by using DTO projections in repositories and the correct fetch type strategy in associations, as well as the use of caching SQL queries.

## Used technologies & frameworks
Spring Framework: Boot, Web, Data JPA, Security.<br />
Hibernate, Junit, Lombok, Log4j2, Flyway<br />
MySQL.

## How To
To start app, you need a database and correct settings of datasource, timezone, app url, email address and password to send emails. You can do it by editing application.yml or by setting environment variables:  APP_URL, DB_URL, GMAIL_USERNAME, GMAIL_APPPASS, SERVER_APP_AND_DB_TIMEZONE
