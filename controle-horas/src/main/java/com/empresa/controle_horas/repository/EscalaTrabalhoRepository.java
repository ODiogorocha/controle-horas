package com.empresa.controle_horas.repository;

import com.empresa.controle_horas.model.EscalaTrabalho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EscalaTrabalhoRepository
        extends JpaRepository<EscalaTrabalho, Long> {

    EscalaTrabalho findByNome(String nome);
    boolean existsByNome(String nome);

    List<EscalaTrabalho> findByTipo(EscalaTrabalho.TipoEscala tipo);
}