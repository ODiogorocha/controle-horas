package com.empresa.controle_horas;

import com.empresa.controle_horas.service.EscalaService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Executado automaticamente quando o sistema inicia.
 *
 * Cria as escalas de trabalho padrao no banco de dados
 * se ainda nao existirem (primeira execucao).
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final EscalaService escalaService;

    @Override
    public void run(String... args) {
        escalaService.criarEscalasPadrao();
        System.out.println("[OK] Escalas de trabalho padrao verificadas.");
    }
}