package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Dto.Slot;
import com.capstone.OpportuGrow.Repository.AvailabilityRepository;
import com.capstone.OpportuGrow.model.*;
import com.capstone.OpportuGrow.Repository.AppointmentRepository;
import com.capstone.OpportuGrow.Repository.ConsultantRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class AppointmentController {

    private final ConsultantRepository consultantRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final AvailabilityRepository availabilityRepository;

    public AppointmentController(AvailabilityRepository availabilityRepository,
            ConsultantRepository consultantRepository, AppointmentRepository appointmentRepository,
            UserRepository userRepository) {
        this.consultantRepository = consultantRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.availabilityRepository = availabilityRepository;
    }

    /*
     * =========================
     * GET – صفحة حجز المواعيد
     * =========================
     */
    @GetMapping("/appointments")
    public String showAppointmentsPage(Model model) {
        List<Consultant> consultants = consultantRepository.findAll();
        model.addAttribute("consultants", consultants);
        return "appointments";
    }

    @GetMapping("/member/consultants/{id}")
    public String viewConsultantDetails(@PathVariable Long id, Model model) {
        Consultant consultant = consultantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid consultant Id"));

        consultant.setAvailableSlots(generateAvailableSlots(consultant));

        model.addAttribute("consultant", consultant);
        return "consultant-details";
    }

    @PostMapping("/appointments/book")
    public String bookAppointment(@RequestParam Long consultantId,
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam Integer duration,
            @RequestParam String topic,
            Principal principal,
            Model model) {

        Consultant consultant = consultantRepository.findById(consultantId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid consultant Id"));

        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate localDate = LocalDate.parse(date);
        LocalTime localTime = LocalTime.parse(time);

        Appointment appointment = new Appointment();
        appointment.setConsultant(consultant);
        appointment.setUser(user);
        appointment.setDate(localDate);
        appointment.setTime(localTime);
        appointment.setDuration(duration);
        appointment.setTopic(topic);
        appointment.setStatus(AppointmentStatus.PENDING);

        appointmentRepository.save(appointment);

        return "redirect:/appointments?success=true";
    }

    // Consultant Management
    @GetMapping("/consultant/appointments")
    public String viewConsultantAppointments(Model model, Principal principal) {
        Consultant consultant = consultantRepository.findByEmail(principal.getName());
        List<Appointment> allAppointments = appointmentRepository.findByConsultantId(consultant.getId());

        List<Appointment> pendingAppointments = allAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.PENDING)
                .toList();

        model.addAttribute("appointments", pendingAppointments);
        return "consultant-appointments";
    }

    @PostMapping("/consultant/appointments/{id}/approve")
    public String approveAppointment(@PathVariable Long id, Principal principal) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid appointment Id"));

        Consultant consultant = consultantRepository.findByEmail(principal.getName());
        if (appointment.getConsultant().getId() != consultant.getId()) {
            throw new RuntimeException("Unauthorized");
        }

        appointment.setStatus(AppointmentStatus.APPROVED);
        appointmentRepository.save(appointment);
        return "redirect:/consultant/appointments?success=approved";
    }

    @PostMapping("/consultant/appointments/{id}/reject")
    public String rejectAppointment(@PathVariable Long id, Principal principal) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid appointment Id"));

        Consultant consultant = consultantRepository.findByEmail(principal.getName());
        if (appointment.getConsultant().getId() != consultant.getId()) {
            throw new RuntimeException("Unauthorized");
        }

        appointment.setStatus(AppointmentStatus.REJECTED);
        appointmentRepository.save(appointment);
        return "redirect:/consultant/appointments?success=rejected";
    }

    private List<Slot> generateAvailableSlots(Consultant consultant) {
        List<Slot> slots = new ArrayList<>();
        for (int day = 0; day < 5; day++) {
            LocalDate date = LocalDate.now().plusDays(day);
            List<Availability> availabilities = availabilityRepository.findByConsultantAndDay(
                    consultant, date.getDayOfWeek());
            for (Availability availability : availabilities) {
                LocalTime time = availability.getStartTime();
                while (time.isBefore(availability.getEndTime())) {
                    LocalTime tempTime = time;
                    boolean isTaken = appointmentRepository.findByConsultantId(consultant.getId())
                            .stream()
                            .anyMatch(a -> a.getDate().equals(date) && a.getTime().equals(tempTime));
                    if (!isTaken) {
                        Slot newSlot = new Slot();
                        newSlot.setTime(tempTime);
                        newSlot.setDate(date);
                        newSlot.setBooked(false);
                        slots.add(newSlot);
                    }
                    time = time.plusHours(1);
                }
            }
        }
        return slots;
    }
}
