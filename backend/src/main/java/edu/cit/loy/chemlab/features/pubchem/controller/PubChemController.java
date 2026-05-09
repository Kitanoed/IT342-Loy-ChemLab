package edu.cit.loy.chemlab.features.pubchem.controller;

import edu.cit.loy.chemlab.features.inventory.dto.PubChemLookupResponse;
import edu.cit.loy.chemlab.features.pubchem.service.PubChemService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class PubChemController {

    private final PubChemService pubChemService;

    public PubChemController(PubChemService pubChemService) {
        this.pubChemService = pubChemService;
    }

    @GetMapping("/api/pubchem/lookup")
    @PreAuthorize("hasAnyRole('STUDENT','TECHNICIAN','ADMIN')")
    public ResponseEntity<PubChemLookupResponse> lookup(@RequestParam("name") String name, HttpServletRequest request) {
        String clientKey = request.getRemoteAddr();
        return ResponseEntity.ok(pubChemService.lookupByName(name, clientKey));
    }

    @GetMapping("/functions/v1/pubchem-lookup")
    @PreAuthorize("hasAnyRole('STUDENT','TECHNICIAN','ADMIN')")
    public ResponseEntity<PubChemLookupResponse> lookupFunctionStyle(@RequestParam("name") String name, HttpServletRequest request) {
        String clientKey = request.getRemoteAddr();
        return ResponseEntity.ok(pubChemService.lookupByName(name, clientKey));
    }
}