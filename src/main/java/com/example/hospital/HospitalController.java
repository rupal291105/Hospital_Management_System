package com.example.hospital;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
public class HospitalController {
    private final PatientRepository patients;
    private final DoctorRepository doctors;
    private final AppointmentRepository appointments;
    private final BillRepository bills;

    public HospitalController(PatientRepository patients, DoctorRepository doctors,
                              AppointmentRepository appointments, BillRepository bills) {
        this.patients = patients;
        this.doctors = doctors;
        this.appointments = appointments;
        this.bills = bills;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("patientCount", patients.count());
        model.addAttribute("doctorCount", doctors.count());
        model.addAttribute("appointmentCount", appointments.count());
        model.addAttribute("billCount", bills.count());
        return "index";
    }

    @GetMapping("/patients")
    public String patients(@RequestParam(required = false) String search, Model model) {
        model.addAttribute("patients", search == null || search.isBlank()
                ? patients.findAll() : patients.findByNameContainingIgnoreCase(search));
        model.addAttribute("search", search == null ? "" : search);
        return "patients";
    }

    @GetMapping("/patients/new")
    public String newPatient(Model model) {
        model.addAttribute("patient", new Patient());
        return "patient-form";
    }

    @PostMapping("/patients/save")
    public String savePatient(@Valid @ModelAttribute Patient patient, BindingResult result) {
        if (result.hasErrors()) return "patient-form";
        patients.save(patient);
        return "redirect:/patients";
    }

    @PostMapping("/patients/delete/{id}")
    public String deletePatient(@PathVariable Long id) {
        patients.deleteById(id);
        return "redirect:/patients";
    }

    @GetMapping("/doctors")
    public String doctors(@RequestParam(required = false) String search, Model model) {
        model.addAttribute("doctors", search == null || search.isBlank()
                ? doctors.findAll() : doctors.findByNameContainingIgnoreCase(search));
        model.addAttribute("search", search == null ? "" : search);
        return "doctors";
    }

    @GetMapping("/doctors/new")
    public String newDoctor(Model model) {
        model.addAttribute("doctor", new Doctor());
        return "doctor-form";
    }

    @PostMapping("/doctors/save")
    public String saveDoctor(@Valid @ModelAttribute Doctor doctor, BindingResult result) {
        if (result.hasErrors()) return "doctor-form";
        doctors.save(doctor);
        return "redirect:/doctors";
    }

    @PostMapping("/doctors/delete/{id}")
    public String deleteDoctor(@PathVariable Long id) {
        doctors.deleteById(id);
        return "redirect:/doctors";
    }

    @GetMapping("/appointments")
    public String appointments(Model model) {
        model.addAttribute("appointments", this.appointments.findAll());
        return "appointments";
    }

    @GetMapping("/appointments/new")
    public String newAppointment(Model model) {
        model.addAttribute("appointment", new Appointment());
        model.addAttribute("patients", patients.findAll());
        model.addAttribute("doctors", doctors.findAll());
        return "appointment-form";
    }

    @PostMapping("/appointments/save")
    public String saveAppointment(@ModelAttribute Appointment appointment,
                                  @RequestParam Long patientId,
                                  @RequestParam Long doctorId) {
        appointment.setPatient(patients.findById(patientId).orElseThrow());
        appointment.setDoctor(doctors.findById(doctorId).orElseThrow());
        if (appointment.getStatus() == null) appointment.setStatus(Appointment.Status.SCHEDULED);
        appointments.save(appointment);
        return "redirect:/appointments";
    }

    @PostMapping("/appointments/cancel/{id}")
    public String cancelAppointment(@PathVariable Long id) {
        Appointment a = appointments.findById(id).orElseThrow();
        a.setStatus(Appointment.Status.CANCELLED);
        appointments.save(a);
        return "redirect:/appointments";
    }

    @GetMapping("/bills")
    public String bills(Model model) {
        model.addAttribute("bills", bills.findAll());
        return "bills";
    }

    @GetMapping("/bills/new")
    public String newBill(Model model) {
        Bill bill = new Bill();
        bill.setTotalAmount(BigDecimal.ZERO);
        bill.setPaidAmount(BigDecimal.ZERO);
        model.addAttribute("bill", bill);
        model.addAttribute("patients", patients.findAll());
        return "bill-form";
    }

    @PostMapping("/bills/save")
    public String saveBill(@ModelAttribute Bill bill, @RequestParam Long patientId) {
        bill.setPatient(patients.findById(patientId).orElseThrow());
        if (bill.getTotalAmount() == null) bill.setTotalAmount(BigDecimal.ZERO);
        if (bill.getPaidAmount() == null) bill.setPaidAmount(BigDecimal.ZERO);
        if (bill.getPaidAmount().compareTo(BigDecimal.ZERO) <= 0) {
            bill.setStatus(Bill.PaymentStatus.UNPAID);
        } else if (bill.getPaidAmount().compareTo(bill.getTotalAmount()) >= 0) {
            bill.setStatus(Bill.PaymentStatus.PAID);
        } else {
            bill.setStatus(Bill.PaymentStatus.PARTIAL);
        }
        bill.setBillDate(LocalDateTime.now());
        bills.save(bill);
        return "redirect:/bills";
    }
}
