# Hospital Management System - Mini Project

A small, deployable Hospital Management System for a DBMS lab/mini project.

## Technology
- Java 21
- Spring Boot
- Spring Data JPA / Hibernate
- MySQL 8
- Thymeleaf
- HTML/CSS/Bootstrap-style custom CSS

## Modules
1. Dashboard
2. Patient Management
3. Doctor Management
4. Appointment Management
5. Billing

## Database tables
- patients
- doctors
- appointments
- bills

## Run locally

### 1. Requirements
Install:
- JDK 21
- Maven 3.9+
- MySQL 8

### 2. Create database
Run in MySQL:
```sql
CREATE DATABASE hospital_db;
```

### 3. Configure credentials
Edit:
`src/main/resources/application.properties`

Default values are:
- username: root
- password: root

Change the password if your MySQL installation uses another password.

### 4. Start the application
From the project folder:
```bash
mvn spring-boot:run
```

Open:
http://localhost:8080

The tables are created automatically by Hibernate.

## Useful DBMS SQL
See:
`src/main/resources/db/dbms_queries.sql`

It contains DDL, DML, joins, aggregate queries, a stored procedure, function and trigger examples.

## GitHub
After testing:
```bash
git init
git add .
git commit -m "Initial Hospital Management System"
git branch -M main
git remote add origin YOUR_GITHUB_REPOSITORY_URL
git push -u origin main
```

## Deployment
The application is prepared for cloud deployment using environment variables:
- DB_URL
- DB_USERNAME
- DB_PASSWORD
- PORT

Railway is one possible hosting platform. You can deploy the GitHub repository there and attach a MySQL database.
