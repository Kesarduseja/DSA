# Quiz Database Manager - DBMS Mini Project

## Project Overview
This is a Java Swing application designed to connect to a Microsoft SQL Server database (`your_quiz_db`) and manage its core entities. It provides CRUD functionality and advanced reporting features using JDBC.

## Prerequisites
1. Java JDK 17.0.8 or later.
2. Microsoft SQL Server [Specify Version] instance running on `localhost:1433`.
3. Microsoft JDBC Driver for SQL Server (JAR file).

## Setup Instructions

### 1. Database Setup
1. Execute the DDL statements from `data/sample_data.sql` in your SQL Server instance to create the schema and populate the tables.
2. Ensure you have a valid SQL Server login user (`DB_USER`, `DB_PASSWORD`) with access to the database.

### 2. Application Setup
1. Place the `mssql-jdbc-[version].jar` file in a `lib` directory inside the project root.
2. **Crucially, edit `code/QuizDBViewer.java`** and replace the placeholders for `DB_URL`, `DB_USER`, and `DB_PASSWORD` with your actual credentials.

### 3. Compile and Run
Open your terminal in the project root directory.

```bash
# Assuming the JDBC driver is in lib/
# 1. Compile
javac -cp ".;lib/mssql-jdbc-[version].jar" code/QuizDBViewer.java

Features
Load Data: View data from any of the configured tables (Student, Admin, Quiz, etc.).
CRUD Operations: Buttons are provided for Add, Update, and Delete (functionality is stubbed in the provided code).
Reports:
Student Performance Report (by Student ID)
Quiz Summary Report (Aggregate scores by Quiz ID)
High Scorers Report (Students above a custom percentage)
Admin Quiz Count (Number of quizzes created by each administrator)

# 2. Run
java -cp ".;lib/mssql-jdbc-[version].jar:code" QuizDBViewer
