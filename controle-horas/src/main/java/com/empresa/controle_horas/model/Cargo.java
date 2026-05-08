package com.empresa.controle_horas.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cargos")

public class Cargo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private  Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    //horas a serem trabalhadas por dia ex: 8.0
    @Column(nullable = true)
    private double horasDiarias;

}
