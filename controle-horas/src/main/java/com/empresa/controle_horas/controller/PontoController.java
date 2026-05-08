package com.empresa.controle_horas.controller;

import com.empresa.controle_horas.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/ponto")
@RequiredArgsConstructor

public class PontoController {
    private final CsvService csvService;
    private final HorasService horasService;

    @PostMapping("/importar")
    public ResponseEntity<String> importar(@RequestParam("arquivo")MultipartFile file)
            throws Exception {
        return ResponseEntity.ok(csvService.importarCsv(file));
    }

    @GetMapping("/relatorio")
    public ResponseEntity<Map<String, Object>> relatorio(
            @RequestParam Long funcionarioId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate inicio,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fim) {
        return ResponseEntity.ok(horasService.calcularRelatorio(funcionarioId, inicio, fim));
    }
}
