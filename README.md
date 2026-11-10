# BorrowLoop

BorrowLoop is a Java Swing desktop application for managing shared resources in a school, library, or office environment. It allows users to add resources, track availability, register students, borrow assets, return them, and monitor overdue records through a clean dashboard.

The project uses MySQL as the database backend and Java Swing for the desktop interface. The application is intended for a resource-sharing workflow where each item has a total quantity and an available quantity that changes as items are borrowed and returned.

## Overview

BorrowLoop helps administrators manage:

- Resource inventory and categories
- Borrowing and returning items
- Student registration and contact tracking
- Borrowing history and overdue reports
- Live dashboard summaries from the database

At a high level, the project includes:

- A Swing UI for browsing and managing records
- A MySQL database schema for students, resources, and borrow history
- JDBC-based data access using prepared statements
- Thread-safe borrow processing to avoid race conditions when multiple users attempt to borrow the same item at the same time

## Features

### Dashboard

The main dashboard shows real-time statistics such as:

- Total resources in the system
- Available resources across all categories
- Borrowed resources currently out
- Overdue records

These values are read directly from the MySQL database and refreshed when the dashboard is opened or refreshed.

### Add Resource

Users can add a new resource to the database with:

- Resource name
- Category
- Quantity

When a new resource is added, both the total quantity and the available quantity are initialized to the same value.

### View Resources

This section displays all resources currently stored in the database in a table layout. The table includes:

- Resource ID
- Name
- Category
- Total quantity
- Available quantity

### Search Resource

Users can search for resources by name using a SQL LIKE query. This helps quickly locate items in a larger inventory.

### Borrow Resource

The borrow flow allows a user to:

- Enter the student's registration number
- Register a new student if needed
- Select a resource from the current inventory
- Set a borrow date and due date

A borrow operation:

1. Checks whether the student already exists
2. Registers the student automatically if they are new
3. Validates the resource availability
4. Inserts a record into `borrow_records`
5. Decrements `available_qty` for the selected resource

Borrow processing is protected using a shared synchronized lock so that two threads cannot simultaneously borrow the last remaining item.

### Return Resource

Users can mark an item as returned by entering the borrowing record ID and the return date. The system then:

- Marks the return date on the borrow record
- Increases the resource's available quantity

### Borrowing History

The history view shows records for all borrow transactions. It includes:

- Record ID
- Registration number
- Student name
- Resource name
- Borrow date
- Due date
- Return status

### Overdue Items

The overdue view displays any records that are still outstanding and whose due date is before today. It includes the number of days late for each record.

## Tech Stack

- Java SE
- Swing (desktop GUI)
- MySQL database
- JDBC (Java Database Connectivity)
- MySQL Connector/J driver

## Project Structure

```text
BorrowLoop with UI/
├── BorrowManager.java
├── BorrowTask.java
├── DatabaseConnection.java
├── Main.java
├── MainFrame.java
├── ResourceManager.java
├── UITheme.java
├── database.sql
├── mysql-connector-j-8.4.0.jar
├── BorrowManager.class
├── BorrowTask.class
├── DatabaseConnection.class
├── Main.class
├── MainFrame.class
├── ResourceManager.class
├── UITheme.class
├── .gitignore
└── README.md
```

## Database Design

The database schema is defined in [database.sql](database.sql).

### 1. `students`

Stores student information.

```sql
CREATE TABLE IF NOT EXISTS students (
    reg_no      VARCHAR(20)  PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(100),
    phone       VARCHAR(15)
);
```

### 2. `resources`

Stores inventory details.

```sql
CREATE TABLE IF NOT EXISTS resources (
    resource_id     INT          AUTO_INCREMENT PRIMARY KEY,
    resource_name   VARCHAR(100) NOT NULL,
    category        VARCHAR(50)  NOT NULL,
    total_qty       INT          NOT NULL DEFAULT 1,
    available_qty   INT          NOT NULL DEFAULT 1
);
```

### 3. `borrow_records`

Stores each borrow transaction.

```sql
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
```

## Database Connection Configuration

The database connection is created in [DatabaseConnection.java](DatabaseConnection.java).

By default, the project attempts to connect to:

- URL: `jdbc:mysql://localhost:3306/borrowloop`
- Username: `root`
- Password: `root`

These values can be overridden using environment variables:

```bash
BORROWLOOP_DB_URL
BORROWLOOP_DB_USER
BORROWLOOP_DB_PASSWORD
```

Example:

```bash
set BORROWLOOP_DB_URL=jdbc:mysql://localhost:3306/borrowloop
set BORROWLOOP_DB_USER=root
set BORROWLOOP_DB_PASSWORD=your_password
```

## Setup Instructions

### Prerequisites

Before running the application, make sure you have:

- Java JDK installed
- MySQL server installed and running
- MySQL Connector/J JAR available in the project folder
- A database named `borrowloop`

### Step 1: Create the database

Open MySQL and run the SQL script from [database.sql](database.sql):

```sql
SOURCE database.sql;
```

Or paste the file contents into MySQL Workbench or the MySQL command line client.

### Step 2: Start MySQL

Ensure the MySQL service is running locally.

### Step 3: Compile the project

From the project directory, run:

```bash
javac *.java
```

### Step 4: Run the application

```bash
java -cp ".;mysql-connector-j-8.4.0.jar" Main
```

On Linux or macOS, use:

```bash
java -cp ".:mysql-connector-j-8.4.0.jar" Main
```

If the MySQL connector is not included in the classpath, the app will fail to connect to the database and display an error dialog.

## How the Application Works

### Main Entry Point

The application starts in [Main.java](Main.java). It:

- Sets the system look and feel
- Creates a database connection
- Creates the `ResourceManager` and `BorrowManager`
- Opens the main `MainFrame`

### UI Layer

The desktop interface is built in [MainFrame.java](MainFrame.java). It uses Swing panels, cards, forms, and tables to implement the different screens:

- Dashboard
- Add Resource
- View Resources
- Search Resource
- Borrow Resource
- Return Resource
- Borrowing History
- Overdue Items

### Business Logic Layer

The application logic is separated into:

- [ResourceManager.java](ResourceManager.java): handles resource creation, retrieval, and totals
- [BorrowManager.java](BorrowManager.java): handles student registration, borrow processing, returns, and reports
- [BorrowTask.java](BorrowTask.java): thread-based borrow processor using synchronization

### Styling

The custom theme and visual styling are defined in [UITheme.java](UITheme.java). This includes the sidebar, buttons, table headers, and card components.

## Important Business Rules

The project includes a few application rules that are worth knowing:

- A resource must have a valid name and category before it can be added
- Quantity must be at least 1
- Students are auto-registered when borrowing with a new registration number
- A resource cannot be borrowed when `available_qty <= 0`
- Borrowing is synchronized to prevent race conditions
- Returning an item increases the available quantity by 1
- Overdue records are identified using `due_date < CURDATE()` and `return_date IS NULL`

## Example Workflow

1. Start MySQL and create the database using [database.sql](database.sql)
2. Launch the app using `java -cp ... Main`
3. Add a few resources like:
   - Whiteboard
   - Projector
   - Laptop
4. Borrow a resource by entering a registration number and choosing the item
5. View the borrowing history
6. Return the item using its borrow record ID
7. Check the dashboard totals and overdue list

## Troubleshooting

### MySQL driver not found

If you see an error about the MySQL driver, make sure the Connector/J JAR is present and included in the classpath.

```bash
java -cp ".;mysql-connector-j-8.4.0.jar" Main
```

### Database connection failed

Check:

- Whether MySQL is running
- The database name is `borrowloop`
- The username/password are correct
- The `BORROWLOOP_DB_*` environment variables are set correctly

### Resource not found or unavailable

This usually means:

- The resource ID is invalid
- The selected item has zero stock available
- The resource was never added to the database

### UI does not open

Ensure Java is installed and the class files were compiled successfully with:

```bash
javac *.java
```

## Design Notes

This project demonstrates several software engineering concepts:

- JDBC prepared statements for safe database queries
- Java Swing-based GUI development
- Synchronization and multithreading for concurrency control
- Separation of concerns between UI, business logic, and database access
- Real-time dashboard reporting using database aggregation queries

## Security and Validations

The application uses prepared statements in queries to reduce the risk of SQL injection and to safely pass input values into database statements.

The borrower thread uses a shared lock to guard the critical borrow check/update sequence and prevent inventory inconsistencies during concurrent borrowing.

## Future Enhancements

Possible improvements include:

- User authentication and admin roles
- Better validation for dates and student records
- Search by category or student name
- PDF or Excel export for history
- Advanced overdue notifications
- Better data filtering and pagination

## Notes

This project is designed as a desktop resource-management system and is well-suited for student projects, academic labs, or internal inventory tracking demos.

## License

No explicit license file is included in the project at this time. The project is intended for educational and demonstration use unless otherwise stated by the repository owner.
