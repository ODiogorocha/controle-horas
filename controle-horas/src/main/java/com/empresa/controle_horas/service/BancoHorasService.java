package com.empresa.controle_horas.service;

import com.empresa.controle_horas.model.*;
import com.empresa.controle_horas.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BancoHorasService {

    private final BancoHorasRepository bancoHorasRepository;
    private final FolgaRepository folgaRepository;
    private final FuncionarioRepository funcionarioRepository;

    public double consultarSaldo(Long funcionarioId) {
        Funcionario funcionario = buscarFuncionario(funcionarioId);
        return bancoHorasRepository.calcularSaldo(funcionario);
    }

    @Transactional
    public Map<String, Object> creditarHoras(Long funcionarioId,
                                              double horas,
                                              String motivo) {
        if (horas <= 0)
            throw new RuntimeException("A quantidade de horas deve ser maior que zero.");

        Funcionario funcionario = buscarFuncionario(funcionarioId);

        BancoHoras registro = new BancoHoras();
        registro.setFuncionario(funcionario);
        registro.setTipo(BancoHoras.TipoMovimentacao.CREDITO);
        registro.setHoras(horas);
        registro.setMotivo(motivo);
        bancoHorasRepository.save(registro);

        double novoSaldo = bancoHorasRepository.calcularSaldo(funcionario);

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("funcionario",   funcionario.getNome());
        resultado.put("operacao",      "CREDITO (horas extras adicionadas)");
        resultado.put("horasCreditadas", String.format("%.2f h", horas));
        resultado.put("saldoAtual",    String.format("%.2f h", novoSaldo));
        resultado.put("motivo",        motivo);
        return resultado;
    }

    @Transactional
    public Map<String, Object> registrarFolgaRemunerada(Long funcionarioId,
                                                         LocalDate data,
                                                         double horas,
                                                         String observacao) {
        Funcionario funcionario = buscarFuncionario(funcionarioId);

        double saldoAtual = bancoHorasRepository.calcularSaldo(funcionario);
        if (saldoAtual < horas)
            throw new RuntimeException(
                String.format("Saldo insuficiente. Disponivel: %.2f h. Solicitado: %.2f h.",
                    saldoAtual, horas));

        if (folgaRepository.existsByFuncionarioAndData(funcionario, data))
            throw new RuntimeException(
                "Ja existe folga cadastrada para " + funcionario.getNome() + " em " + data);

        Folga folga = new Folga();
        folga.setFuncionario(funcionario);
        folga.setData(data);
        folga.setTipo(Folga.TipoFolga.COMPENSATORIA);
        folga.setHorasCompensadas(horas);
        folga.setObservacao(observacao != null ? observacao : "Folga remunerada - banco de horas");
        folgaRepository.save(folga);

        BancoHoras debito = new BancoHoras();
        debito.setFuncionario(funcionario);
        debito.setTipo(BancoHoras.TipoMovimentacao.DEBITO);
        debito.setHoras(horas);
        debito.setMotivo("Folga remunerada em " + data);
        bancoHorasRepository.save(debito);

        double novoSaldo = bancoHorasRepository.calcularSaldo(funcionario);

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("funcionario",     funcionario.getNome());
        resultado.put("dataFolga",       data.toString());
        resultado.put("horasUtilizadas", String.format("%.2f h", horas));
        resultado.put("saldoAnterior",   String.format("%.2f h", saldoAtual));
        resultado.put("saldoRestante",   String.format("%.2f h", novoSaldo));
        resultado.put("status",          "Folga remunerada registrada com sucesso!");
        return resultado;
    }

    @Transactional
    public Map<String, Object> registrarFolga(Long funcionarioId,
                                               LocalDate data,
                                               Folga.TipoFolga tipo,
                                               String observacao) {
        Funcionario funcionario = buscarFuncionario(funcionarioId);

        if (folgaRepository.existsByFuncionarioAndData(funcionario, data))
            throw new RuntimeException(
                "Ja existe folga cadastrada para " + funcionario.getNome() + " em " + data);

        Folga folga = new Folga();
        folga.setFuncionario(funcionario);
        folga.setData(data);
        folga.setTipo(tipo);
        folga.setHorasCompensadas(funcionario.getCargo().getHorasDiarias());
        folga.setObservacao(observacao);
        folgaRepository.save(folga);

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("funcionario", funcionario.getNome());
        resultado.put("data",        data.toString());
        resultado.put("tipo",        tipo.toString());
        resultado.put("observacao",  observacao != null ? observacao : "");
        resultado.put("status",      "Folga registrada com sucesso!");
        return resultado;
    }

    public Map<String, Object> extrato(Long funcionarioId) {
        Funcionario funcionario = buscarFuncionario(funcionarioId);
        List<BancoHoras> movimentacoes = bancoHorasRepository
                .findByFuncionarioOrderByCriadoEmDesc(funcionario);
        double saldo        = bancoHorasRepository.calcularSaldo(funcionario);
        double totalCreditos = movimentacoes.stream()
                .filter(m -> m.getTipo() == BancoHoras.TipoMovimentacao.CREDITO)
                .mapToDouble(BancoHoras::getHoras).sum();
        double totalDebitos  = movimentacoes.stream()
                .filter(m -> m.getTipo() == BancoHoras.TipoMovimentacao.DEBITO)
                .mapToDouble(BancoHoras::getHoras).sum();

        List<Map<String, String>> lista = movimentacoes.stream().map(m -> {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("data",   m.getCriadoEm().toLocalDate().toString());
            item.put("tipo",   m.getTipo() == BancoHoras.TipoMovimentacao.CREDITO
                    ? "CREDITO" : "DEBITO");
            item.put("horas",  String.format("%.2f h", m.getHoras()));
            item.put("motivo", m.getMotivo());
            return item;
        }).toList();

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("funcionario",   funcionario.getNome());
        resultado.put("matricula",     funcionario.getMatricula());
        resultado.put("totalCreditos", String.format("%.2f h", totalCreditos));
        resultado.put("totalDebitos",  String.format("%.2f h", totalDebitos));
        resultado.put("saldoAtual",    String.format("%.2f h", saldo));
        resultado.put("movimentacoes", lista);
        return resultado;
    }

    public List<Folga> listarFolgas(Long funcionarioId,
                                     LocalDate inicio, LocalDate fim) {
        Funcionario funcionario = buscarFuncionario(funcionarioId);
        return folgaRepository.findByFuncionarioAndDataBetween(funcionario, inicio, fim);
    }

    private Funcionario buscarFuncionario(Long id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Funcionario nao encontrado com ID: " + id));
    }
}
