package com.empresa.controle_horas.repository;

import com.empresa.controle_horas.model.Folga;
import com.empresa.controle_horas.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FolgaRepository extends JpaRepository<Folga, Long> {

    // Todas as folgas de um funcionario em um periodo
    List<Folga> findByFuncionarioAndDataBetween(
            Funcionario funcionario,
            LocalDate inicio,
            LocalDate fim
    );

    // Todas as folgas de um funcionario (sem filtro de data)
    List<Folga> findByFuncionario(Funcionario funcionario);

    // Verifica se ja existe folga cadastrada nessa data para esse funcionario
    boolean existsByFuncionarioAndData(Funcionario funcionario, LocalDate data);

    // Busca folga especifica de um funcionario em uma data
    Optional<Folga> findByFuncionarioAndData(Funcionario funcionario, LocalDate data);

    // Folgas de um tipo especifico em um periodo
    List<Folga> findByFuncionarioAndTipoAndDataBetween(
            Funcionario funcionario,
            Folga.TipoFolga tipo,
            LocalDate inicio,
            LocalDate fim
    );
}