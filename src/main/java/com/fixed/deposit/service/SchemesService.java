package com.fixed.deposit.service;

import com.fixed.deposit.model.Schemes;
import com.fixed.deposit.repository.SchemesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchemesService {

    @Autowired
    private SchemesRepository schemesRepo;

    public Schemes addScheme(Schemes scheme) {
        scheme.setActive(true);
        if ("CUMULATIVE".equalsIgnoreCase(scheme.getSchemeType())) {
            scheme.setPayout("On Maturity");
        }
        return schemesRepo.save(scheme);
    }

    public Schemes updateScheme(Long id, Schemes updatedScheme) {
        Schemes existing = schemesRepo.findById(id).orElse(null);
        if (existing == null) return null;

        existing.setSchemeName(updatedScheme.getSchemeName());
        existing.setInterestRate(updatedScheme.getInterestRate());
        existing.setTenureMonths(updatedScheme.getTenureMonths());
        existing.setMinAmount(updatedScheme.getMinAmount());
        existing.setSchemeType(updatedScheme.getSchemeType());
        existing.setActive(updatedScheme.isActive());
        existing.setPayout(updatedScheme.getPayout());

        return schemesRepo.save(existing);
    }

    public List<Schemes> getAllSchemes() {
        return schemesRepo.findAll();
    }

    public List<Schemes> getActiveSchemes() {
        return schemesRepo.findByIsActiveTrue();
    }

    public Schemes toggleStatus(Long id) {
        Schemes scheme = schemesRepo.findById(id).orElse(null);
        if (scheme != null) {
            scheme.setActive(!scheme.isActive());
            return schemesRepo.save(scheme);
        }
        return null;
    }

    public void updateSeniorRates(List<Schemes> updatedSchemes) {
        for (Schemes incoming : updatedSchemes) {
            Schemes existing = schemesRepo.findById(incoming.getId()).orElse(null);
            if (existing != null) {
                existing.setSeniorBonusRate(incoming.getSeniorBonusRate());
                schemesRepo.save(existing);
            }
        }
    }

    public void updatePenaltyRate(Double penaltyRate) {
        List<Schemes> allSchemes = schemesRepo.findAll();
        for (Schemes scheme : allSchemes) {
            scheme.setPenality(penaltyRate);
        }
        schemesRepo.saveAll(allSchemes);
    }
    public Double getCurrentPenaltyRate() {
        List<Schemes> schemes = schemesRepo.findAll();

        return schemes.stream()
                .filter(s -> s.getPenality() != null)
                .mapToDouble(Schemes::getPenality)
                .findFirst()
                .orElse(0.0);
    }
}