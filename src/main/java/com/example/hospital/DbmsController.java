package com.example.hospital;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
public class DbmsController {

    private final JdbcTemplate jdbc;

    public DbmsController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // =========================================================
    // MAIN DBMS DASHBOARD
    // =========================================================

    @GetMapping("/dbms")
    public String dbms(
            @RequestParam(required = false) Long patientId,
            Model model) {

        setupDbmsObjects(model);
        loadDashboardData(model);

        if (patientId != null) {
            loadProcedureResult(patientId, model);
        }

        return "dbms";
    }


    // =========================================================
    // PROCEDURE / FUNCTION / TRIGGER
    // =========================================================

    private void setupDbmsObjects(Model model) {

        try {

            // ---------------- PROCEDURE ----------------

            jdbc.execute(
                    "DROP PROCEDURE IF EXISTS GetPatientAppointments"
            );

            jdbc.execute("""
                CREATE PROCEDURE GetPatientAppointments(IN p_patient_id BIGINT)
                BEGIN
                    SELECT
                        a.id,
                        a.appointment_date,
                        d.name AS doctor,
                        a.status
                    FROM appointments a
                    JOIN doctors d
                        ON a.doctor_id = d.id
                    WHERE a.patient_id = p_patient_id
                    ORDER BY a.appointment_date;
                END
            """);


            // ---------------- FUNCTION ----------------

            jdbc.execute(
                    "DROP FUNCTION IF EXISTS CalculateDue"
            );

            jdbc.execute("""
                CREATE FUNCTION CalculateDue(
                    total_amt DECIMAL(12,2),
                    paid_amt DECIMAL(12,2)
                )
                RETURNS DECIMAL(12,2)
                DETERMINISTIC
                RETURN GREATEST(total_amt - paid_amt, 0)
            """);


            // ---------------- TRIGGER ----------------

            jdbc.execute(
                    "DROP TRIGGER IF EXISTS before_bill_insert"
            );

            jdbc.execute("""
                CREATE TRIGGER before_bill_insert
                BEFORE INSERT ON bills
                FOR EACH ROW
                BEGIN
                    IF NEW.paid_amount > NEW.total_amount THEN
                        SET NEW.paid_amount = NEW.total_amount;
                    END IF;
                END
            """);


            model.addAttribute(
                    "setupMessage",
                    "Procedure, Function and Trigger are ready."
            );

        } catch (Exception e) {

            model.addAttribute(
                    "setupError",
                    "DBMS object setup error: " + e.getMessage()
            );
        }
    }


    // =========================================================
    // PATIENT CRUD
    // =========================================================

    @PostMapping("/dbms/patient/insert")
    public String insertPatient(
            @RequestParam String name,
            @RequestParam Integer age,
            @RequestParam String gender,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam String address,
            @RequestParam String medicalHistory) {

        jdbc.update("""
            INSERT INTO patients
            (name, age, gender, phone, email, address, medical_history)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """,
                name,
                age,
                gender,
                phone,
                email,
                address,
                medicalHistory
        );

        return "redirect:/dbms";
    }


    @PostMapping("/dbms/patient/update")
    public String updatePatient(
            @RequestParam Long id,
            @RequestParam String name,
            @RequestParam Integer age,
            @RequestParam String gender,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam String address,
            @RequestParam String medicalHistory) {

        jdbc.update("""
            UPDATE patients
            SET name = ?,
                age = ?,
                gender = ?,
                phone = ?,
                email = ?,
                address = ?,
                medical_history = ?
            WHERE id = ?
        """,
                name,
                age,
                gender,
                phone,
                email,
                address,
                medicalHistory,
                id
        );

        return "redirect:/dbms";
    }


    @PostMapping("/dbms/patient/delete")
    public String deletePatient(@RequestParam Long id) {

        try {

            jdbc.update(
                    "DELETE FROM appointments WHERE patient_id = ?",
                    id
            );

            jdbc.update(
                    "DELETE FROM bills WHERE patient_id = ?",
                    id
            );

            jdbc.update(
                    "DELETE FROM patients WHERE id = ?",
                    id
            );

        } catch (Exception e) {
            // Ignore and return to dashboard.
        }

        return "redirect:/dbms";
    }


    // =========================================================
    // DOCTOR CRUD
    // =========================================================

    @PostMapping("/dbms/doctor/insert")
    public String insertDoctor(
            @RequestParam String name,
            @RequestParam String specialization,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam String roomNumber) {

        jdbc.update("""
            INSERT INTO doctors
            (name, specialization, phone, email, room_number)
            VALUES (?, ?, ?, ?, ?)
        """,
                name,
                specialization,
                phone,
                email,
                roomNumber
        );

        return "redirect:/dbms";
    }


    @PostMapping("/dbms/doctor/update")
    public String updateDoctor(
            @RequestParam Long id,
            @RequestParam String name,
            @RequestParam String specialization,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam String roomNumber) {

        jdbc.update("""
            UPDATE doctors
            SET name = ?,
                specialization = ?,
                phone = ?,
                email = ?,
                room_number = ?
            WHERE id = ?
        """,
                name,
                specialization,
                phone,
                email,
                roomNumber,
                id
        );

        return "redirect:/dbms";
    }


    @PostMapping("/dbms/doctor/delete")
    public String deleteDoctor(@RequestParam Long id) {

        try {

            jdbc.update(
                    "DELETE FROM appointments WHERE doctor_id = ?",
                    id
            );

            jdbc.update(
                    "DELETE FROM doctors WHERE id = ?",
                    id
            );

        } catch (Exception e) {
            // Ignore and return.
        }

        return "redirect:/dbms";
    }


    // =========================================================
    // APPOINTMENT CRUD
    // =========================================================

    @PostMapping("/dbms/appointment/insert")
    public String insertAppointment(
            @RequestParam Long patientId,
            @RequestParam Long doctorId,
            @RequestParam String appointmentDate,
            @RequestParam String status,
            @RequestParam String reason) {

        LocalDateTime dateTime =
                LocalDateTime.parse(appointmentDate);

        jdbc.update("""
            INSERT INTO appointments
            (patient_id, doctor_id, appointment_date, status, reason)
            VALUES (?, ?, ?, ?, ?)
        """,
                patientId,
                doctorId,
                Timestamp.valueOf(dateTime),
                status,
                reason
        );

        return "redirect:/dbms";
    }


    // =========================================================
    // FETCH APPOINTMENT BY ID
    // =========================================================

    @GetMapping("/dbms/appointment/get")
    @ResponseBody
    public Map<String, Object> getAppointment(
            @RequestParam Long id) {

        return jdbc.queryForMap("""
            SELECT
                id,
                patient_id,
                doctor_id,
                appointment_date,
                status,
                reason
            FROM appointments
            WHERE id = ?
        """, id);
    }


    // =========================================================
    // UPDATE APPOINTMENT
    // =========================================================

    @PostMapping("/dbms/appointment/update")
    public String updateAppointment(
            @RequestParam Long id,
            @RequestParam Long patientId,
            @RequestParam Long doctorId,
            @RequestParam String appointmentDate,
            @RequestParam String status,
            @RequestParam String reason) {

        LocalDateTime dateTime =
                LocalDateTime.parse(appointmentDate);

        jdbc.update("""
            UPDATE appointments
            SET patient_id = ?,
                doctor_id = ?,
                appointment_date = ?,
                status = ?,
                reason = ?
            WHERE id = ?
        """,
                patientId,
                doctorId,
                Timestamp.valueOf(dateTime),
                status,
                reason,
                id
        );

        return "redirect:/dbms";
    }


    @PostMapping("/dbms/appointment/delete")
    public String deleteAppointment(@RequestParam Long id) {

        jdbc.update(
                "DELETE FROM appointments WHERE id = ?",
                id
        );

        return "redirect:/dbms";
    }


    // =========================================================
    // BILL CRUD
    // =========================================================

    @PostMapping("/dbms/bill/insert")
    public String insertBill(
            @RequestParam Long patientId,
            @RequestParam BigDecimal totalAmount,
            @RequestParam BigDecimal paidAmount) {

        String status =
                calculateBillStatus(
                        totalAmount,
                        paidAmount
                );

        jdbc.update("""
            INSERT INTO bills
            (patient_id, total_amount, paid_amount, status, bill_date)
            VALUES (?, ?, ?, ?, NOW())
        """,
                patientId,
                totalAmount,
                paidAmount,
                status
        );

        return "redirect:/dbms";
    }


    // =========================================================
    // FETCH BILL BY ID
    // =========================================================

    @GetMapping("/dbms/bill/get")
    @ResponseBody
    public Map<String, Object> getBill(
            @RequestParam Long id) {

        return jdbc.queryForMap("""
            SELECT
                b.id,
                b.patient_id,
                p.name AS patient,
                b.total_amount,
                b.paid_amount,
                b.status
            FROM bills b
            JOIN patients p
                ON b.patient_id = p.id
            WHERE b.id = ?
        """, id);
    }


    // =========================================================
    // UPDATE BILL
    // =========================================================

    @PostMapping("/dbms/bill/update")
    public String updateBill(
            @RequestParam Long id,
            @RequestParam BigDecimal totalAmount,
            @RequestParam BigDecimal paidAmount) {

        String status =
                calculateBillStatus(
                        totalAmount,
                        paidAmount
                );

        jdbc.update("""
            UPDATE bills
            SET total_amount = ?,
                paid_amount = ?,
                status = ?
            WHERE id = ?
        """,
                totalAmount,
                paidAmount,
                status,
                id
        );

        return "redirect:/dbms";
    }


    @PostMapping("/dbms/bill/delete")
    public String deleteBill(@RequestParam Long id) {

        jdbc.update(
                "DELETE FROM bills WHERE id = ?",
                id
        );

        return "redirect:/dbms";
    }


    // =========================================================
    // BILL STATUS
    // =========================================================

    private String calculateBillStatus(
            BigDecimal total,
            BigDecimal paid) {

        if (paid.compareTo(BigDecimal.ZERO) == 0) {
            return "UNPAID";
        }

        if (paid.compareTo(total) >= 0) {
            return "PAID";
        }

        return "PARTIAL";
    }


    // =========================================================
    // STORED FUNCTION
    // =========================================================

    @GetMapping("/dbms/function")
    public String function(
            @RequestParam BigDecimal total,
            @RequestParam BigDecimal paid,
            Model model) {

        setupDbmsObjects(model);
        loadDashboardData(model);

        try {

            BigDecimal due =
                    jdbc.queryForObject(
                            "SELECT CalculateDue(?, ?)",
                            BigDecimal.class,
                            total,
                            paid
                    );

            model.addAttribute(
                    "functionTotal",
                    total
            );

            model.addAttribute(
                    "functionPaid",
                    paid
            );

            model.addAttribute(
                    "functionDue",
                    due
            );

        } catch (Exception e) {

            model.addAttribute(
                    "functionError",
                    "Function error: " + e.getMessage()
            );
        }

        return "dbms";
    }


    // =========================================================
    // TRIGGER
    // =========================================================

    @PostMapping("/dbms/trigger")
    public String triggerTest(
            @RequestParam Long patientId,
            @RequestParam BigDecimal total,
            @RequestParam BigDecimal paid,
            Model model) {

        try {

            String status =
                    calculateBillStatus(
                            total,
                            paid
                    );

            jdbc.update("""
                INSERT INTO bills
                (patient_id, total_amount, paid_amount, status, bill_date)
                VALUES (?, ?, ?, ?, NOW())
            """,
                    patientId,
                    total,
                    paid,
                    status
            );

            Map<String, Object> result =
                    jdbc.queryForMap("""
                        SELECT
                            id,
                            total_amount,
                            paid_amount,
                            status
                        FROM bills
                        WHERE patient_id = ?
                        ORDER BY id DESC
                        LIMIT 1
                    """,
                            patientId
                    );

            model.addAttribute(
                    "triggerResult",
                    result
            );

        } catch (Exception e) {

            model.addAttribute(
                    "triggerError",
                    "Trigger test failed: " + e.getMessage()
            );
        }

        loadDashboardData(model);

        return "dbms";
    }


    // =========================================================
    // STORED PROCEDURE
    // =========================================================

    private void loadProcedureResult(
            Long patientId,
            Model model) {

        try {

            List<Map<String, Object>> results =
                    jdbc.queryForList(
                            "CALL GetPatientAppointments(?)",
                            patientId
                    );

            model.addAttribute(
                    "procedureResults",
                    results
            );

            model.addAttribute(
                    "selectedPatientId",
                    patientId
            );

        } catch (Exception e) {

            model.addAttribute(
                    "procedureError",
                    "Procedure error: " + e.getMessage()
            );
        }
    }


    // =========================================================
    // LOAD DASHBOARD DATA
    // =========================================================

    private void loadDashboardData(Model model) {

        // ---------------- PATIENTS ----------------

        model.addAttribute(
                "patients",
                jdbc.queryForList("""
                    SELECT
                        id,
                        name,
                        age,
                        gender,
                        phone,
                        email,
                        address,
                        medical_history
                    FROM patients
                    ORDER BY id DESC
                """)
        );


        // ---------------- DOCTORS ----------------

        model.addAttribute(
                "doctors",
                jdbc.queryForList("""
                    SELECT
                        id,
                        name,
                        specialization,
                        phone,
                        email,
                        room_number
                    FROM doctors
                    ORDER BY id DESC
                """)
        );


        // ---------------- APPOINTMENTS ----------------

        model.addAttribute(
                "appointments",
                jdbc.queryForList("""
                    SELECT
                        a.id,
                        a.patient_id,
                        a.doctor_id,
                        p.name AS patient,
                        d.name AS doctor,
                        a.appointment_date,
                        a.status,
                        a.reason
                    FROM appointments a
                    JOIN patients p
                        ON a.patient_id = p.id
                    JOIN doctors d
                        ON a.doctor_id = d.id
                    ORDER BY a.appointment_date DESC
                """)
        );


        // ---------------- BILLS ----------------

        model.addAttribute(
                "bills",
                jdbc.queryForList("""
                    SELECT
                        b.id,
                        b.patient_id,
                        p.name AS patient,
                        b.total_amount,
                        b.paid_amount,
                        b.status,
                        b.bill_date
                    FROM bills b
                    JOIN patients p
                        ON b.patient_id = p.id
                    ORDER BY b.id DESC
                """)
        );


        // ---------------- JOIN ----------------

        model.addAttribute(
                "joinResults",
                jdbc.queryForList("""
                    SELECT
                        p.name AS patient,
                        d.name AS doctor,
                        a.appointment_date,
                        a.status
                    FROM appointments a
                    JOIN patients p
                        ON a.patient_id = p.id
                    JOIN doctors d
                        ON a.doctor_id = d.id
                    ORDER BY a.appointment_date DESC
                """)
        );


        // ---------------- GROUP BY ----------------

        model.addAttribute(
                "groupResults",
                jdbc.queryForList("""
                    SELECT
                        status,
                        COUNT(*) AS total
                    FROM appointments
                    GROUP BY status
                """)
        );


        // ---------------- TOTAL BILLING ----------------

        BigDecimal totalBilled =
                jdbc.queryForObject(
                        "SELECT COALESCE(SUM(total_amount), 0) FROM bills",
                        BigDecimal.class
                );

        model.addAttribute(
                "totalBilled",
                totalBilled
        );
    }
}