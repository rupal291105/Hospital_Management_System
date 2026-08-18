package com.example.hospital;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
public class DbmsController {

    private final JdbcTemplate jdbc;

    public DbmsController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // =========================
    // DBMS DASHBOARD
    // =========================
    @GetMapping("/dbms")
    public String dbms(
            @RequestParam(required = false) Long patientId,
            Model model) {

        setupDbmsObjects(model);

        // READ - all patients
        model.addAttribute(
                "patients",
                jdbc.queryForList(
                        "SELECT id, name, age, gender, phone, email, address " +
                        "FROM patients ORDER BY id DESC"
                )
        );

        // JOIN
        model.addAttribute(
                "joinResults",
                jdbc.queryForList(
                        "SELECT p.name AS patient, " +
                        "d.name AS doctor, " +
                        "a.appointment_date, " +
                        "a.status " +
                        "FROM appointments a " +
                        "JOIN patients p ON a.patient_id = p.id " +
                        "JOIN doctors d ON a.doctor_id = d.id " +
                        "ORDER BY a.appointment_date DESC"
                )
        );

        // GROUP BY
        model.addAttribute(
                "groupResults",
                jdbc.queryForList(
                        "SELECT status, COUNT(*) AS total " +
                        "FROM appointments GROUP BY status"
                )
        );

        // AGGREGATE
        BigDecimal totalBilled = jdbc.queryForObject(
                "SELECT COALESCE(SUM(total_amount),0) FROM bills",
                BigDecimal.class
        );

        model.addAttribute("totalBilled", totalBilled);

        // STORED PROCEDURE
        if (patientId != null) {
            try {
                List<Map<String, Object>> procedureResults =
                        jdbc.queryForList(
                                "CALL GetPatientAppointments(?)",
                                patientId
                        );

                model.addAttribute(
                        "procedureResults",
                        procedureResults
                );

                model.addAttribute("selectedPatientId", patientId);

            } catch (Exception e) {
                model.addAttribute(
                        "procedureError",
                        "Procedure error: " + e.getMessage()
                );
            }
        }

        return "dbms";
    }

    // =========================
    // SETUP PROCEDURE / FUNCTION / TRIGGER
    // =========================
    private void setupDbmsObjects(Model model) {

        try {

            // PROCEDURE
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
                    JOIN doctors d ON a.doctor_id = d.id
                    WHERE a.patient_id = p_patient_id
                    ORDER BY a.appointment_date;
                END
            """);

            // FUNCTION
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

            // TRIGGER
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

    // =========================
    // CREATE / INSERT PATIENT
    // =========================
    @PostMapping("/dbms/patient/insert")
    public String insertPatient(
            @RequestParam String name,
            @RequestParam Integer age,
            @RequestParam String gender,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam String address,
            @RequestParam String medicalHistory) {

        jdbc.update(
                """
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

    // =========================
    // UPDATE PATIENT
    // =========================
    @PostMapping("/dbms/patient/update")
    public String updatePatient(
            @RequestParam Long id,
            @RequestParam Integer age,
            @RequestParam String phone,
            @RequestParam String address) {

        jdbc.update(
                """
                UPDATE patients
                SET age = ?, phone = ?, address = ?
                WHERE id = ?
                """,
                age,
                phone,
                address,
                id
        );

        return "redirect:/dbms";
    }

    // =========================
    // DELETE PATIENT
    // =========================
    @PostMapping("/dbms/patient/delete")
    public String deletePatient(
            @RequestParam Long id) {

        try {

            jdbc.update(
                    "DELETE FROM patients WHERE id = ?",
                    id
            );

        } catch (Exception e) {

            // If foreign-key records exist
            // show normal page instead of crashing.
        }

        return "redirect:/dbms";
    }

    // =========================
    // STORED FUNCTION
    // =========================
    @GetMapping("/dbms/function")
    public String function(
            @RequestParam BigDecimal total,
            @RequestParam BigDecimal paid,
            Model model) {

        setupDbmsObjects(model);

        BigDecimal due = jdbc.queryForObject(
                "SELECT CalculateDue(?, ?)",
                BigDecimal.class,
                total,
                paid
        );

        model.addAttribute("functionTotal", total);
        model.addAttribute("functionPaid", paid);
        model.addAttribute("functionDue", due);

        loadDashboardData(model);

        return "dbms";
    }

    // =========================
    // TRIGGER TEST
    // =========================
    @PostMapping("/dbms/trigger")
    public String triggerTest(
            @RequestParam Long patientId,
            @RequestParam BigDecimal total,
            @RequestParam BigDecimal paid,
            Model model) {

        try {

            String status;

            if (paid.compareTo(BigDecimal.ZERO) == 0) {
                status = "UNPAID";
            } else if (paid.compareTo(total) >= 0) {
                status = "PAID";
            } else {
                status = "PARTIAL";
            }

            jdbc.update(
                    """
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
                    jdbc.queryForMap(
                            """
                            SELECT id, total_amount, paid_amount, status
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

    // =========================
    // LOAD COMMON DATA
    // =========================
    private void loadDashboardData(Model model) {

        model.addAttribute(
                "patients",
                jdbc.queryForList(
                        "SELECT id, name, age, gender, phone, email, address " +
                        "FROM patients ORDER BY id DESC"
                )
        );

        model.addAttribute(
                "joinResults",
                jdbc.queryForList(
                        """
                        SELECT
                            p.name AS patient,
                            d.name AS doctor,
                            a.appointment_date,
                            a.status
                        FROM appointments a
                        JOIN patients p ON a.patient_id = p.id
                        JOIN doctors d ON a.doctor_id = d.id
                        ORDER BY a.appointment_date DESC
                        """
                )
        );

        model.addAttribute(
                "groupResults",
                jdbc.queryForList(
                        """
                        SELECT status, COUNT(*) AS total
                        FROM appointments
                        GROUP BY status
                        """
                )
        );

        BigDecimal totalBilled =
                jdbc.queryForObject(
                        "SELECT COALESCE(SUM(total_amount),0) FROM bills",
                        BigDecimal.class
                );

        model.addAttribute(
                "totalBilled",
                totalBilled
        );
    }
}