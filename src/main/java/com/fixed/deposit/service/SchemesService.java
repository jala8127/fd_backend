package com.fixed.deposit.service;

import com.fixed.deposit.model.Schemes;
import com.fixed.deposit.model.User;
import com.fixed.deposit.repository.SchemesRepository;
import com.fixed.deposit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SchemesService {

    @Autowired
    private SchemesRepository schemesRepo;

    @Autowired
    private UserRepository userRepository;

    public List<Schemes> getActiveSchemesForUser(String email) {
        System.out.println("--- Getting schemes for user: " + email + " ---");

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            System.out.println("User not found in DB. Returning standard schemes.");
            return getActiveSchemes();
        }

        User user = userOpt.get();
        int age = 0;

        System.out.println("DEBUG: User DOB from DB: " + user.getDob());

        if (user.getDob() != null && !user.getDob().isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate dob = LocalDate.parse(user.getDob(), formatter);
                age = Period.between(dob, LocalDate.now()).getYears();
                System.out.println("DEBUG: Calculated Age: " + age);
            } catch (Exception e) {
                System.out.println("ERROR: Could not parse date '" + user.getDob() + "'. Error: " + e.getMessage());
            }
        }

        List<Schemes> activeSchemes = getActiveSchemes();

        if (age >= 60) {
            System.out.println("DEBUG: User is a senior citizen. Applying bonus rates.");
            return activeSchemes.stream().map(scheme -> {
                Schemes userScheme = new Schemes();
                userScheme.setId(scheme.getId());
                userScheme.setSchemeName(scheme.getSchemeName());
                userScheme.setTenureMonths(scheme.getTenureMonths());
                userScheme.setMinAmount(scheme.getMinAmount());
                userScheme.setSchemeType(scheme.getSchemeType());
                userScheme.setActive(scheme.isActive());
                userScheme.setPayout(scheme.getPayout());
                userScheme.setSeniorBonusRate(scheme.getSeniorBonusRate());
                userScheme.setPenality(scheme.getPenality());

                double bonus = scheme.getSeniorBonusRate() != null ? scheme.getSeniorBonusRate() : 0.0;
                userScheme.setInterestRate(scheme.getInterestRate() + bonus);

                return userScheme;
            }).collect(Collectors.toList());
        }

        System.out.println("DEBUG: User is not a senior citizen. Returning standard schemes.");
        return activeSchemes;
    }

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