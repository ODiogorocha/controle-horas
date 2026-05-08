package com.empresa.controle_horas.controller;

import com.empresa.controle_horas.model.*;
import com.empresa.controle_horas.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/funcionarios")
@RequiredArgsConstructor

public class FuncionarioController {
    private final FuncionarioRepository funcRepo;
    private final CargoRepository cargoRepo;

    @GetMapping
    public List<Funcionario> listar(){
        return funcRepo.findAll();
    }

    @PostMapping
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        funcRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
