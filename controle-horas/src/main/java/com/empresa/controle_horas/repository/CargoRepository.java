package com.empresa.controle_horas.repository;

import com.empresa.controle_horas.model.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;

//jparepository:
//  Cargo = tipo de entidade
//  Long = tipo do ID


public interface CargoRepository
    extends JpaRepository<Cargo, Long> {
    // O Spring entende o nome do método e cria a query:
    // SELECT * FROM cargo WHERE nome = ?

    Cargo findByNome(String nome);
}
