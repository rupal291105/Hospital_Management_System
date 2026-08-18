package com.example.hospital;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
public class HospitalController {

    private final PatientRepository patients;
    private final DoctorRepository doctors;
    private final AppointmentRepository appointments;
    private final BillRepository bills;

    public HospitalController(
            PatientRepository patients,
            DoctorRepository doctors,
            AppointmentRepository appointments,
            BillRepository bills) {

        this.patients = patients;
        this.doctors = doctors;
        this.appointments = appointments;
        this.bills = bills;
    }

    // =========================
    // DASHBOARD
    // =========================

    @GetMapping("/")
    public String dashboard(Model model) {

        model.addAttribute("patientCount", patients.count());
        model.addAttribute("doctorCount", doctors.count());
        model.addAttribute("appointmentCount", appointments.count());
        model.addAttribute("billCount", bills.count());

        return "index";
    }

    // =========================
    // PATIENTS
    // =========================

    @GetMapping("/patients")
    public String patients(
            @RequestParam(required = false) String search,
            Model model) {

        if (search == null || search.isBlank()) {
            model.addAttribute("patients", patients.findAll());
        } else {
            model.addAttribute(
                    "patients",
                    patients.findByNameContainingIgnoreCase(search)
            );
        }

        model.addAttribute(
                "search",
                search == null ? "" : search
        );

        return "patients";
    }

    @GetMapping("/patients/new")
    public String newPatient(Model model) {

        model.addAttribute("patient", new Patient());

        return "patient-form";
    }

    @PostMapping("/patients/save")
    public String savePatient(
            @ModelAttribute("patient") Patient patient) {

        patients.save(patient);

        return "redirect:/patients";
    }

    @PostMapping("/patients/delete/{id}")
    public String deletePatient(@PathVariable Long id) {

        patients.deleteById(id);

        return "redirect:/patients";
    }

    // =========================
    // DOCTORS
    // =========================

    @GetMapping("/doctors")
    public String doctors(
            @RequestParam(required = false) String search,
            Model model) {

        if (search == null || search.isBlank()) {
            model.addAttribute("doctors", doctors.findAll());
        } else {
            model.addAttribute(
                    "doctors",
                    doctors.findByNameContainingIgnoreCase(search)
            );
        }

        model.addAttribute(
                "search",
                search == null ? "" : search
        );

        return "doctors";
    }

    @GetMapping("/doctors/new")
    public String newDoctor(Model model) {

        model.addAttribute("doctor", new Doctor());

        return "doctor-form";
    }

    @PostMapping("/doctors/save")
    public String saveDoctor(
            @ModelAttribute("doctor") Doctor doctor) {

        doctors.save(doctor);

        return "redirect:/doctors";
    }

    @PostMapping("/doctors/delete/{id}")
    public String deleteDoctor(@PathVariable Long id) {

        doctors.deleteById(id);

        return "redirect:/doctors";
    }

    // =========================
    // APPOINTMENTS
    // =========================

    @GetMapping("/appointments")
    public String appointments(Model model) {

        model.addAttribute(
                "appointments",
                appointments.findAll()
        );

        return "appointments";
    }

    @GetMapping("/appointments/new")
    public String newAppointment(Model model) {

        model.addAttribute(
                "appointment",
                new Appointment()
        );

        model.addAttribute(
                "patients",
                patients.findAll()
        );

        model.addAttribute(
                "doctors",
                doctors.findAll()
        );

        return "appointment-form";
    }

    @PostMapping("/appointments/save")
    public String saveAppointment(
            @ModelAttribute("appointment") Appointment appointment,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            Model model) {

        boolean error = false;

        // Patient
        if (patientId == null) {

            model.addAttribute(
                    "patientError",
                    "Please select a patient."
            );

            error = true;

        } else {

            Patient patient =
                    patients.findById(patientId).orElse(null);

            if (patient == null) {

                model.addAttribute(
                        "patientError",
                        "Selected patient is invalid."
                );

                error = true;

            } else {

                appointment.setPatient(patient);
            }
        }

        // Doctor
        if (doctorId == null) {

            model.addAttribute(
                    "doctorError",
                    "Please select a doctor."
            );

            error = true;

        } else {

            Doctor doctor =
                    doctors.findById(doctorId).orElse(null);

            if (doctor == null) {

                model.addAttribute(
                        "doctorError",
                        "Selected doctor is invalid."
                );

                error = true;

            } else {

                appointment.setDoctor(doctor);
            }
        }

        // Date
        if (appointment.getAppointmentDate() == null) {

            model.addAttribute(
                    "appointmentDateError",
                    "Appointment date and time is required."
            );

            error = true;

        } else if (
                !appointment.getAppointmentDate()
                        .isAfter(LocalDateTime.now())
        ) {

            model.addAttribute(
                    "appointmentDateError",
                    "Appointment must be in the future."
            );

            error = true;
        }

        // Reason
        if (appointment.getReason() == null ||
                appointment.getReason().trim().isEmpty()) {

            model.addAttribute(
                    "reasonError",
                    "Reason is required."
            );

            error = true;
        }

        if (error) {

            model.addAttribute(
                    "patients",
                    patients.findAll()
            );

            model.addAttribute(
                    "doctors",
                    doctors.findAll()
            );

            return "appointment-form";
        }

        if (appointment.getStatus() == null) {

            appointment.setStatus(
                    Appointment.Status.SCHEDULED
            );
        }

        appointments.save(appointment);

        return "redirect:/appointments";
    }

    @PostMapping("/appointments/cancel/{id}")
    public String cancelAppointment(@PathVariable Long id) {

        Appointment appointment =
                appointments.findById(id).orElseThrow();

        appointment.setStatus(
                Appointment.Status.CANCELLED
        );

        appointments.save(appointment);

        return "redirect:/appointments";
    }

    // =========================
    // BILLS
    // =========================

    @GetMapping("/bills")
    public String bills(Model model) {

        model.addAttribute(
                "bills",
                bills.findAll()
        );

        return "bills";
    }

    @GetMapping("/bills/new")
    public String newBill(Model model) {

        model.addAttribute(
                "patients",
                patients.findAll()
        );

        return "bill-form";
    }

    @PostMapping("/bills/save")
    public String saveBill(
            @RequestParam("patientId") Long patientId,
            @RequestParam("totalAmount") BigDecimal totalAmount,
            @RequestParam("paidAmount") BigDecimal paidAmount,
            Model model) {

        // =========================
        // CHECK PATIENT
        // =========================

        Patient patient =
                patients.findById(patientId).orElse(null);

        if (patient == null) {

            model.addAttribute(
                    "patientError",
                    "Please select a valid patient."
            );

            model.addAttribute(
                    "patients",
                    patients.findAll()
            );

            return "bill-form";
        }

        // =========================
        // CHECK TOTAL AMOUNT
        // =========================

        if (totalAmount == null ||
                totalAmount.compareTo(BigDecimal.ZERO) <= 0) {

            model.addAttribute(
                    "amountError",
                    "Total amount must be greater than 0."
            );

            model.addAttribute(
                    "patients",
                    patients.findAll()
            );

            return "bill-form";
        }

        // =========================
        // CHECK PAID AMOUNT
        // =========================

        if (paidAmount == null ||
                paidAmount.compareTo(BigDecimal.ZERO) < 0) {

            model.addAttribute(
                    "paidAmountError",
                    "Paid amount cannot be negative."
            );

            model.addAttribute(
                    "patients",
                    patients.findAll()
            );

            return "bill-form";
        }

        // =========================
        // PAID CANNOT EXCEED TOTAL
        // =========================

        if (paidAmount.compareTo(totalAmount) > 0) {

            model.addAttribute(
                    "paidAmountError",
                    "Paid amount cannot be greater than total amount."
            );

            model.addAttribute(
                    "patients",
                    patients.findAll()
            );

            return "bill-form";
        }

        // =========================
        // CREATE BILL
        // =========================

        Bill bill = new Bill();

        bill.setPatient(patient);
        bill.setTotalAmount(totalAmount);
        bill.setPaidAmount(paidAmount);
        bill.setBillDate(LocalDateTime.now());

        // =========================
        // PAYMENT STATUS
        // =========================

        if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {

            bill.setStatus(
                    Bill.PaymentStatus.UNPAID
            );

        } else if (
                paidAmount.compareTo(totalAmount) == 0
        ) {

            bill.setStatus(
                    Bill.PaymentStatus.PAID
            );

        } else {

            bill.setStatus(
                    Bill.PaymentStatus.PARTIAL
            );
        }

        // =========================
        // SAVE
        // =========================

        bills.save(bill);

        return "redirect:/bills";
    }
}