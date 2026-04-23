package br.ufscar.dc.compiladores.linguagem.la;

import java.io.IOException;
import java.io.PrintWriter;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class Principal {
    public static void main(String[] args) throws IOException {
        // Verifica argumentos de entrada
        if (args.length < 2) {
            System.out.println("Uso: java -jar me-compilador.jar {entrada} {saida}");
            return;
        }

        // Armazena caminhos dos arquivos
        String arquivoEntrada = args[0];
        String arquivoSaida = args[1];

        // Lê o arquivo fonte
        CharStream cs = CharStreams.fromFileName(arquivoEntrada);

        // Cria analisador léxico
        LALexer lex = new LALexer(cs);

        // Cria fluxo de tokens
        CommonTokenStream tokens = new CommonTokenStream(lex);

        // Cria analisador sintático
        LAParser parser = new LAParser(tokens);

        // Abre arquivo de saída
        try (PrintWriter pw = new PrintWriter(arquivoSaida)) {


            // Inicia a análise sintática
            parser.programa();

        } catch (IOException ex) {
            System.err.println("Erro na manipulação do arquivo: " + ex.getMessage());
        }
    }
}