package com.empresa.controle_horas.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainApp extends Application {

    private static final String API = "http://localhost:8080/api";
    private final HttpClient http = HttpClient.newHttpClient();

    // Campos da aba Cargos
    private TextField cargoNomeField;
    private TextField horasField;

    // Campos da aba Funcionarios
    private TextField nomeField;
    private TextField matriculaField;
    private TextField cargoIdField;

    // Campos da aba Relatorio
    private TextField relFuncIdField;
    private TextField relInicioField;
    private TextField relFimField;

    // Painel de resultado do relatorio
    private VBox resultadoBox;

    // Console de log
    private TextArea logArea;

    // Barra de status
    private Label statusBar;
    private Rectangle statusIndicator;

    // ── Paleta de cores ───────────────────────────────────────────
    private static final String COR_FUNDO        = "#F0F4F8";
    private static final String COR_PAINEL       = "#FFFFFF";
    private static final String COR_CABECALHO    = "#1A3C5E";
    private static final String COR_ACENTO       = "#2E86C1";
    private static final String COR_VERDE        = "#1E8449";
    private static final String COR_VERMELHO     = "#C0392B";
    private static final String COR_LARANJA      = "#D35400";
    private static final String COR_TEXTO        = "#1C2833";
    private static final String COR_TEXTO_FRACO  = "#566573";
    private static final String COR_BORDA        = "#D5D8DC";
    private static final String COR_CAMPO        = "#FDFEFE";
    private static final String COR_DICA         = "#EBF5FB";
    private static final String COR_DICA_BORDA   = "#AED6F1";
    private static final String COR_SUCESSO_BG   = "#EAFAF1";
    private static final String COR_SUCESSO_BORD = "#A9DFBF";

    @Override
    public void start(Stage stage) {
        stage.setTitle("Sistema de Controle de Horas - Gestao de Ponto");
        stage.setMinWidth(960);
        stage.setMinHeight(680);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + COR_FUNDO + ";");

        root.setTop(criarCabecalho());
        root.setLeft(criarMenuLateral());

        // Area central com abas
        TabPane abas = criarAbas();
        ScrollPane scroll = new ScrollPane(abas);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background: " + COR_FUNDO + "; -fx-background-color: " + COR_FUNDO + ";");
        root.setCenter(scroll);

        root.setBottom(criarBarraInferior());

        Scene scene = new Scene(root, 960, 680);
        stage.setScene(scene);
        stage.show();

        setStatus("Sistema iniciado com sucesso. Servidor rodando em localhost:8080", true);
        log("Bem-vindo ao Sistema de Controle de Horas!");
        log("Siga as abas acima para: cadastrar cargos, funcionarios, importar ponto e ver relatorios.");
    }

    // ── Cabecalho principal ───────────────────────────────────────
    private VBox criarCabecalho() {
        // Faixa superior com gradiente
        HBox topo = new HBox();
        topo.setPrefHeight(72);
        topo.setAlignment(Pos.CENTER_LEFT);
        topo.setPadding(new Insets(0, 24, 0, 24));
        topo.setStyle(
                "-fx-background-color: linear-gradient(to right, #1A3C5E, #2E86C1);"
        );

        // Bloco do titulo
        VBox blocoTitulo = new VBox(2);
        blocoTitulo.setAlignment(Pos.CENTER_LEFT);

        Label titulo = new Label("Sistema de Controle de Horas");
        titulo.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;"
        );

        Label subtitulo = new Label("Gerencie funcionarios, cargos e calcule horas trabalhadas e horas extras");
        subtitulo.setStyle(
                "-fx-text-fill: #AED6F1;" +
                        "-fx-font-size: 11px;"
        );

        blocoTitulo.getChildren().addAll(titulo, subtitulo);

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        // Indicador de conexao
        VBox conexao = new VBox(3);
        conexao.setAlignment(Pos.CENTER_RIGHT);

        statusIndicator = new Rectangle(10, 10);
        statusIndicator.setFill(Color.web("#2ECC71"));
        statusIndicator.setArcWidth(10);
        statusIndicator.setArcHeight(10);

        Label labelConexao = new Label("API Conectada");
        labelConexao.setStyle("-fx-text-fill: #AED6F1; -fx-font-size: 10px;");

        HBox indicadorBox = new HBox(6, statusIndicator, labelConexao);
        indicadorBox.setAlignment(Pos.CENTER);

        Label labelServidor = new Label("localhost:8080");
        labelServidor.setStyle("-fx-text-fill: #85C1E9; -fx-font-size: 10px;");
        labelServidor.setAlignment(Pos.CENTER);

        conexao.getChildren().addAll(indicadorBox, labelServidor);

        topo.getChildren().addAll(blocoTitulo, espacador, conexao);

        // Barra de dica logo abaixo do cabecalho
        HBox dica = new HBox();
        dica.setPadding(new Insets(8, 24, 8, 24));
        dica.setAlignment(Pos.CENTER_LEFT);
        dica.setStyle(
                "-fx-background-color: #EBF5FB;" +
                        "-fx-border-color: #AED6F1;" +
                        "-fx-border-width: 0 0 1 0;"
        );

        Label dicaTexto = new Label(
                "COMO USAR:  " +
                        "1. Cadastre um cargo (ex: Analista, 8h/dia)  " +
                        "->  " +
                        "2. Cadastre funcionarios  " +
                        "->  " +
                        "3. Importe o arquivo de ponto (CSV)  " +
                        "->  " +
                        "4. Consulte o relatorio de horas"
        );
        dicaTexto.setStyle(
                "-fx-text-fill: #1A5276;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;"
        );

        dica.getChildren().add(dicaTexto);

        VBox cabecalho = new VBox(topo, dica);
        return cabecalho;
    }

    // ── Menu lateral com instrucoes ───────────────────────────────
    private VBox criarMenuLateral() {
        VBox menu = new VBox(0);
        menu.setPrefWidth(200);
        menu.setStyle(
                "-fx-background-color: #1A3C5E;" +
                        "-fx-padding: 0;"
        );

        Label labelAjuda = new Label("GUIA RAPIDO");
        labelAjuda.setStyle(
                "-fx-text-fill: #85C1E9;" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 16 12 8 16;"
        );

        menu.getChildren().add(labelAjuda);

        // Itens do menu lateral com descricao
        String[][] itens = {
                {"1. Cargos", "Defina funcoes e\ncarga horaria diaria"},
                {"2. Funcionarios", "Cadastre sua\nequipe de trabalho"},
                {"3. Importar Ponto", "Carregue o arquivo\nCSV com os registros"},
                {"4. Relatorio", "Veja horas trabalhadas\ne horas extras"},
        };

        for (String[] item : itens) {
            VBox bloco = new VBox(3);
            bloco.setPadding(new Insets(10, 12, 10, 16));
            bloco.setStyle(
                    "-fx-border-color: transparent transparent #243D5C transparent;" +
                            "-fx-border-width: 0 0 1 0;"
            );

            Label lNome = new Label(item[0]);
            lNome.setStyle(
                    "-fx-text-fill: #D6EAF8;" +
                            "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;"
            );

            Label lDesc = new Label(item[1]);
            lDesc.setStyle(
                    "-fx-text-fill: #85C1E9;" +
                            "-fx-font-size: 10px;"
            );

            bloco.getChildren().addAll(lNome, lDesc);
            menu.getChildren().add(bloco);
        }

        // Espaco restante
        Region espaco = new Region();
        VBox.setVgrow(espaco, Priority.ALWAYS);
        menu.getChildren().add(espaco);

        // Rodape do menu
        VBox rodape = new VBox(4);
        rodape.setPadding(new Insets(12, 12, 16, 16));
        rodape.setStyle("-fx-background-color: #16314F;");

        Label lRodape1 = new Label("Sistema v1.0");
        lRodape1.setStyle("-fx-text-fill: #566573; -fx-font-size: 10px;");

        Label lRodape2 = new Label("Banco: H2 (memoria)");
        lRodape2.setStyle("-fx-text-fill: #566573; -fx-font-size: 10px;");

        Label lRodape3 = new Label("Java 21 + Spring Boot");
        lRodape3.setStyle("-fx-text-fill: #566573; -fx-font-size: 10px;");

        rodape.getChildren().addAll(lRodape1, lRodape2, lRodape3);
        menu.getChildren().add(rodape);

        return menu;
    }

    // ── Abas principais ───────────────────────────────────────────
    private TabPane criarAbas() {
        TabPane abas = new TabPane();
        abas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        abas.setStyle(
                "-fx-background-color: " + COR_FUNDO + ";" +
                        "-fx-tab-min-width: 160px;" +
                        "-fx-tab-min-height: 36px;"
        );

        abas.getTabs().addAll(
                criarAbaCargos(),
                criarAbaFuncionarios(),
                criarAbaImportarCSV(),
                criarAbaRelatorio()
        );

        return abas;
    }

    // ── Aba 1: Cargos ─────────────────────────────────────────────
    private Tab criarAbaCargos() {
        Tab tab = new Tab("  1. Cargos  ");

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + COR_FUNDO + "; -fx-background-color: " + COR_FUNDO + ";");

        VBox conteudo = new VBox(16);
        conteudo.setPadding(new Insets(24));
        conteudo.setStyle("-fx-background-color: " + COR_FUNDO + ";");

        // Explicacao do que e um cargo
        conteudo.getChildren().add(criarCaixaInfo(
                "O que e um Cargo?",
                "Um cargo define a funcao do funcionario na empresa e quantas horas ele deve\n" +
                        "trabalhar por dia. Por exemplo: um 'Analista' pode ter 8 horas diarias,\n" +
                        "enquanto um 'Estagiario' pode ter 6 horas diarias.\n\n" +
                        "Cadastre os cargos primeiro, antes de cadastrar os funcionarios.",
                COR_DICA, COR_DICA_BORDA
        ));

        // Formulario
        VBox painel = criarPainel("Cadastrar Novo Cargo");

        // Campo: Nome do cargo
        VBox campoCargo = criarCampoComAjuda(
                "Nome do Cargo",
                "Digite o nome da funcao, como: Desenvolvedor, Analista, Gerente, Estagiario",
                "Exemplo: Desenvolvedor"
        );
        cargoNomeField = (TextField) ((VBox) campoCargo).getChildren().get(1);

        // Campo: Horas diarias
        VBox campoHoras = criarCampoComAjuda(
                "Horas de Trabalho por Dia",
                "Quantas horas este cargo deve trabalhar por dia. Use ponto para decimal.\n" +
                        "Exemplos: 8.0 = oito horas completas | 6.0 = seis horas | 4.5 = quatro horas e meia",
                "Exemplo: 8.0"
        );
        horasField = (TextField) ((VBox) campoHoras).getChildren().get(1);

        HBox botoes = new HBox(12);
        botoes.setAlignment(Pos.CENTER_LEFT);
        botoes.setPadding(new Insets(8, 0, 0, 0));

        Button btnSalvar = criarBotaoPrimario("Salvar Cargo");
        btnSalvar.setOnAction(e -> criarCargo());

        Button btnListar = criarBotaoSecundario("Ver Todos os Cargos");
        btnListar.setOnAction(e -> listarCargos());

        botoes.getChildren().addAll(btnSalvar, btnListar);

        painel.getChildren().addAll(campoCargo, new Separator(), campoHoras, botoes);
        conteudo.getChildren().add(painel);

        // Exemplo de uso
        conteudo.getChildren().add(criarCaixaInfo(
                "Exemplo de Cadastro",
                "Cargo: Desenvolvedor  ->  Horas por dia: 8.0\n" +
                        "Cargo: Estagiario     ->  Horas por dia: 6.0\n" +
                        "Cargo: Gerente        ->  Horas por dia: 8.0\n\n" +
                        "Depois de salvar, va para a aba '2. Funcionarios' para cadastrar sua equipe.",
                COR_SUCESSO_BG, COR_SUCESSO_BORD
        ));

        scroll.setContent(conteudo);
        tab.setContent(scroll);
        return tab;
    }

    // ── Aba 2: Funcionarios ───────────────────────────────────────
    private Tab criarAbaFuncionarios() {
        Tab tab = new Tab("  2. Funcionarios  ");

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + COR_FUNDO + "; -fx-background-color: " + COR_FUNDO + ";");

        VBox conteudo = new VBox(16);
        conteudo.setPadding(new Insets(24));
        conteudo.setStyle("-fx-background-color: " + COR_FUNDO + ";");

        conteudo.getChildren().add(criarCaixaInfo(
                "Antes de comecar",
                "Voce precisa ter pelo menos um Cargo cadastrado antes de cadastrar funcionarios.\n" +
                        "Se ainda nao cadastrou, volte para a aba '1. Cargos'.\n\n" +
                        "A matricula e o codigo unico de cada funcionario. Ela sera usada no arquivo\n" +
                        "de ponto (CSV) para identificar quem trabalhou em cada dia.",
                COR_DICA, COR_DICA_BORDA
        ));

        VBox painel = criarPainel("Cadastrar Novo Funcionario");

        // Campo: Nome
        VBox campoNome = criarCampoComAjuda(
                "Nome Completo do Funcionario",
                "Digite o nome completo como aparece nos documentos da empresa.",
                "Exemplo: Maria Oliveira Santos"
        );
        nomeField = (TextField) ((VBox) campoNome).getChildren().get(1);

        // Campo: Matricula
        VBox campoMatricula = criarCampoComAjuda(
                "Matricula (codigo do funcionario)",
                "A matricula e um codigo unico para identificar o funcionario.\n" +
                        "Este mesmo codigo deve aparecer no arquivo CSV de ponto.\n" +
                        "Use letras e numeros, sem espacos.",
                "Exemplo: EMP001"
        );
        matriculaField = (TextField) ((VBox) campoMatricula).getChildren().get(1);

        // Campo: ID do Cargo
        VBox campoCargoId = criarCampoComAjuda(
                "ID do Cargo",
                "Informe o numero do cargo cadastrado anteriormente.\n" +
                        "Para ver os IDs dos cargos, clique em 'Ver Todos os Cargos' na aba anterior.\n" +
                        "O ID e o numero que aparece na resposta, no campo 'id'.",
                "Exemplo: 1"
        );
        cargoIdField = (TextField) ((VBox) campoCargoId).getChildren().get(1);

        HBox botoes = new HBox(12);
        botoes.setAlignment(Pos.CENTER_LEFT);
        botoes.setPadding(new Insets(8, 0, 0, 0));

        Button btnSalvar = criarBotaoPrimario("Salvar Funcionario");
        btnSalvar.setOnAction(e -> criarFuncionario());

        Button btnListar = criarBotaoSecundario("Ver Todos os Funcionarios");
        btnListar.setOnAction(e -> listarFuncionarios());

        botoes.getChildren().addAll(btnSalvar, btnListar);

        painel.getChildren().addAll(campoNome, new Separator(), campoMatricula,
                new Separator(), campoCargoId, botoes);
        conteudo.getChildren().add(painel);

        conteudo.getChildren().add(criarCaixaInfo(
                "Proximo Passo",
                "Depois de cadastrar os funcionarios, va para a aba '3. Importar Ponto'\n" +
                        "para carregar o arquivo CSV com os registros de entrada e saida.",
                COR_SUCESSO_BG, COR_SUCESSO_BORD
        ));

        scroll.setContent(conteudo);
        tab.setContent(scroll);
        return tab;
    }

    // ── Aba 3: Importar CSV ───────────────────────────────────────
    private Tab criarAbaImportarCSV() {
        Tab tab = new Tab("  3. Importar Ponto  ");

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + COR_FUNDO + "; -fx-background-color: " + COR_FUNDO + ";");

        VBox conteudo = new VBox(16);
        conteudo.setPadding(new Insets(24));
        conteudo.setStyle("-fx-background-color: " + COR_FUNDO + ";");

        conteudo.getChildren().add(criarCaixaInfo(
                "O que e um arquivo CSV?",
                "CSV e um arquivo de planilha simples que pode ser criado no Excel ou LibreOffice.\n" +
                        "Ele registra quem trabalhou, em qual data, e o horario de entrada e saida.\n" +
                        "O sistema vai ler este arquivo e calcular automaticamente as horas de cada funcionario.",
                COR_DICA, COR_DICA_BORDA
        ));

        // Mostra o formato do CSV
        VBox painelFormato = criarPainel("Como deve ser o arquivo CSV");

        Label lFormatoTitulo = new Label("O arquivo deve ter exatamente estas 4 colunas na primeira linha:");
        lFormatoTitulo.setStyle("-fx-text-fill: " + COR_TEXTO + "; -fx-font-size: 12px;");

        // Tabela visual do formato
        GridPane tabela = new GridPane();
        tabela.setHgap(0);
        tabela.setVgap(0);
        tabela.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + COR_BORDA + ";" +
                        "-fx-border-width: 1;"
        );

        String[] colunas = {"matricula", "data", "entrada", "saida"};
        String[] descricoes = {
                "Codigo do funcionario\n(ex: EMP001)",
                "Data no formato\nANO-MES-DIA\n(ex: 2025-01-15)",
                "Hora de entrada\nno formato HH:MM\n(ex: 08:00)",
                "Hora de saida\nno formato HH:MM\n(ex: 17:30)"
        };

        for (int i = 0; i < colunas.length; i++) {
            VBox celula = new VBox(4);
            celula.setPadding(new Insets(10, 16, 10, 16));
            celula.setStyle(
                    "-fx-background-color: " + COR_CABECALHO + ";" +
                            (i < colunas.length - 1 ? "-fx-border-color: transparent #2C5282 transparent transparent; -fx-border-width: 0 1 0 0;" : "")
            );

            Label lCol = new Label(colunas[i]);
            lCol.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");

            celula.getChildren().add(lCol);
            tabela.add(celula, i, 0);

            VBox celulaDesc = new VBox(4);
            celulaDesc.setPadding(new Insets(8, 16, 8, 16));
            celulaDesc.setStyle(
                    "-fx-background-color: " + COR_CAMPO + ";" +
                            (i < colunas.length - 1 ? "-fx-border-color: transparent " + COR_BORDA + " transparent transparent; -fx-border-width: 0 1 0 0;" : "") +
                            "-fx-border-color: " + COR_BORDA + " " +
                            (i < colunas.length - 1 ? COR_BORDA : "transparent") +
                            " transparent transparent; -fx-border-width: 0 1 0 0;"
            );

            Label lDesc = new Label(descricoes[i]);
            lDesc.setStyle("-fx-text-fill: " + COR_TEXTO_FRACO + "; -fx-font-size: 11px;");
            lDesc.setWrapText(true);

            celulaDesc.getChildren().add(lDesc);
            tabela.add(celulaDesc, i, 1);
        }

        // Exemplo do arquivo
        Label lExemploTitulo = new Label("Exemplo de arquivo CSV:");
        lExemploTitulo.setStyle(
                "-fx-text-fill: " + COR_TEXTO + ";" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 0 4 0;"
        );

        Label lExemplo = new Label(
                "matricula,data,entrada,saida\n" +
                        "EMP001,2025-01-13,08:00,17:30\n" +
                        "EMP001,2025-01-14,08:15,18:00\n" +
                        "EMP002,2025-01-13,09:00,18:30\n" +
                        "EMP002,2025-01-14,08:00,17:00"
        );
        lExemplo.setStyle(
                "-fx-background-color: #1C2833;" +
                        "-fx-text-fill: #58D68D;" +
                        "-fx-font-family: monospace;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 12 16;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;"
        );
        lExemplo.setMaxWidth(Double.MAX_VALUE);

        painelFormato.getChildren().addAll(lFormatoTitulo, tabela, lExemploTitulo, lExemplo);
        conteudo.getChildren().add(painelFormato);

        // Painel de upload
        VBox painelUpload = criarPainel("Selecionar e Importar o Arquivo");

        Label lUploadDesc = new Label(
                "Clique no botao abaixo para escolher o arquivo CSV do seu computador.\n" +
                        "Certifique-se de que as matriculas no arquivo ja estao cadastradas no sistema."
        );
        lUploadDesc.setStyle("-fx-text-fill: " + COR_TEXTO + "; -fx-font-size: 12px;");
        lUploadDesc.setWrapText(true);

        Label arquivoLabel = new Label("Nenhum arquivo selecionado ainda.");
        arquivoLabel.setStyle(
                "-fx-text-fill: " + COR_TEXTO_FRACO + ";" +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 6 10;" +
                        "-fx-background-color: " + COR_FUNDO + ";" +
                        "-fx-border-color: " + COR_BORDA + ";" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;"
        );
        arquivoLabel.setMaxWidth(Double.MAX_VALUE);

        final File[] arquivoSelecionado = {null};

        Button btnEscolher = criarBotaoSecundario("Escolher arquivo CSV...");
        btnEscolher.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Selecionar arquivo de ponto");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Arquivos CSV", "*.csv"));
            File f = fc.showOpenDialog(btnEscolher.getScene().getWindow());
            if (f != null) {
                arquivoSelecionado[0] = f;
                arquivoLabel.setText("Arquivo selecionado: " + f.getName()
                        + "  (" + f.length() / 1024 + " KB)");
                arquivoLabel.setStyle(
                        "-fx-text-fill: " + COR_VERDE + ";" +
                                "-fx-font-size: 11px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 6 10;" +
                                "-fx-background-color: " + COR_SUCESSO_BG + ";" +
                                "-fx-border-color: " + COR_SUCESSO_BORD + ";" +
                                "-fx-border-radius: 4;" +
                                "-fx-background-radius: 4;"
                );
            }
        });

        Button btnImportar = criarBotaoPrimario("Importar Registros de Ponto");
        btnImportar.setStyle(btnImportar.getStyle()
                .replace(COR_ACENTO, COR_VERDE).replace("#2471A3", "#1A7A3F"));
        btnImportar.setOnAction(e -> {
            if (arquivoSelecionado[0] == null) {
                log("ATENCAO: Selecione um arquivo CSV antes de importar.");
                setStatus("Selecione um arquivo CSV antes de importar.", false);
                return;
            }
            importarCSV(arquivoSelecionado[0]);
        });

        HBox botoesUpload = new HBox(12, btnEscolher, btnImportar);
        botoesUpload.setAlignment(Pos.CENTER_LEFT);
        botoesUpload.setPadding(new Insets(4, 0, 0, 0));

        painelUpload.getChildren().addAll(lUploadDesc, arquivoLabel, botoesUpload);
        conteudo.getChildren().add(painelUpload);

        scroll.setContent(conteudo);
        tab.setContent(scroll);
        return tab;
    }

    // ── Aba 4: Relatorio ──────────────────────────────────────────
    private Tab criarAbaRelatorio() {
        Tab tab = new Tab("  4. Relatorio de Horas  ");

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + COR_FUNDO + "; -fx-background-color: " + COR_FUNDO + ";");

        VBox conteudo = new VBox(16);
        conteudo.setPadding(new Insets(24));
        conteudo.setStyle("-fx-background-color: " + COR_FUNDO + ";");

        conteudo.getChildren().add(criarCaixaInfo(
                "O que o relatorio mostra?",
                "O relatorio calcula, para o periodo informado:\n" +
                        "- Total de horas que o funcionario trabalhou\n" +
                        "- Total de horas que ele DEVERIA ter trabalhado (baseado no cargo)\n" +
                        "- Horas extras (trabalhou mais do que o esperado)\n" +
                        "- Horas faltando (trabalhou menos do que o esperado)",
                COR_DICA, COR_DICA_BORDA
        ));

        VBox painelFiltros = criarPainel("Filtros do Relatorio");

        // Campo: ID do funcionario
        VBox campoId = criarCampoComAjuda(
                "ID do Funcionario",
                "Informe o numero de identificacao do funcionario.\n" +
                        "Para ver os IDs, va em '2. Funcionarios' e clique em 'Ver Todos os Funcionarios'.",
                "Exemplo: 1"
        );
        relFuncIdField = (TextField) ((VBox) campoId).getChildren().get(1);

        // Campo: Data inicio
        VBox campoInicio = criarCampoComAjuda(
                "Data de Inicio do Periodo",
                "Data inicial do periodo a ser calculado, no formato ANO-MES-DIA.",
                "Exemplo: 2025-01-01"
        );
        relInicioField = (TextField) ((VBox) campoInicio).getChildren().get(1);
        relInicioField.setText(LocalDate.now().withDayOfMonth(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        // Campo: Data fim
        VBox campoFim = criarCampoComAjuda(
                "Data de Fim do Periodo",
                "Data final do periodo a ser calculado, no formato ANO-MES-DIA.",
                "Exemplo: 2025-01-31"
        );
        relFimField = (TextField) ((VBox) campoFim).getChildren().get(1);
        relFimField.setText(LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        HBox botoes = new HBox(12);
        botoes.setAlignment(Pos.CENTER_LEFT);
        botoes.setPadding(new Insets(8, 0, 0, 0));

        Button btnGerar = criarBotaoPrimario("Gerar Relatorio");
        btnGerar.setOnAction(e -> gerarRelatorio());

        Button btnGeral = criarBotaoSecundario("Ver Relatorio de Todos");
        btnGeral.setOnAction(e -> gerarRelatorioGeral());

        Label lDicaBotoes = new Label(
                "'Gerar Relatorio' mostra apenas o funcionario informado.  " +
                        "'Ver Relatorio de Todos' mostra todos os funcionarios de uma vez."
        );
        lDicaBotoes.setStyle("-fx-text-fill: " + COR_TEXTO_FRACO + "; -fx-font-size: 11px;");
        lDicaBotoes.setWrapText(true);

        botoes.getChildren().addAll(btnGerar, btnGeral);

        painelFiltros.getChildren().addAll(campoId, new Separator(),
                campoInicio, new Separator(), campoFim, botoes, lDicaBotoes);
        conteudo.getChildren().add(painelFiltros);

        // Painel de resultado (dinamico)
        resultadoBox = new VBox(0);
        resultadoBox.setStyle("-fx-background-color: " + COR_FUNDO + ";");
        conteudo.getChildren().add(resultadoBox);

        scroll.setContent(conteudo);
        tab.setContent(scroll);
        return tab;
    }

    // ── Barra inferior com console ────────────────────────────────
    private VBox criarBarraInferior() {
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(120);
        logArea.setStyle(
                "-fx-control-inner-background: #0D1117;" +
                        "-fx-text-fill: #58D68D;" +
                        "-fx-font-family: monospace;" +
                        "-fx-font-size: 11px;" +
                        "-fx-border-color: " + COR_BORDA + ";" +
                        "-fx-border-width: 1 0 0 0;"
        );

        HBox cabecalhoConsole = new HBox();
        cabecalhoConsole.setPadding(new Insets(4, 12, 4, 12));
        cabecalhoConsole.setAlignment(Pos.CENTER_LEFT);
        cabecalhoConsole.setStyle(
                "-fx-background-color: #1C2833;" +
                        "-fx-border-color: " + COR_BORDA + ";" +
                        "-fx-border-width: 1 0 0 0;"
        );

        Label lConsole = new Label("Console de Atividades  (aqui aparecem as respostas do sistema)");
        lConsole.setStyle("-fx-text-fill: #85C1E9; -fx-font-size: 10px; -fx-font-weight: bold;");

        Region esp = new Region();
        HBox.setHgrow(esp, Priority.ALWAYS);

        Button btnLimpar = new Button("Limpar");
        btnLimpar.setStyle(
                "-fx-background-color: #2C3E50;" +
                        "-fx-text-fill: #85C1E9;" +
                        "-fx-font-size: 10px;" +
                        "-fx-padding: 2 8;" +
                        "-fx-cursor: hand;"
        );
        btnLimpar.setOnAction(e -> logArea.clear());

        cabecalhoConsole.getChildren().addAll(lConsole, esp, btnLimpar);

        statusBar = new Label("Pronto.");
        statusBar.setMaxWidth(Double.MAX_VALUE);
        statusBar.setStyle(
                "-fx-background-color: " + COR_VERDE + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 4 12;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;"
        );

        return new VBox(cabecalhoConsole, logArea, statusBar);
    }

    // ── Helpers de chamada a API ──────────────────────────────────
    private void criarCargo() {
        String nome  = cargoNomeField.getText().trim();
        String horas = horasField.getText().trim();

        if (nome.isEmpty() || horas.isEmpty()) {
            log("ERRO: Preencha o nome do cargo e as horas diarias antes de salvar.");
            setStatus("Preencha todos os campos do cargo.", false);
            return;
        }

        try {
            Double.parseDouble(horas);
        } catch (NumberFormatException ex) {
            log("ERRO: O campo 'Horas por dia' deve ser um numero. Use ponto: 8.0");
            setStatus("Horas invalidas. Use formato: 8.0", false);
            return;
        }

        String json = String.format("{\"nome\":\"%s\",\"horasDiarias\":%s}", nome, horas);
        post("/cargos", json, "Cargo '" + nome + "' salvo com sucesso!");
        cargoNomeField.clear();
        horasField.clear();
    }

    private void listarCargos() {
        get("/cargos", "Lista de cargos carregada. Veja o console abaixo.");
    }

    private void criarFuncionario() {
        String nome      = nomeField.getText().trim();
        String matricula = matriculaField.getText().trim();
        String cargoId   = cargoIdField.getText().trim();

        if (nome.isEmpty() || matricula.isEmpty() || cargoId.isEmpty()) {
            log("ERRO: Preencha todos os campos: nome, matricula e ID do cargo.");
            setStatus("Preencha todos os campos do funcionario.", false);
            return;
        }

        try {
            Long.parseLong(cargoId);
        } catch (NumberFormatException ex) {
            log("ERRO: O ID do cargo deve ser um numero inteiro. Exemplo: 1");
            setStatus("ID do cargo invalido.", false);
            return;
        }

        String json = String.format(
                "{\"nome\":\"%s\",\"matricula\":\"%s\",\"cargo\":{\"id\":%s}}",
                nome, matricula, cargoId);
        post("/funcionarios", json, "Funcionario '" + nome + "' salvo com sucesso!");
        nomeField.clear();
        matriculaField.clear();
        cargoIdField.clear();
    }

    private void listarFuncionarios() {
        get("/funcionarios", "Lista de funcionarios carregada. Veja o console abaixo.");
    }

    private void importarCSV(File arquivo) {
        new Thread(() -> {
            try {
                Platform.runLater(() -> {
                    log("Enviando arquivo: " + arquivo.getName() + "...");
                    setStatus("Importando arquivo CSV...", true);
                });

                String boundary = "----Boundary" + System.currentTimeMillis();
                byte[] fileBytes = Files.readAllBytes(arquivo.toPath());

                String bodyStart = "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"arquivo\"; filename=\""
                        + arquivo.getName() + "\"\r\n"
                        + "Content-Type: text/csv\r\n\r\n";
                String bodyEnd = "\r\n--" + boundary + "--\r\n";

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                out.write(bodyStart.getBytes());
                out.write(fileBytes);
                out.write(bodyEnd.getBytes());

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(API + "/ponto/importar"))
                        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                        .POST(HttpRequest.BodyPublishers.ofByteArray(out.toByteArray()))
                        .build();

                HttpResponse<String> resp = http.send(req,
                        HttpResponse.BodyHandlers.ofString());

                Platform.runLater(() -> {
                    log("Resposta da importacao:\n" + resp.body());
                    setStatus("Arquivo importado! Veja o resultado no console.", true);
                });

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    log("ERRO ao importar: " + ex.getMessage());
                    setStatus("Erro ao importar arquivo. Veja o console.", false);
                });
            }
        }).start();
    }

    private void gerarRelatorio() {
        String id    = relFuncIdField.getText().trim();
        String inicio = relInicioField.getText().trim();
        String fim   = relFimField.getText().trim();

        if (id.isEmpty()) {
            log("ERRO: Informe o ID do funcionario.");
            setStatus("Informe o ID do funcionario.", false);
            return;
        }

        setStatus("Calculando relatorio...", true);
        String url = "/ponto/relatorio?funcionarioId=" + id
                + "&inicio=" + inicio + "&fim=" + fim;

        new Thread(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(API + url))
                        .GET().build();
                HttpResponse<String> resp = http.send(req,
                        HttpResponse.BodyHandlers.ofString());
                Platform.runLater(() -> {
                    log("Relatorio gerado:\n" + resp.body());
                    exibirResultadoRelatorio(resp.body());
                    setStatus("Relatorio gerado com sucesso!", true);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    log("ERRO: " + ex.getMessage());
                    setStatus("Erro ao gerar relatorio.", false);
                });
            }
        }).start();
    }

    private void gerarRelatorioGeral() {
        String inicio = relInicioField.getText().trim();
        String fim    = relFimField.getText().trim();
        setStatus("Calculando relatorio geral...", true);
        get("/ponto/relatorio/geral?inicio=" + inicio + "&fim=" + fim,
                "Relatorio geral carregado. Veja o console abaixo.");
    }

    // Exibe resultado do relatorio em cards visuais
    private void exibirResultadoRelatorio(String json) {
        resultadoBox.getChildren().clear();

        VBox painel = criarPainel("Resultado do Relatorio");
        painel.setStyle(painel.getStyle() +
                "-fx-border-color: " + COR_SUCESSO_BORD + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 8;"
        );

        // Parse manual simples do JSON para exibir os campos
        String[] campos = {
                "funcionario", "matricula", "cargo", "periodo",
                "diasTrabalhados", "horasTrabalhadas", "horasEsperadas",
                "horasExtras", "horasFaltando", "status"
        };
        String[] rotulos = {
                "Funcionario", "Matricula", "Cargo", "Periodo analisado",
                "Dias trabalhados", "Total de horas trabalhadas",
                "Total de horas esperadas", "Horas extras",
                "Horas faltando", "Situacao"
        };

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);
        grid.setPadding(new Insets(8, 0, 8, 0));

        int linha = 0;
        for (int i = 0; i < campos.length; i++) {
            String campo = campos[i];
            String valor = extrairValorJson(json, campo);
            if (valor.isEmpty()) continue;

            Label lRotulo = new Label(rotulos[i] + ":");
            lRotulo.setStyle(
                    "-fx-text-fill: " + COR_TEXTO_FRACO + ";" +
                            "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;"
            );
            lRotulo.setMinWidth(200);

            Label lValor = new Label(valor);
            boolean ehHoraExtra = campo.equals("horasExtras")
                    && !valor.startsWith("0.00");
            boolean ehFaltando  = campo.equals("horasFaltando")
                    && !valor.startsWith("0.00");
            boolean ehStatus    = campo.equals("status");

            String corValor = COR_TEXTO;
            if (ehHoraExtra)  corValor = COR_LARANJA;
            if (ehFaltando)   corValor = COR_VERMELHO;
            if (ehStatus && valor.contains("extras"))  corValor = COR_LARANJA;
            if (ehStatus && valor.contains("cumprida")) corValor = COR_VERDE;
            if (ehStatus && valor.contains("faltando")) corValor = COR_VERMELHO;

            lValor.setStyle(
                    "-fx-text-fill: " + corValor + ";" +
                            "-fx-font-size: 13px;" +
                            (ehStatus ? "-fx-font-weight: bold;" : "")
            );

            grid.add(lRotulo, 0, linha);
            grid.add(lValor, 1, linha);
            linha++;

            // Separador apos cada grupo
            if (campo.equals("periodo") || campo.equals("horasEsperadas")) {
                Separator sep = new Separator();
                sep.setPadding(new Insets(2, 0, 2, 0));
                GridPane.setColumnSpan(sep, 2);
                grid.add(sep, 0, linha++);
            }
        }

        painel.getChildren().add(grid);
        resultadoBox.getChildren().add(painel);
    }

    private String extrairValorJson(String json, String chave) {
        String busca = "\"" + chave + "\":";
        int idx = json.indexOf(busca);
        if (idx == -1) return "";
        int inicio = idx + busca.length();
        char primeiroChar = json.charAt(inicio);
        if (primeiroChar == '"') {
            int fim = json.indexOf('"', inicio + 1);
            return json.substring(inicio + 1, fim);
        } else {
            int fim = json.indexOf(',', inicio);
            if (fim == -1) fim = json.indexOf('}', inicio);
            return json.substring(inicio, fim).trim();
        }
    }

    // ── Helpers HTTP ──────────────────────────────────────────────
    private void get(String path, String msgSucesso) {
        new Thread(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(API + path))
                        .GET().build();
                HttpResponse<String> resp = http.send(req,
                        HttpResponse.BodyHandlers.ofString());
                Platform.runLater(() -> {
                    log(resp.body());
                    setStatus(msgSucesso, true);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    log("ERRO: " + ex.getMessage());
                    setStatus("Erro de comunicacao. O servidor esta rodando?", false);
                });
            }
        }).start();
    }

    private void post(String path, String json, String msgSucesso) {
        new Thread(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(API + path))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
                HttpResponse<String> resp = http.send(req,
                        HttpResponse.BodyHandlers.ofString());
                Platform.runLater(() -> {
                    log("Resposta: " + resp.body());
                    setStatus(msgSucesso, true);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    log("ERRO: " + ex.getMessage());
                    setStatus("Erro de comunicacao. O servidor esta rodando?", false);
                });
            }
        }).start();
    }

    // ── Helpers de UI ─────────────────────────────────────────────

    // Cria um painel branco com titulo
    private VBox criarPainel(String titulo) {
        VBox painel = new VBox(14);
        painel.setPadding(new Insets(20));
        painel.setStyle(
                "-fx-background-color: " + COR_PAINEL + ";" +
                        "-fx-border-color: " + COR_BORDA + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 8, 0, 0, 2);"
        );

        Label lTitulo = new Label(titulo);
        lTitulo.setStyle(
                "-fx-text-fill: " + COR_CABECALHO + ";" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );

        Separator sep = new Separator();
        sep.setPadding(new Insets(0, 0, 4, 0));

        painel.getChildren().addAll(lTitulo, sep);
        return painel;
    }

    // Cria um campo com label, caixa de texto e texto de ajuda
    private VBox criarCampoComAjuda(String rotulo, String ajuda, String placeholder) {
        VBox box = new VBox(4);

        Label lRotulo = new Label(rotulo);
        lRotulo.setStyle(
                "-fx-text-fill: " + COR_TEXTO + ";" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
        );

        TextField campo = new TextField();
        campo.setPromptText(placeholder);
        campo.setMaxWidth(340);
        campo.setStyle(
                "-fx-background-color: " + COR_CAMPO + ";" +
                        "-fx-text-fill: " + COR_TEXTO + ";" +
                        "-fx-border-color: " + COR_BORDA + ";" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;" +
                        "-fx-padding: 7 10;" +
                        "-fx-font-size: 12px;"
        );

        Label lAjuda = new Label(ajuda);
        lAjuda.setStyle(
                "-fx-text-fill: " + COR_TEXTO_FRACO + ";" +
                        "-fx-font-size: 11px;"
        );
        lAjuda.setWrapText(true);

        box.getChildren().addAll(lRotulo, campo, lAjuda);
        return box;
    }

    // Cria uma caixa de informacao colorida
    private VBox criarCaixaInfo(String titulo, String texto,
                                String corFundo, String corBorda) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(14, 16, 14, 16));
        box.setStyle(
                "-fx-background-color: " + corFundo + ";" +
                        "-fx-border-color: " + corBorda + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;"
        );

        Label lTitulo = new Label(titulo);
        lTitulo.setStyle(
                "-fx-text-fill: " + COR_CABECALHO + ";" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
        );

        Label lTexto = new Label(texto);
        lTexto.setStyle(
                "-fx-text-fill: " + COR_TEXTO + ";" +
                        "-fx-font-size: 11px;"
        );
        lTexto.setWrapText(true);

        box.getChildren().addAll(lTitulo, lTexto);
        return box;
    }

    private Button criarBotaoPrimario(String texto) {
        Button b = new Button(texto);
        b.setStyle(
                "-fx-background-color: " + COR_ACENTO + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 9 20;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;"
        );
        b.setOnMouseEntered(e -> b.setStyle(b.getStyle()
                .replace(COR_ACENTO, "#2471A3")));
        b.setOnMouseExited(e -> b.setStyle(b.getStyle()
                .replace("#2471A3", COR_ACENTO)));
        return b;
    }

    private Button criarBotaoSecundario(String texto) {
        Button b = new Button(texto);
        b.setStyle(
                "-fx-background-color: " + COR_PAINEL + ";" +
                        "-fx-text-fill: " + COR_ACENTO + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 8 18;" +
                        "-fx-background-radius: 5;" +
                        "-fx-border-color: " + COR_ACENTO + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-cursor: hand;"
        );
        return b;
    }

    private void log(String msg) {
        Platform.runLater(() -> {
            String hora = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            logArea.appendText("[" + hora + "] " + msg + "\n");
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    private void setStatus(String msg, boolean sucesso) {
        Platform.runLater(() -> {
            statusBar.setText("  " + msg);
            String cor = sucesso ? COR_VERDE : COR_VERMELHO;
            statusBar.setStyle(
                    "-fx-background-color: " + cor + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-padding: 4 12;" +
                            "-fx-font-size: 11px;" +
                            "-fx-font-weight: bold;"
            );
            if (statusIndicator != null) {
                statusIndicator.setFill(sucesso
                        ? Color.web("#2ECC71") : Color.web("#E74C3C"));
            }
        });
    }
}