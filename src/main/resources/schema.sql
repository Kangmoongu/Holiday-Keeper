CREATE TABLE IF NOT EXISTS tbl_countries
(
    id              UUID            PRIMARY KEY,
    country_code    VARCHAR(10)     NOT NULL,
    name            VARCHAR(100)    NOT NULL
);


CREATE TABLE IF NOT EXISTS tbl_holidays
(
    id              UUID            PRIMARY KEY,
    date            DATE            NOT NULL,
    local_name      VARCHAR(500)    NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    country_id      UUID            NOT NULL,
    fixed           BOOLEAN         NOT NULL,
    global          BOOLEAN         NOT NULL,
    counties        TEXT            ,
    launch_year     INT             ,
    types           TEXT            ,
    CONSTRAINT fk_holiday_country FOREIGN KEY (country_id) REFERENCES tbl_countries(id) ON DELETE CASCADE
);