package com.empresa.controle_horas.repository;

import com.empresa.controle_horas.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FuncionarioRepository
extends JpaRepository<Funcionario, Long> {
    Funcionario findByMatricula (String matricula);
}
