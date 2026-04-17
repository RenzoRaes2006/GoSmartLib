package ap.be.backend.controller;

import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.dto.ScanRequestDTO;
import ap.be.backend.dto.ScanResponseDTO;
import ap.be.backend.service.ScanService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class ScanController {
    private final ScanService scanService;

    public ScanController(ScanService scanService) {
        this.scanService = scanService;
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/api/scan")
    public ResponseEntity<ScanResponseDTO> verwerkScan(@RequestBody ScanRequestDTO scanRequest) {
        ScanResponseDTO response = scanService.verwerkScan(scanRequest);
        return ResponseEntity.ok(response);
    }

}
