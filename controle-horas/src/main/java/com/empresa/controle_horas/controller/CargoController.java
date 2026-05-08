package com.empresa.controle_horas.controller;

import com.empresa.controle_horas.model.Cargo;
import com.empresa.controle_horas.repository.CargoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cargos")
@RequiredArgsConstructor

public class CargoController {

    private final CargoRepository cargoRepo;

    @GetMapping
    public List<Cargo> listar() {
        return cargoRepo.findAll();

    }

    @PostMapping
    public ResponseEntity<Cargo> criar(@RequestBody Cargo cargo) {
        return ResponseEntity.ok(cargoRepo.save(cargo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cargo> atualizar(@PathVariable Long id, @RequestBody Cargo cargo){
        cargo.setId(id);
        return ResponseEntity.ok(cargoRepo.save(cargo));
    }
}
