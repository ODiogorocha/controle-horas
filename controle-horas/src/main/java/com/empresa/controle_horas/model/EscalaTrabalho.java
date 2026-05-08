package com.empresa.controle_horas.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Representa uma escala de trabalho fixa.
 *
 * Exemplos de escalas:
 *   - Segunda a Sexta (5 dias de trabalho, 2 de folga)
 *   - 12x36 (12h trabalhadas, 36h de descanso)
 *   - Sabado e Domingo (final de semana)
 *   - Turno da Manha Segunda a Sabado
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "escalas_trabalho")
public class EscalaTrabalho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome descritivo da escala.
     * Ex: "Segunda a Sexta", "12x36", "Turno da Manha"
     */
    @Column(nullable = false, unique = true)
    private String nome;

    /**
     * Descricao detalhada para ajudar o usuario a entender a escala.
     * Ex: "Trabalha de segunda a sexta, folga sabado e domingo"
     */
    private String descricao;

    /**
     * Tipo da escala. Define como o calculo de dias de trabalho funciona.
     *
     * SEMANAL  = dias fixos da semana (ex: seg, ter, qua, qui, sex)
     * REVEZAMENTO = ciclo de dias trabalhados x dias de folga (ex: 12x36, 6x2)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEscala tipo;

    // ── Campos para escala SEMANAL ────────────────────────────
    // Cada campo indica se aquele dia da semana e dia de trabalho

    /** Segunda-feira e dia de trabalho? */
    private boolean segunda = false;

    /** Terca-feira e dia de trabalho? */
    private boolean terca = false;

    /** Quarta-feira e dia de trabalho? */
    private boolean quarta = false;

    /** Quinta-feira e dia de trabalho? */
    private boolean quinta = false;

    /** Sexta-feira e dia de trabalho? */
    private boolean sexta = false;

    /** Sabado e dia de trabalho? */
    private boolean sabado = false;

    /** Domingo e dia de trabalho? */
    private boolean domingo = false;

    // ── Campos para escala de REVEZAMENTO ────────────────────

    /**
     * Quantos dias seguidos o funcionario trabalha no ciclo.
     * Ex: no 12x36, este valor e 1 (trabalha 12h em 1 dia)
     * Ex: no 6x2, este valor e 6
     */
    private Integer diasTrabalhados;

    /**
     * Quantos dias seguidos o funcionario folga no ciclo.
     * Ex: no 12x36, este valor e 1 (folga 36h = proximos 1,5 dias)
     * Ex: no 6x2, este valor e 2
     */
    private Integer diasFolga;

    /**
     * Quantas horas o funcionario trabalha por turno no revezamento.
     * Ex: no 12x36, este valor e 12
     */
    private Double horasPorTurno;

    public enum TipoEscala {
        SEMANAL,      // Dias fixos da semana
        REVEZAMENTO   // Ciclo de dias trabalhados x dias de folga
    }
}