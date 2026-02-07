package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Dto.AvailabilityDto;
import com.capstone.OpportuGrow.Dto.ConsultantDto;
import com.capstone.OpportuGrow.Service.ConsultantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultants")
public class ApiConsultantController {

    private final ConsultantService consultantService;

    public ApiConsultantController(ConsultantService consultantService) {
        this.consultantService = consultantService;
    }

    @GetMapping
    public ResponseEntity<List<ConsultantDto>> getAllConsultants() {
        return ResponseEntity.ok(consultantService.getAllConsultants());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultantDto> getConsultantById(@PathVariable Long id) {
        return consultantService.getConsultantById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<List<AvailabilityDto>> getConsultantAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(consultantService.getConsultantAvailability(id));
    }
}
