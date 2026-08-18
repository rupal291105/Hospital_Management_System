-- BCS-551 DBMS PRACTICAL QUERIES
-- Run these after the application has created the tables.

-- DDL
CREATE DATABASE IF NOT EXISTS hospital_db;

-- DML examples
INSERT INTO patients (name, age, gender, phone, email, address, medical_history)
VALUES ('Demo Patient', 25, 'Female', '9999999999', 'demo@example.com', 'Lucknow', 'None');

INSERT INTO doctors (name, specialization, phone, email, room_number)
VALUES ('Dr. Demo', 'General Medicine', '8888888888', 'doctor@example.com', '101');

UPDATE patients SET age = 26 WHERE name = 'Demo Patient';

DELETE FROM patients WHERE email = 'demo@example.com';

-- SELECT / RESTRICTION / SORTING
SELECT * FROM patients;
SELECT * FROM patients WHERE age >= 18;
SELECT * FROM doctors ORDER BY name;

-- JOIN
SELECT p.name AS patient, d.name AS doctor, a.appointment_date, a.status
FROM appointments a
JOIN patients p ON a.patient_id = p.id
JOIN doctors d ON a.doctor_id = d.id;

-- AGGREGATE / GROUP BY
SELECT status, COUNT(*) AS total
FROM appointments
GROUP BY status;

SELECT SUM(total_amount) AS total_billed
FROM bills;

-- Stored procedure (MySQL)
DELIMITER //
CREATE PROCEDURE GetPatientAppointments(IN p_patient_id BIGINT)
BEGIN
    SELECT a.id, a.appointment_date, d.name AS doctor, a.status
    FROM appointments a
    JOIN doctors d ON a.doctor_id = d.id
    WHERE a.patient_id = p_patient_id
    ORDER BY a.appointment_date;
END //
DELIMITER ;

-- Stored function
DELIMITER //
CREATE FUNCTION CalculateDue(total_amt DECIMAL(10,2), paid_amt DECIMAL(10,2))
RETURNS DECIMAL(10,2)
DETERMINISTIC
RETURN GREATEST(total_amt - paid_amt, 0);
//
DELIMITER ;

-- Trigger: keep paid amount from becoming greater than total
DELIMITER //
CREATE TRIGGER before_bill_insert
BEFORE INSERT ON bills
FOR EACH ROW
BEGIN
    IF NEW.paid_amount > NEW.total_amount THEN
        SET NEW.paid_amount = NEW.total_amount;
    END IF;
END //
DELIMITER ;

-- Note:
-- MySQL does not support Oracle-style PACKAGES.
-- Spring Boot services + MySQL procedures/functions are used as the practical equivalent.
