package com.empresa.controle_horas.controller;

import com.empresa.controle_horas.model.Folga;
import com.empresa.controle_horas.service.BancoHorasService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/banco-horas")
@RequiredArgsConstructor
public class BancoHorasController {

    private final BancoHorasService bancoHorasService;

    @GetMapping("/{id}/saldo")
    public ResponseEntity<?> saldo(@PathVariable Long id) {
        try {
            double saldo = bancoHorasService.consultarSaldo(id);
            return ResponseEntity.ok(Map.of(
                "funcionarioId", id,
                "saldoDisponivel", String.format("%.2f h", saldo),
                "mensagem", saldo > 0
                    ? "Voce tem " + String.format("%.2f", saldo) + " horas para folga remunerada"
                    : "Sem saldo disponivel no banco de horas"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/extrato")
    public ResponseEntity<?> extrato(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(bancoHorasService.extrato(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/creditar")
    public ResponseEntity<?> creditar(@PathVariable Long id,
                                       @RequestBody Map<String, Object> body) {
        try {
            double horas  = Double.parseDouble(body.get("horas").toString());
            String motivo = body.getOrDefault("motivo", "Horas extras").toString();
            return ResponseEntity.ok(
                    bancoHorasService.creditarHoras(id, horas, motivo));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/folga-remunerada")
    public ResponseEntity<?> folgaRemunerada(@PathVariable Long id,
                                              @RequestBody Map<String, Object> body) {
        try {
            LocalDate data = LocalDate.parse(body.get("data").toString());
            double horas   = Double.parseDouble(body.get("horas").toString());
            String obs     = body.getOrDefault("observacao", "").toString();
            return ResponseEntity.ok(
                    bancoHorasService.registrarFolgaRemunerada(id, data, horas, obs));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/folga")
    public ResponseEntity<?> registrarFolga(@PathVariable Long id,
                                             @RequestBody Map<String, Object> body) {
        try {
            LocalDate data       = LocalDate.parse(body.get("data").toString());
            Folga.TipoFolga tipo = Folga.TipoFolga.valueOf(
                    body.getOrDefault("tipo", "DESCANSO").toString().toUpperCase());
            String obs           = body.getOrDefault("observacao", "").toString();
            return ResponseEntity.ok(
                    bancoHorasService.registrarFolga(id, data, tipo, obs));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/folgas")
    public ResponseEntity<?> listarFolgas(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        try {
            List<Folga> folgas = bancoHorasService.listarFolgas(id, inicio, fim);
            return ResponseEntity.ok(folgas);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
