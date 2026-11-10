# BorrowLoop – Resource Sharing System

BorrowLoop is a desktop-based resource sharing application built with **Java Swing** and **MySQL**. It provides a single interface to manage shared resources, track borrowing and returns, and monitor overdue records.

## Features

- **Dashboard** – View the total number of resources, available resources, borrowed resources, and overdue records.
- **Add Resource** – Register a resource in the system.
- **View Resources** – Browse the resources stored in the database.
- **Search Resource** – Find a resource using the application's search screen.
- **Borrow Resource** – Record when a resource is borrowed.
- **Return Resource** – Record a resource return and update its availability.
- **Borrowing History** – Review borrowing records.
- **Overdue Items** – View records that are overdue.

## Technology Stack

- **Language:** Java
- **Desktop UI:** Java Swing
- **Database:** MySQL
- **Database connectivity:** JDBC with MySQL Connector/J

## Prerequisites

Before running the project, make sure you have:

- A Java Development Kit (JDK) installed.
- MySQL Server installed and running.
- A MySQL client such as MySQL Workbench or the MySQL command-line client.
- MySQL Connector/J added to the project's classpath (the project folder appears to include a Connector/J JAR).
- An IDE such as IntelliJ IDEA, Eclipse, or Visual Studio Code with Java support, or the ability to compile and run Java from a terminal.

## Getting Started

### 1. Clone the repository

```bash
git clone <your-repository-url>
cd <your-repository-folder>
```

### 2. Create the database

1. Start MySQL Server.
2. Open MySQL Workbench or a MySQL terminal.
3. Open and execute the SQL statements in `database.sql`.
4. Confirm that the database and tables have been created successfully.

> The database name and table definitions should be taken from `database.sql`. This README does not assume their names because they are not visible in the project screenshot.

### 3. Configure the database connection

Open `DatabaseConnection.java` and check the JDBC connection settings. Update the connection URL, database name, username, and password to match your local MySQL setup.

For example, a local MySQL JDBC URL generally has this format:

```text
jdbc:mysql://localhost:3306/<database-name>
```

Do not commit real database passwords or other credentials to a public repository. Prefer environment variables or a local configuration file that is excluded from version control.

### 4. Add the MySQL JDBC driver

Ensure MySQL Connector/J is included in the project's classpath or build path. If you use an IDE, add the Connector/J JAR as a project library. If the project already contains a Connector/J JAR, verify that the configured path points to that file.

### 5. Run the application

Open the project in your Java IDE, ensure the MySQL driver is available, and run `Main.java` (or the project's configured main class).

The application should open the BorrowLoop desktop interface. Use the navigation panel to access the resource and borrowing features.

## Project Structure

The following names are visible in the current project and indicate the main areas of the application. Exact package paths may differ:

```text
BorrowLoop/
├── Main.java                  # Application entry point
├── MainFrame.java             # Main application window
├── DatabaseConnection.java    # MySQL/JDBC connection setup
├── ResourceManager.java       # Resource-related operations
├── BorrowManager.java         # Borrowing and return operations
├── BorrowTask.java            # Borrowing task/model support
├── database.sql               # Database setup script
├── UITheme.java                # UI styling/theme
└── mysql-connector-j-*.jar     # MySQL JDBC driver (if included)
```

Additional UI component classes and compiled `.class` files may also be present. The `.java` source files are the ones to maintain; compiled `.class` files are generated when the project is built.

## Usage

1. Launch BorrowLoop.
2. Open **Add Resource** to register resources.
3. Use **View Resources** or **Search Resource** to find resources.
4. Use **Borrow Resource** to record a borrowing transaction.
5. Use **Return Resource** when the resource is returned.
6. Check **Borrowing History** and **Overdue Items** to review borrowing activity.
7. Return to **Dashboard** and refresh it to see updated totals.

## Troubleshooting

- **Unable to connect to MySQL:** Make sure MySQL Server is running and verify the JDBC URL, database name, username, and password in `DatabaseConnection.java`.
- **Driver not found:** Add the MySQL Connector/J JAR to the IDE's build path or the runtime classpath.
- **Table or database not found:** Run `database.sql` and check that the connection points to the database created by that script.
- **Application does not launch:** Confirm that the JDK is installed, the project builds without errors, and `Main.java` is configured as the entry point.

## Future Improvements

- Add user authentication and role-based access.
- Add input validation and clearer error messages.
- Provide reports or exports for resource and borrowing activity.
- Add automated tests for resource, borrowing, and return operations.

## Contributing

Contributions and suggestions are welcome. Create a branch for your changes, make sure the project builds, and submit a pull request with a clear description of the update.

## License

No license has been specified yet. Add a `LICENSE` file before distributing or reusing this project under an open-source license.
