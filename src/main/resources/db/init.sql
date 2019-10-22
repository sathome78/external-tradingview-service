CREATE TABLE USER
(
  id           INT(40) UNSIGNED PRIMARY KEY   NOT NULL AUTO_INCREMENT,
  login        VARCHAR(25) UNIQUE             NOT NULL,
  password     VARCHAR(80)                    NOT NULL,
  phone        VARCHAR(15) UNIQUE             NOT NULL,
  created_date TIMESTAMP                      NOT NULL,
  2fa_code     VARCHAR(80)
)
  CHARACTER SET utf8
  COLLATE utf8_general_ci;