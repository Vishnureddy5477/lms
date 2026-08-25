-- ─────────────────────────────────────────────────────────────
--  Run this ONCE in MySQL before starting the backend.
--    mysql -u root -p < db/init.sql
--  (or paste it into MySQL Workbench)
--
--  You only need to create the DATABASE. Hibernate creates the
--  tables automatically from the @Entity classes, because
--  application.properties has spring.jpa.hibernate.ddl-auto=update
-- ─────────────────────────────────────────────────────────────

CREATE DATABASE IF NOT EXISTS intern_template
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE intern_template;

-- The `students` table below is created automatically by Hibernate.
-- It is shown here only so you can see the shape of the data.
--
-- CREATE TABLE students (
--   id       BIGINT AUTO_INCREMENT PRIMARY KEY,
--   name     VARCHAR(100) NOT NULL,
--   email    VARCHAR(150) NOT NULL,
--   course   VARCHAR(100),
--   progress INT,
--   status   VARCHAR(30)
-- );
