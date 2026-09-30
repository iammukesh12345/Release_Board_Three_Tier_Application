CREATE DATABASE IF NOT EXISTS releaseboard;
USE releaseboard;

CREATE TABLE IF NOT EXISTS tasks (
  id    BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  done  BIT NOT NULL DEFAULT 0
);

INSERT INTO tasks (title, done) VALUES
  ('Merge feature branches to main', 1),
  ('Run smoke tests on staging', 0),
  ('Tag the release', 0);
