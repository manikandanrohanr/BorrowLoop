CREATE DATABASE IF NOT EXISTS borrowloop;
USE borrowloop;

-- Table: students
CREATE TABLE IF NOT EXISTS students (
    reg_no      VARCHAR(20)  PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(100),
    phone       VARCHAR(15)
);

-- Table: resources
CREATE TABLE IF NOT EXISTS resources (
    resource_id     INT          AUTO_INCREMENT PRIMARY KEY,
    resource_name   VARCHAR(100) NOT NULL,
    category        VARCHAR(50)  NOT NULL,
    total_qty       INT          NOT NULL DEFAULT 1,
    available_qty   INT          NOT NULL DEFAULT 1
);

-- Table: borrow_records
CREATE TABLE IF NOT EXISTS borrow_records (
    record_id       INT         AUTO_INCREMENT PRIMARY KEY,
    reg_no          VARCHAR(20) NOT NULL,
    resource_id     INT         NOT NULL,
    borrow_date     DATE        NOT NULL,
    due_date        DATE        NOT NULL,
    return_date     DATE        DEFAULT NULL,
    FOREIGN KEY (reg_no)      REFERENCES students(reg_no),
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id)
);