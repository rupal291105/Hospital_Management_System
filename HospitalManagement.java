import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.sql.Time;
import java.util.Scanner;

public class HospitalManagement {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        while (true) {

            System.out.println("\n================================");
            System.out.println("     HOSPITAL MANAGEMENT SYSTEM");
            System.out.println("================================");
           System.out.println("1. Add Patient");
System.out.println("2. View Patients");
System.out.println("3. Add Doctor");
System.out.println("4. View Doctors");
System.out.println("5. Book Appointment");
System.out.println("6. View Appointments");
System.out.println("7. Generate Bill");
System.out.println("8. View Bills");
System.out.println("9. Update Patient");
System.out.println("10. Delete Patient");
System.out.println("11. Exit");
            System.out.println("================================");

            System.out.print("Enter your choice: ");

            int choice;

            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.println("Please enter a valid number.");
                continue;
            }

            switch (choice) {

                // ================= ADD PATIENT =================
                case 1:
                    addPatient();
                    break;

                // ================= VIEW PATIENTS =================
                case 2:
                    viewPatients();
                    break;

                // ================= ADD DOCTOR =================
                case 3:
                    addDoctor();
                    break;

                // ================= VIEW DOCTORS =================
                case 4:
                    viewDoctors();
                    break;

                // ================= BOOK APPOINTMENT =================
                case 5:
                    bookAppointment();
                    break;

                // ================= VIEW APPOINTMENTS =================
                case 6:
                    viewAppointments();
                    break;

                // ================= GENERATE BILL =================
                case 7:
                    generateBill();
                    break;

                // ================= VIEW BILLS =================
                case 8:
                    viewBills();
                    break;

                // ================= EXIT =================
               // ================= UPDATE PATIENT =================
case 9:

    System.out.println("\n--- UPDATE PATIENT ---");

    System.out.print("Enter patient ID: ");
    int updateId = sc.nextInt();
    sc.nextLine();

    System.out.print("Enter new name: ");
    String newName = sc.nextLine();

    System.out.print("Enter new age: ");
    int newAge = sc.nextInt();
    sc.nextLine();

    System.out.print("Enter new gender: ");
    String newGender = sc.nextLine();

    System.out.print("Enter new phone: ");
    String newPhone = sc.nextLine();

    System.out.print("Enter new address: ");
    String newAddress = sc.nextLine();

    String updateSQL =
            "UPDATE patients SET name=?, age=?, gender=?, phone=?, address=? " +
            "WHERE patient_id=?";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(updateSQL)) {

        ps.setString(1, newName);
        ps.setInt(2, newAge);
        ps.setString(3, newGender);
        ps.setString(4, newPhone);
        ps.setString(5, newAddress);
        ps.setInt(6, updateId);

        int rows = ps.executeUpdate();

        if (rows > 0) {
            System.out.println("Patient updated successfully!");
        } else {
            System.out.println("Patient ID not found.");
        }

    } catch (Exception e) {
        System.out.println("Failed to update patient.");
        e.printStackTrace();
    }

    break;


// ================= DELETE PATIENT =================
case 10:

    System.out.println("\n--- DELETE PATIENT ---");

    System.out.print("Enter patient ID: ");
    int deleteId = sc.nextInt();
    sc.nextLine();

    String deleteSQL =
            "DELETE FROM patients WHERE patient_id=?";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(deleteSQL)) {

        ps.setInt(1, deleteId);

        int rows = ps.executeUpdate();

        if (rows > 0) {
            System.out.println("Patient deleted successfully!");
        } else {
            System.out.println("Patient ID not found.");
        }

    } catch (Exception e) {
        System.out.println("Failed to delete patient.");
        e.printStackTrace();
    }

    break;


// ================= EXIT =================
case 11:

    System.out.println("\nThank you for using Hospital Management System!");
    sc.close();
    return;
                default:
                    System.out.println("Invalid choice. Please select 1-9.");
            }
        }
    }

    // =========================================================
    // ADD PATIENT
    // =========================================================

    static void addPatient() {

        System.out.println("\n--- ADD PATIENT ---");

        System.out.print("Enter patient name: ");
        String name = sc.nextLine();

        System.out.print("Enter age: ");
        int age;

        try {
            age = Integer.parseInt(sc.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid age.");
            return;
        }

        System.out.print("Enter gender: ");
        String gender = sc.nextLine();

        System.out.print("Enter phone: ");
        String phone = sc.nextLine();

        System.out.print("Enter address: ");
        String address = sc.nextLine();

        String sql =
                "INSERT INTO patients (name, age, gender, phone, address) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setString(3, gender);
            ps.setString(4, phone);
            ps.setString(5, address);

            ps.executeUpdate();

            System.out.println("Patient added successfully!");

        } catch (Exception e) {
            System.out.println("Failed to add patient.");
            e.printStackTrace();
        }
    }

    // =========================================================
    // VIEW PATIENTS
    // =========================================================

    static void viewPatients() {

        System.out.println("\n--- PATIENT LIST ---");

        String sql = "SELECT * FROM patients";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean found = false;

            while (rs.next()) {

                found = true;

                System.out.println("--------------------------------");
                System.out.println("Patient ID : " + rs.getInt("patient_id"));
                System.out.println("Name       : " + rs.getString("name"));
                System.out.println("Age        : " + rs.getInt("age"));
                System.out.println("Gender     : " + rs.getString("gender"));
                System.out.println("Phone      : " + rs.getString("phone"));
                System.out.println("Address    : " + rs.getString("address"));
            }

            if (!found) {
                System.out.println("No patients found.");
            }

        } catch (Exception e) {
            System.out.println("Failed to fetch patients.");
            e.printStackTrace();
        }
    }

    // =========================================================
    // ADD DOCTOR
    // =========================================================

    static void addDoctor() {

        System.out.println("\n--- ADD DOCTOR ---");

        System.out.print("Enter doctor name: ");
        String name = sc.nextLine();

        System.out.print("Enter specialization: ");
        String specialization = sc.nextLine();

        System.out.print("Enter phone: ");
        String phone = sc.nextLine();

        String sql =
                "INSERT INTO doctors (name, specialization, phone) " +
                "VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, specialization);
            ps.setString(3, phone);

            ps.executeUpdate();

            System.out.println("Doctor added successfully!");

        } catch (Exception e) {
            System.out.println("Failed to add doctor.");
            e.printStackTrace();
        }
    }

    // =========================================================
    // VIEW DOCTORS
    // =========================================================

    static void viewDoctors() {

        System.out.println("\n--- DOCTOR LIST ---");

        String sql = "SELECT * FROM doctors";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean found = false;

            while (rs.next()) {

                found = true;

                System.out.println("--------------------------------");
                System.out.println("Doctor ID       : " + rs.getInt("doctor_id"));
                System.out.println("Name            : " + rs.getString("name"));
                System.out.println("Specialization  : " + rs.getString("specialization"));
                System.out.println("Phone           : " + rs.getString("phone"));
            }

            if (!found) {
                System.out.println("No doctors found.");
            }

        } catch (Exception e) {
            System.out.println("Failed to fetch doctors.");
            e.printStackTrace();
        }
    }

    // =========================================================
    // BOOK APPOINTMENT
    // =========================================================

    static void bookAppointment() {

        System.out.println("\n--- BOOK APPOINTMENT ---");

        System.out.print("Enter patient ID: ");
        int patientId;

        try {
            patientId = Integer.parseInt(sc.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid patient ID.");
            return;
        }

        System.out.print("Enter doctor ID: ");
        int doctorId;

        try {
            doctorId = Integer.parseInt(sc.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid doctor ID.");
            return;
        }

        System.out.print("Enter appointment date (YYYY-MM-DD): ");
        String dateInput = sc.nextLine();

        System.out.print("Enter appointment time (HH:MM:SS): ");
        String timeInput = sc.nextLine();

        System.out.print("Enter reason: ");
        String reason = sc.nextLine();

        String sql =
                "INSERT INTO appointments " +
                "(patient_id, doctor_id, appointment_date, appointment_time, reason) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            ps.setDate(3, Date.valueOf(dateInput));
            ps.setTime(4, Time.valueOf(timeInput));
            ps.setString(5, reason);

            ps.executeUpdate();

            System.out.println("Appointment booked successfully!");

        } catch (Exception e) {
            System.out.println("Failed to book appointment.");
            e.printStackTrace();
        }
    }

    // =========================================================
    // VIEW APPOINTMENTS
    // =========================================================

    static void viewAppointments() {

        System.out.println("\n--- APPOINTMENT LIST ---");

        String sql =
                "SELECT a.appointment_id, " +
                "p.name AS patient_name, " +
                "d.name AS doctor_name, " +
                "d.specialization, " +
                "a.appointment_date, " +
                "a.appointment_time, " +
                "a.reason " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN doctors d ON a.doctor_id = d.doctor_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean found = false;

            while (rs.next()) {

                found = true;

                System.out.println("--------------------------------");
                System.out.println("Appointment ID : " +
                        rs.getInt("appointment_id"));
                System.out.println("Patient        : " +
                        rs.getString("patient_name"));
                System.out.println("Doctor         : " +
                        rs.getString("doctor_name"));
                System.out.println("Specialization : " +
                        rs.getString("specialization"));
                System.out.println("Date           : " +
                        rs.getDate("appointment_date"));
                System.out.println("Time           : " +
                        rs.getTime("appointment_time"));
                System.out.println("Reason         : " +
                        rs.getString("reason"));
            }

            if (!found) {
                System.out.println("No appointments found.");
            }

        } catch (Exception e) {
            System.out.println("Failed to fetch appointments.");
            e.printStackTrace();
        }
    }

    // =========================================================
    // GENERATE BILL
    // =========================================================

    static void generateBill() {

        System.out.println("\n--- GENERATE BILL ---");

        System.out.print("Enter patient ID: ");
        int patientId;

        try {
            patientId = Integer.parseInt(sc.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid patient ID.");
            return;
        }

        System.out.print("Enter bill amount: ");
        double amount;

        try {
            amount = Double.parseDouble(sc.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid amount.");
            return;
        }

        System.out.print("Enter bill status (Paid/Pending): ");
        String status = sc.nextLine();

        String sql =
                "INSERT INTO bills (patient_id, amount, bill_date, status) " +
                "VALUES (?, ?, CURDATE(), ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            ps.setDouble(2, amount);
            ps.setString(3, status);

            ps.executeUpdate();

            System.out.println("Bill generated successfully!");

        } catch (Exception e) {
            System.out.println("Failed to generate bill.");
            e.printStackTrace();
        }
    }

    // =========================================================
    // VIEW BILLS
    // =========================================================

    static void viewBills() {

        System.out.println("\n--- BILL LIST ---");

        String sql =
                "SELECT b.bill_id, p.name AS patient_name, " +
                "b.amount, b.bill_date, b.status " +
                "FROM bills b " +
                "JOIN patients p ON b.patient_id = p.patient_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean found = false;

            while (rs.next()) {

                found = true;

                System.out.println("--------------------------------");
                System.out.println("Bill ID      : " +
                        rs.getInt("bill_id"));
                System.out.println("Patient      : " +
                        rs.getString("patient_name"));
                System.out.println("Amount       : Rs." +
                        rs.getDouble("amount"));
                System.out.println("Bill Date    : " +
                        rs.getDate("bill_date"));
                System.out.println("Status       : " +
                        rs.getString("status"));
            }

            if (!found) {
                System.out.println("No bills found.");
            }

        } catch (Exception e) {
            System.out.println("Failed to fetch bills.");
            e.printStackTrace();
        }
    }
}