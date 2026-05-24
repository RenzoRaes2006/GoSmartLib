package ap.be.backend.controller;

import org.springframework.web.bind.annotation.RestController;

import ap.be.backend.dto.RetourSchadeRequestDTO;
import ap.be.backend.dto.ScanBatchRequestDTO;
import ap.be.backend.dto.ScanBatchResponseDTO;
import ap.be.backend.service.ScanService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class ScanController {
    private final ScanService scanService;

    public ScanController(ScanService scanService) {
        this.scanService = scanService;
    }

    @PreAuthorize("hasRole('BEHEERDER')")
    @PostMapping("/api/scan/batch")
    public ResponseEntity<ScanBatchResponseDTO> verwerkBatch(@RequestBody ScanBatchRequestDTO batchRequest) {
        ScanBatchResponseDTO response = scanService.verwerkBatch(batchRequest);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('BEHEERDER')")
    @PostMapping("/api/scan/retour-met-schade")
    public ResponseEntity<ScanBatchResponseDTO> verwerkRetourMetSchade(
            @RequestBody RetourSchadeRequestDTO request) {
        return ResponseEntity.ok(scanService.verwerkRetourMetSchade(request));
    }
}
