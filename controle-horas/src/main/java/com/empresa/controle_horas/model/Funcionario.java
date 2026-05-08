package com.empresa.controle_horas.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Representa um funcionario da empresa.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "funcionarios")
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String matricula;

    /**
     * Cargo do funcionario.
     * Define quantas horas por dia ele deve trabalhar.
     */
    @ManyToOne
    @JoinColumn(name = "cargo_id", nullable = false)
    private Cargo cargo;

    /**
     * Escala de trabalho do funcionario.
     * Define quais dias da semana ou ciclo de revezamento ele trabalha.
     * Opcional: se nao informado, usa os dias de trabalho do cargo.
     */
    @ManyToOne
    @JoinColumn(name = "escala_id")
    private EscalaTrabalho escala;
}