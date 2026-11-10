# BorrowLoop

BorrowLoop is a Java Swing application for managing shared resources and student borrow records. It uses MySQL for storage.

## Requirements

- Java 8 or newer
- MySQL

## Database setup

Run [`database.sql`](database.sql) in MySQL to create the `borrowloop` database and its tables.

The connection defaults to `jdbc:mysql://localhost:3306/borrowloop` and the MySQL user `root`. Set these environment variables to use different connection settings:

- `BORROWLOOP_DB_URL`
- `BORROWLOOP_DB_USER`
- `BORROWLOOP_DB_PASSWORD`

For example, in PowerShell:

```powershell
$env:BORROWLOOP_DB_USER = "root"
$env:BORROWLOOP_DB_PASSWORD = "your-mysql-password"
```

## Compile and run

The MySQL Connector/J driver is included in this repository. From the project directory, run:

```powershell
javac -cp ".;mysql-connector-j-8.4.0.jar" *.java
java -cp ".;mysql-connector-j-8.4.0.jar" Main
```
