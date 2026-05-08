package com.empresa.controle_horas.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

/**
 * Representa um dia de folga de um funcionario.
 *
 * Tipos de folga:
 *   DESCANSO       = folga normal prevista na escala
 *   COMPENSATORIA  = folga gerada por horas extras (banco de horas)
 *   FERIADO        = feriado nacional, estadual ou municipal
 *   ABONO          = folga concedida pelo empregador
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "folgas")
public class Folga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Funcionario que tem a folga.
     */
    @ManyToOne
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    /**
     * Data da folga.
     */
    @Column(nullable = false)
    private LocalDate data;

    /**
     * Tipo da folga.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoFolga tipo;

    /**
     * Descricao opcional.
     * Ex: "Feriado Municipal - Aniversario da Cidade"
     *     "Folga compensatoria referente as horas extras de janeiro"
     */
    private String observacao;

    /**
     * Horas de folga remunerada concedidas neste dia.
     * Para folgas compensatorias, registra quantas horas do banco foram usadas.
     * Para outros tipos, normalmente e igual as horas diarias do cargo.
     */
    private double horasCompensadas = 0;

    public enum TipoFolga {
        DESCANSO,       // Folga normal da escala
        COMPENSATORIA,  // Gerada por horas extras (banco de horas)
        FERIADO,        // Feriado
        ABONO           // Concedida pelo empregador
    }
}