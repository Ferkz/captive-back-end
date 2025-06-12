package dev.codingsales.Captive.controller;

import dev.codingsales.Captive.entity.TermsAndPrivacy;
import dev.codingsales.Captive.service.impl.TermsAndPrivacyServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/terms")
@CrossOrigin(origins = "*")
public class TermsAndPrivacyController {
    @Autowired
    private TermsAndPrivacyServiceImpl service;

    @GetMapping("/{type}")
    public ResponseEntity<TermsAndPrivacy> getTermsByType(@PathVariable String type){
        Optional<TermsAndPrivacy> terms = service.getByType(type.toUpperCase());
        return terms.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @GetMapping
    public ResponseEntity<List<TermsAndPrivacy>> getAllTerms() {
        List<TermsAndPrivacy> terms = service.getAll();
        return ResponseEntity.ok(terms);
    }

    @PostMapping
    public ResponseEntity<TermsAndPrivacy> saveOrUpdateTerms(@RequestBody TermsAndPrivacy terms) {
        if (terms.getType() == null || terms.getType().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        terms.setType(terms.getType().toUpperCase());
        TermsAndPrivacy savedTerms = service.saveOrUpdate(terms);
        return ResponseEntity.ok(savedTerms);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTerms(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
