package com.empresa.controle_horas.service;

import com.empresa.controle_horas.model.EscalaTrabalho;
import com.empresa.controle_horas.repository.EscalaTrabalhoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * Servico responsavel pela logica das escalas de trabalho.
 *
 * Determina se um dia especifico e dia de trabalho ou nao
 * para um funcionario, com base na sua escala.
 */
@Service
@RequiredArgsConstructor
public class EscalaService {

    private final EscalaTrabalhoRepository escalaRepository;

    /**
     * Verifica se uma data especifica e um dia de trabalho
     * para a escala informada.
     *
     * Para escala SEMANAL: verifica o dia da semana.
     * Para escala REVEZAMENTO: calcula com base no ciclo.
     *
     * @param escala Escala do funcionario
     * @param data   Data a verificar
     * @return true se e dia de trabalho, false se e folga
     */
    public boolean isDiaDeTrabalho(EscalaTrabalho escala, LocalDate data) {
        if (escala == null) {
            // Sem escala definida: assume segunda a sexta
            DayOfWeek dia = data.getDayOfWeek();
            return dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY;
        }

        if (escala.getTipo() == EscalaTrabalho.TipoEscala.SEMANAL) {
            return isDiaDeTrabalhoSemanal(escala, data);
        } else {
            return isDiaDeTrabalhoRevezamento(escala, data);
        }
    }

    /**
     * Verifica dia de trabalho para escala SEMANAL.
     * Simplesmente checa se o dia da semana esta marcado na escala.
     */
    private boolean isDiaDeTrabalhoSemanal(EscalaTrabalho escala, LocalDate data) {
        DayOfWeek dia = data.getDayOfWeek();
        return switch (dia) {
            case MONDAY    -> escala.isSegunda();
            case TUESDAY   -> escala.isTerca();
            case WEDNESDAY -> escala.isQuarta();
            case THURSDAY  -> escala.isQuinta();
            case FRIDAY    -> escala.isSexta();
            case SATURDAY  -> escala.isSabado();
            case SUNDAY    -> escala.isDomingo();
        };
    }

    /**
     * Verifica dia de trabalho para escala de REVEZAMENTO (ex: 12x36, 6x2).
     *
     * O calculo usa uma data de referencia (01/01/2025) como ponto de partida
     * do ciclo. A posicao no ciclo determina se e dia de trabalho ou folga.
     *
     * Exemplo para 12x36 (1 dia trabalha, 1 dia folga):
     *   Dia 0 do ciclo = trabalha
     *   Dia 1 do ciclo = folga
     *   Dia 2 do ciclo = trabalha (volta ao inicio)
     */
    private boolean isDiaDeTrabalhoRevezamento(EscalaTrabalho escala, LocalDate data) {
        if (escala.getDiasTrabalhados() == null || escala.getDiasFolga() == null) {
            return true; // Dados incompletos: assume que e dia de trabalho
        }

        // Ponto de referencia: inicio do ciclo
        LocalDate referencia = LocalDate.of(2025, 1, 1);

        int ciclo = escala.getDiasTrabalhados() + escala.getDiasFolga();
        long diasDesdeReferencia = referencia.until(data,
                java.time.temporal.ChronoUnit.DAYS);

        // Posicao dentro do ciclo atual (sempre positivo)
        int posicaoNoCiclo = (int) (((diasDesdeReferencia % ciclo) + ciclo) % ciclo);

        // Se a posicao esta dentro dos dias trabalhados, e dia de trabalho
        return posicaoNoCiclo < escala.getDiasTrabalhados();
    }

    /**
     * Conta quantos dias de trabalho existem em um periodo
     * para uma escala especifica.
     */
    public int contarDiasDeTrabalho(EscalaTrabalho escala,
                                    LocalDate inicio, LocalDate fim) {
        int count = 0;
        LocalDate data = inicio;
        while (!data.isAfter(fim)) {
            if (isDiaDeTrabalho(escala, data)) {
                count++;
            }
            data = data.plusDays(1);
        }
        return count;
    }

    /**
     * Cria as escalas fixas padrao no banco de dados.
     * Chamado na inicializacao se o banco estiver vazio.
     */
    public void criarEscalasPadrao() {
        if (escalaRepository.count() > 0) return;

        // Segunda a Sexta (mais comum)
        EscalaTrabalho segSex = new EscalaTrabalho();
        segSex.setNome("Segunda a Sexta");
        segSex.setDescricao("Trabalha de segunda a sexta-feira. Folga sabado e domingo.");
        segSex.setTipo(EscalaTrabalho.TipoEscala.SEMANAL);
        segSex.setSegunda(true);
        segSex.setTerca(true);
        segSex.setQuarta(true);
        segSex.setQuinta(true);
        segSex.setSexta(true);
        escalaRepository.save(segSex);

        // Segunda a Sabado
        EscalaTrabalho segSab = new EscalaTrabalho();
        segSab.setNome("Segunda a Sabado");
        segSab.setDescricao("Trabalha de segunda a sabado. Folga apenas no domingo.");
        segSab.setTipo(EscalaTrabalho.TipoEscala.SEMANAL);
        segSab.setSegunda(true);
        segSab.setTerca(true);
        segSab.setQuarta(true);
        segSab.setQuinta(true);
        segSab.setSexta(true);
        segSab.setSabado(true);
        escalaRepository.save(segSab);

        // 12x36
        EscalaTrabalho dozeX36 = new EscalaTrabalho();
        dozeX36.setNome("12x36");
        dozeX36.setDescricao("Trabalha 12 horas seguidas e folga as proximas 36 horas. Repete o ciclo.");
        dozeX36.setTipo(EscalaTrabalho.TipoEscala.REVEZAMENTO);
        dozeX36.setDiasTrabalhados(1);
        dozeX36.setDiasFolga(1);
        dozeX36.setHorasPorTurno(12.0);
        escalaRepository.save(dozeX36);

        // 6x2
        EscalaTrabalho seisX2 = new EscalaTrabalho();
        seisX2.setNome("6x2");
        seisX2.setDescricao("Trabalha 6 dias seguidos e folga 2 dias. Repete o ciclo.");
        seisX2.setTipo(EscalaTrabalho.TipoEscala.REVEZAMENTO);
        seisX2.setDiasTrabalhados(6);
        seisX2.setDiasFolga(2);
        escalaRepository.save(seisX2);

        // Turno - Final de Semana
        EscalaTrabalho fds = new EscalaTrabalho();
        fds.setNome("Final de Semana");
        fds.setDescricao("Trabalha apenas sabado e domingo. Folga de segunda a sexta.");
        fds.setTipo(EscalaTrabalho.TipoEscala.SEMANAL);
        fds.setSabado(true);
        fds.setDomingo(true);
        escalaRepository.save(fds);
    }
}