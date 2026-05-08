package com.empresa.controle_horas.controller;

import com.empresa.controle_horas.model.EscalaTrabalho;
import com.empresa.controle_horas.repository.EscalaTrabalhoRepository;
import com.empresa.controle_horas.service.EscalaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/escalas")
@RequiredArgsConstructor
public class EscalaController {

    private final EscalaTrabalhoRepository escalaRepository;
    private final EscalaService escalaService;

    @GetMapping
    public List<EscalaTrabalho> listar() {
        return escalaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EscalaTrabalho> buscar(@PathVariable Long id) {
        return escalaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody EscalaTrabalho escala) {
        if (escalaRepository.existsByNome(escala.getNome())) {
            return ResponseEntity.badRequest()
                    .body("Ja existe uma escala com o nome: " + escala.getNome());
        }
        return ResponseEntity.ok(escalaRepository.save(escala));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id,
                                       @RequestBody EscalaTrabalho atualizada) {
        return escalaRepository.findById(id).map(e -> {
            atualizada.setId(id);
            return ResponseEntity.ok(escalaRepository.save(atualizada));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!escalaRepository.existsById(id))
            return ResponseEntity.notFound().build();
        escalaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}