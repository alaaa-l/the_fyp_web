package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Dto.AppointmentRequestDto;
import com.capstone.OpportuGrow.Dto.AppointmentResponseDto;
import com.capstone.OpportuGrow.Service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class ApiAppointmentController {

    private final AppointmentService appointmentService;

    public ApiAppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Book a new appointment
     * POST /api/appointments
     */
    @PostMapping
    public ResponseEntity<?> bookAppointment(@Valid @RequestBody AppointmentRequestDto request,
            Principal principal) {
        try {
            String userEmail = principal.getName();
            AppointmentResponseDto response = appointmentService.bookAppointment(request, userEmail);

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", "Appointment booked successfully");
            successResponse.put("appointment", response);

            return ResponseEntity.status(HttpStatus.CREATED).body(successResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while booking the appointment"));
        }
    }

    /**
     * Get current user's appointments
     * GET /api/appointments
     */
    @GetMapping
    public ResponseEntity<?> getUserAppointments(Principal principal) {
        try {
            String userEmail = principal.getName();
            List<AppointmentResponseDto> appointments = appointmentService.getUserAppointments(userEmail);

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("appointments", appointments);

            return ResponseEntity.ok(successResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get appointments for a specific user ID
     * GET /api/appointments/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserAppointmentsById(@PathVariable Integer userId) {
        try {
            List<AppointmentResponseDto> appointments = appointmentService.getAppointmentsByUserId(userId);

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("appointments", appointments);

            return ResponseEntity.ok(successResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Helper method to create error response
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        return errorResponse;
    }
}
