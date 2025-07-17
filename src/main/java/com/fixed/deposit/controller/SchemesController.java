package com.fixed.deposit.controller;

import com.fixed.deposit.model.Schemes;
import com.fixed.deposit.service.SchemesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schemes")
@CrossOrigin("*")
public class SchemesController {

    @Autowired
    private SchemesService schemesService;

    @PostMapping("/add")
    public ResponseEntity<?> addSchemes(@RequestBody Schemes scheme) {
        return ResponseEntity.ok(schemesService.addScheme(scheme));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateScheme(@PathVariable Long id, @RequestBody Schemes scheme) {
        Schemes updated = schemesService.updateScheme(id, scheme);
        if (updated == null) return ResponseEntity.badRequest().body("Scheme not found");
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/all")
    public List<Schemes> getAllSchemes() {
        return schemesService.getAllSchemes();
    }

    @GetMapping("/active")
    public List<Schemes> getActiveSchemes() {
        return schemesService.getActiveSchemes();
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        Schemes updated = schemesService.toggleStatus(id);
        if (updated == null) return ResponseEntity.badRequest().body("Scheme not found");
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/update-senior-rates")
    public ResponseEntity<Void> updateSeniorRates(@RequestBody List<Schemes> schemes) {
        schemesService.updateSeniorRates(schemes);
        return ResponseEntity.ok().build();
    }
    // In your SchemesController
    @PutMapping("/penalty")
    public ResponseEntity<Void> updatePenalty(@RequestBody Double penaltyRate) {
        schemesService.updatePenaltyRate(penaltyRate);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/penalty")
    public ResponseEntity<Double> getCurrentPenaltyRate() {
        return ResponseEntity.ok(schemesService.getCurrentPenaltyRate());
    }
}