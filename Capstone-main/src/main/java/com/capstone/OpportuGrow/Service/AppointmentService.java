package com.capstone.OpportuGrow.Service;

import com.capstone.OpportuGrow.Dto.AppointmentRequestDto;
import com.capstone.OpportuGrow.Dto.AppointmentResponseDto;
import com.capstone.OpportuGrow.Repository.AppointmentRepository;
import com.capstone.OpportuGrow.Repository.AvailabilityRepository;
import com.capstone.OpportuGrow.Repository.ConsultantRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import com.capstone.OpportuGrow.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ConsultantRepository consultantRepository;
    private final UserRepository userRepository;
    private final AvailabilityRepository availabilityRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
            ConsultantRepository consultantRepository,
            UserRepository userRepository,
            AvailabilityRepository availabilityRepository) {
        this.appointmentRepository = appointmentRepository;
        this.consultantRepository = consultantRepository;
        this.userRepository = userRepository;
        this.availabilityRepository = availabilityRepository;
    }

    /**
     * Book a new appointment
     */
    public AppointmentResponseDto bookAppointment(AppointmentRequestDto request, String userEmail) {
        // Validate consultant exists
        Consultant consultant = consultantRepository.findById(request.getConsultantId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Consultant not found with ID: " + request.getConsultantId()));

        // Validate user exists
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        // Validate duration
        if (request.getDuration() != 30 && request.getDuration() != 50) {
            throw new IllegalArgumentException("Duration must be either 30 or 50 minutes");
        }

        // Parse date and time
        LocalDate date = LocalDate.parse(request.getDate());
        LocalTime time = LocalTime.parse(request.getTime());
        LocalTime endTime = time.plusMinutes(request.getDuration());

        // Validate working hours
        List<Availability> availabilities = availabilityRepository.findByConsultantAndDay(
                consultant, date.getDayOfWeek());

        boolean isWithinWorkingHours = availabilities.stream()
                .anyMatch(avg -> (time.equals(avg.getStartTime()) || time.isAfter(avg.getStartTime()))
                        && (endTime.equals(avg.getEndTime()) || endTime.isBefore(avg.getEndTime())));

        if (!isWithinWorkingHours) {
            throw new IllegalArgumentException("The selected time is outside the consultant's working hours");
        }

        // Check for conflicts with APPROVED appointments
        boolean hasConflict = appointmentRepository.findByConsultantId(consultant.getId()).stream()
                .filter(a -> a.getStatus() == AppointmentStatus.APPROVED)
                .filter(a -> a.getDate().equals(date))
                .anyMatch(a -> {
                    LocalTime aStart = a.getTime();
                    LocalTime aEnd = aStart.plusMinutes(a.getDuration());
                    return time.isBefore(aEnd) && endTime.isAfter(aStart);
                });

        if (hasConflict) {
            throw new IllegalStateException("This time slot conflicts with an existing approved appointment");
        }

        // Create and save appointment
        Appointment appointment = new Appointment();
        appointment.setConsultant(consultant);
        appointment.setUser(user);
        appointment.setDate(date);
        appointment.setTime(time);
        appointment.setDuration(request.getDuration());
        appointment.setTopic(request.getTopic());
        appointment.setStatus(AppointmentStatus.PENDING);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        return mapToResponseDto(savedAppointment);
    }

    /**
     * Get all appointments for a user
     */
    public List<AppointmentResponseDto> getUserAppointments(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        List<Appointment> appointments = appointmentRepository.findByUserId(user.getId());

        return appointments.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Map Appointment entity to DTO
     */
    private AppointmentResponseDto mapToResponseDto(Appointment appointment) {
        return AppointmentResponseDto.builder()
                .id(appointment.getId())
                .consultantId((long) appointment.getConsultant().getId())
                .consultantName(appointment.getConsultant().getName())
                .userId(appointment.getUser().getId())
                .userName(appointment.getUser().getName())
                .date(appointment.getDate().toString())
                .time(appointment.getTime().toString())
                .duration(appointment.getDuration())
                .topic(appointment.getTopic())
                .status(appointment.getStatus().toString())
                .notes(appointment.getNotes())
                .build();
    }
}
