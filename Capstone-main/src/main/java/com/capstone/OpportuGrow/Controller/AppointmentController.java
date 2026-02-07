package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Repository.AppointmentRepository;
import com.capstone.OpportuGrow.Repository.AvailabilityRepository;
import com.capstone.OpportuGrow.Repository.ConsultantRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import com.capstone.OpportuGrow.model.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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

        List<Availability> availabilities = availabilityRepository.findByConsultant(consultant);
        model.addAttribute("consultant", consultant);
        model.addAttribute("availabilities", availabilities);
        return "consultant-details";
    }

    @PostMapping("/appointments/book")
    public String bookAppointment(@RequestParam Long consultantId,
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam Integer duration,
            @RequestParam String topic,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {

        Consultant consultant = consultantRepository.findById(consultantId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid consultant Id"));

        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (duration != 30 && duration != 50) {
            redirectAttributes.addFlashAttribute("error", "Duration must be either 30 or 50 minutes.");
            return "redirect:/member/consultants/" + consultantId;
        }

        LocalDate localDate = LocalDate.parse(date);
        LocalTime localTime = LocalTime.parse(time);
        LocalTime endTime = localTime.plusMinutes(duration);

        // 1️⃣ Check General Availability (Working Hours)
        List<Availability> availabilities = availabilityRepository.findByConsultantAndDay(
                consultant, localDate.getDayOfWeek());

        boolean isWithinWorkingHours = availabilities.stream()
                .anyMatch(avg -> (localTime.equals(avg.getStartTime()) || localTime.isAfter(avg.getStartTime()))
                        && (endTime.equals(avg.getEndTime()) || endTime.isBefore(avg.getEndTime())));

        if (!isWithinWorkingHours) {
            redirectAttributes.addFlashAttribute("error",
                    "The selected time is outside the consultant's working hours.");
            return "redirect:/member/consultants/" + consultantId;
        }

        // 2️⃣ Check for conflicts with APPROVED appointments
        boolean hasConflict = appointmentRepository.findByConsultantId(consultant.getId()).stream()
                .filter(a -> a.getStatus() == AppointmentStatus.APPROVED)
                .filter(a -> a.getDate().equals(localDate))
                .anyMatch(a -> {
                    LocalTime aStart = a.getTime();
                    LocalTime aEnd = aStart.plusMinutes(a.getDuration());
                    // Check intersection: (StartReq < EndExist) and (EndReq > StartExist)
                    return localTime.isBefore(aEnd) && endTime.isAfter(aStart);
                });

        if (hasConflict) {
            redirectAttributes.addFlashAttribute("error",
                    "This time slot conflicts with an existing approved appointment.");
            return "redirect:/member/consultants/" + consultantId;
        }

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

}
