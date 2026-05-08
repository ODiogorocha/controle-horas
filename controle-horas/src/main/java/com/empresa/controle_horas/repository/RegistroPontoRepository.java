package com.empresa.controle_horas.repository;

import com.empresa.controle_horas.model.Funcionario;
import com.empresa.controle_horas.model.RegistroPonto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RegistroPontoRepository
    extends JpaRepository<RegistroPonto, Long> {

    //buscar registros de um funcionario em um periodo
    List<RegistroPonto> findByFuncionarioAndDataBetween(
            Funcionario funcionario,
            LocalDate inicio,
            LocalDate fim
    );
    List<RegistroPonto> findByFuncionario(Funcionario funcionario);
}
