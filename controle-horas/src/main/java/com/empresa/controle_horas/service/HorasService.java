package com.empresa.controle_horas.service;

import com.empresa.controle_horas.model.*;
import com.empresa.controle_horas.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

/**
 * Servico de calculo de horas.
 *
 * ATUALIZADO: agora considera a escala de trabalho do funcionario
 * e os dias de folga cadastrados no calculo de horas esperadas,
 * horas extras e horas faltando.
 *
 * Logica:
 *   1. Para cada dia do periodo, verifica se e dia de trabalho (pela escala)
 *   2. Se e dia de trabalho mas ha folga cadastrada, nao conta como falta
 *   3. Horas extras sao automaticamente creditadas no banco de horas
 */
@Service
@RequiredArgsConstructor
public class HorasService {

    private final FuncionarioRepository    funcionarioRepository;
    private final RegistroPontoRepository  registroPontoRepository;
    private final FolgaRepository          folgaRepository;
    private final BancoHorasRepository     bancoHorasRepository;
    private final EscalaService            escalaService;

    /**
     * Calcula o relatorio completo de horas de um funcionario.
     *
     * O relatorio inclui:
     *   - Horas trabalhadas (soma dos registros de ponto)
     *   - Horas esperadas (dias de trabalho x horas diarias do cargo)
     *   - Dias de folga no periodo
     *   - Horas extras (creditadas no banco de horas)
     *   - Horas faltando
     *   - Saldo atual do banco de horas
     */
    public Map<String, Object> calcularRelatorio(Long funcionarioId,
                                                 LocalDate inicio,
                                                 LocalDate fim) {
        Funcionario func = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> new RuntimeException(
                        "Funcionario nao encontrado com ID: " + funcionarioId));

        EscalaTrabalho escala = func.getEscala();
        double horasDiarias = func.getCargo().getHorasDiarias();

        // Para escala de revezamento, usa as horas do turno
        if (escala != null
                && escala.getTipo() == EscalaTrabalho.TipoEscala.REVEZAMENTO
                && escala.getHorasPorTurno() != null) {
            horasDiarias = escala.getHorasPorTurno();
        }

        // Busca registros de ponto e folgas do periodo
        List<RegistroPonto> registros = registroPontoRepository
                .findByFuncionarioAndDataBetween(func, inicio, fim);

        List<Folga> folgas = folgaRepository
                .findByFuncionarioAndDataBetween(func, inicio, fim);

        // Mapas para acesso rapido
        Set<LocalDate> datasComPonto = new HashSet<>();
        for (RegistroPonto r : registros) datasComPonto.add(r.getData());

        Set<LocalDate> datasComFolga = new HashSet<>();
        for (Folga f : folgas) datasComFolga.add(f.getData());

        // Variaveis de acumulacao
        double totalTrabalhado   = 0;
        double totalEsperado     = 0;
        int    diasTrabalhados   = 0;
        int    diasFolga         = folgas.size();
        int    diasFalta         = 0;

        // Percorre cada dia do periodo
        LocalDate dia = inicio;
        while (!dia.isAfter(fim)) {

            boolean eDiaDeTrabalho = escalaService.isDiaDeTrabalho(escala, dia);
            boolean temFolga       = datasComFolga.contains(dia);
            boolean temPonto       = datasComPonto.contains(dia);

            if (eDiaDeTrabalho && !temFolga) {
                // Dia de trabalho sem folga: conta nas horas esperadas
                totalEsperado += horasDiarias;

                if (temPonto) {
                    diasTrabalhados++;
                } else {
                    // Dia de trabalho sem registro e sem folga = falta
                    diasFalta++;
                }
            }

            dia = dia.plusDays(1);
        }

        // Soma horas trabalhadas dos registros de ponto
        for (RegistroPonto r : registros) {
            long minutos = Duration.between(r.getEntrada(), r.getSaida()).toMinutes();
            totalTrabalhado += minutos / 60.0;
        }

        // Calcula extras e faltas
        double horasExtras   = Math.max(0, totalTrabalhado - totalEsperado);
        double horasFaltando = Math.max(0, totalEsperado - totalTrabalhado);

        // Saldo atual do banco de horas
        double saldoBancoHoras = bancoHorasRepository.calcularSaldo(func);

        // Monta o relatorio
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("funcionario",          func.getNome());
        resultado.put("matricula",            func.getMatricula());
        resultado.put("cargo",                func.getCargo().getNome());
        resultado.put("escala",               escala != null ? escala.getNome() : "Segunda a Sexta (padrao)");
        resultado.put("horasDiarias",         String.format("%.1f h", horasDiarias));
        resultado.put("periodo",              inicio + " a " + fim);
        resultado.put("diasTrabalhados",      diasTrabalhados);
        resultado.put("diasFolga",            diasFolga);
        resultado.put("diasFalta",            diasFalta);
        resultado.put("horasTrabalhadas",     String.format("%.2f h", totalTrabalhado));
        resultado.put("horasEsperadas",       String.format("%.2f h", totalEsperado));
        resultado.put("horasExtras",          String.format("%.2f h", horasExtras));
        resultado.put("horasFaltando",        String.format("%.2f h", horasFaltando));
        resultado.put("saldoBancoHoras",      String.format("%.2f h", saldoBancoHoras));

        // Status geral
        if (horasExtras > 0) {
            resultado.put("status",
                    String.format("Horas extras: %.2f h acumuladas no banco", horasExtras));
        } else if (horasFaltando > 0) {
            resultado.put("status",
                    String.format("Atencao: %.2f h faltando no periodo", horasFaltando));
        } else {
            resultado.put("status", "Carga horaria cumprida corretamente");
        }

        return resultado;
    }

    /**
     * Relatorio geral de todos os funcionarios.
     */
    public List<Map<String, Object>> calcularRelatorioGeral(
            LocalDate inicio, LocalDate fim) {
        return funcionarioRepository.findAll().stream()
                .map(f -> calcularRelatorio(f.getId(), inicio, fim))
                .toList();
    }
}