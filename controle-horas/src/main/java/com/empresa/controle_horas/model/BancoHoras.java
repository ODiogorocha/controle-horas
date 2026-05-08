package com.empresa.controle_horas.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Representa uma movimentacao no banco de horas de um funcionario.
 *
 * Cada vez que o funcionario ganha ou usa horas do banco,
 * um registro e criado aqui. O saldo atual e a soma de todos
 * os registros do funcionario.
 *
 * Exemplos de movimentacoes:
 *   +2.5h  (CREDITO)  - trabalhou 2h30 a mais em um dia
 *   -8.0h  (DEBITO)   - usou 1 dia de folga compensatoria
 *   +4.0h  (CREDITO)  - trabalhou no feriado
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "banco_horas")
public class BancoHoras {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Funcionario dono deste registro.
     */
    @ManyToOne
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    /**
     * Tipo da movimentacao:
     *   CREDITO = horas sendo adicionadas ao banco
     *   DEBITO  = horas sendo retiradas do banco (folga remunerada)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimentacao tipo;

    /**
     * Quantidade de horas movimentadas (sempre positivo).
     * O tipo (CREDITO ou DEBITO) indica se soma ou subtrai.
     */
    @Column(nullable = false)
    private double horas;

    /**
     * Motivo da movimentacao.
     * Ex: "Horas extras do dia 15/01/2025"
     *     "Folga compensatoria concedida em 20/01/2025"
     */
    @Column(nullable = false)
    private String motivo;

    /**
     * Data e hora em que o registro foi criado.
     */
    private LocalDateTime criadoEm = LocalDateTime.now();

    public enum TipoMovimentacao {
        CREDITO,  // Horas extras ganhas (aumenta o saldo)
        DEBITO    // Folga remunerada usada (diminui o saldo)
    }
}