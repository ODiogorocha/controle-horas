package com.empresa.controle_horas.service;

import com.empresa.controle_horas.model.Funcionario;
import com.empresa.controle_horas.model.RegistroPonto;
import com.empresa.controle_horas.repository.FuncionarioRepository;
import com.empresa.controle_horas.repository.RegistroPontoRepository;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor

public class CsvService {
    private final FuncionarioRepository funcRepo;
    private final RegistroPontoRepository pontoRepo;

    /*FuncionarioRepository
     * Formato esperado do CSV:
     * matricula,data,entrada,saida
     * EMP001,2025-01-15,08:00,17:30
     *
    */

    public String importarCsv(MultipartFile arquivo)
        throws Exception{
        int importados = 0;
        int erros = 0;
        List<String> msgs = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(arquivo.getInputStream()))){
            String[] linha;
            boolean primeiraLinha = true;

            while ((linha = reader.readNext()) != null) {
                // pula o cabeçalho
                if(primeiraLinha){
                    primeiraLinha = false;
                    continue;
                }

                try{
                    String matricula = linha[0].trim();
                    LocalDate data = LocalDate.parse(linha[1].trim());
                    LocalTime entrada = LocalTime.parse(linha[2].trim());
                    LocalTime saida = LocalTime.parse(linha[2].trim());

                    Funcionario func = funcRepo.findByMatricula(matricula);

                    if(func == null){
                        erros++;
                        msgs.add("Matricula não encontrada:" + matricula);
                        continue;
                    }

                    RegistroPonto reg = new RegistroPonto();
                    reg.setFuncionario(func);
                    reg.setData(data);
                    reg.setEntrada(entrada);
                    reg.setSaida(saida);
                    pontoRepo.save(reg);
                    importados++;
                } catch (Exception e){
                    erros++;
                    msgs.add("Erro na linha:" + e.getMessage());
                }
            }
        }
        return importados + "registrosimportados, " + erros + "erros. " + msgs;
    }
}
