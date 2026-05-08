package com.empresa.controle_horas.repository;

import com.empresa.controle_horas.model.BancoHoras;
import com.empresa.controle_horas.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BancoHorasRepository extends JpaRepository<BancoHoras, Long> {

    // Todas as movimentacoes de um funcionario
    List<BancoHoras> findByFuncionarioOrderByCriadoEmDesc(Funcionario funcionario);

    // Apenas creditos (horas extras ganhas)
    List<BancoHoras> findByFuncionarioAndTipo(
            Funcionario funcionario,
            BancoHoras.TipoMovimentacao tipo
    );

    /**
     * Calcula o saldo atual do banco de horas de um funcionario.
     *
     * Soma todos os CREDITOS e subtrai todos os DEBITOS.
     * O resultado e o saldo disponivel para folgas remuneradas.
     *
     * COALESCE evita retorno nulo quando nao ha registros.
     */
    @Query("""
        SELECT
            COALESCE(SUM(CASE WHEN b.tipo = 'CREDITO' THEN b.horas ELSE 0 END), 0)
            -
            COALESCE(SUM(CASE WHEN b.tipo = 'DEBITO'  THEN b.horas ELSE 0 END), 0)
        FROM BancoHoras b
        WHERE b.funcionario = :funcionario
    """)
    double calcularSaldo(@Param("funcionario") Funcionario funcionario);
}