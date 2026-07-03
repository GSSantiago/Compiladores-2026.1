package br.ufscar.dc.compiladores.linguagem.la;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class Principal {
    public static void main(String[] args) {
        File caminho = new File(args.length > 0 ? args[0] : "casos_de_teste");

        if (caminho.isDirectory()) {
            System.out.println("Testando arquivos da pasta: " + caminho.getPath());
            File[] arquivos = caminho.listFiles((dir, name) -> name.endsWith(".sim"));
            if (arquivos != null) {
                for (File arquivo : arquivos) testarArquivo(arquivo.getPath());
            }
        } else {
            testarArquivo(caminho.getPath());
        }
    }

    private static void testarArquivo(String arquivoTeste) {
        try {
            CharStream cs = CharStreams.fromFileName(arquivoTeste);
            SimLangLexer lexer = new SimLangLexer(cs);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            SimLangParser parser = new SimLangParser(tokens);
            SimLangParser.ProgramaContext arvore = parser.programa();

            if (parser.getNumberOfSyntaxErrors() > 0) return; 

            SimLangSemanticoUtils.errosSemanticos.clear();
            SimLangSemantico semantico = new SimLangSemantico();
            semantico.visit(arvore);

            if (SimLangSemanticoUtils.errosSemanticos.isEmpty()) {
                SimLangGeradorPython gerador = new SimLangGeradorPython(semantico.getTabela());
                gerador.visit(arvore);

                // GERA APENAS O ARQUIVO .PY SOLTO
                String caminhoSaida = arquivoTeste.replace(".sim", ".py");
                try (PrintWriter pw = new PrintWriter(caminhoSaida, StandardCharsets.UTF_8)) {
                    pw.print(gerador.saida.toString());
                    System.out.println("Código Python gerado: " + caminhoSaida);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }
}