package br.ufscar.dc.compiladores.linguagem.la;

import java.io.IOException;
import java.io.PrintWriter;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

public class Principal {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: java -jar me-compilador.jar {entrada} {saida}");
            return;
        }

        String arquivoEntrada = args[0];
        String arquivoSaida = args[1];

        try (PrintWriter pw = new PrintWriter(arquivoSaida)) {
            
            CharStream cs = CharStreams.fromFileName(arquivoEntrada);
            // LALexer lex = new LALexer(cs);
            
            Token t = null;
            while ((t = lex.nextToken()).getType() != Token.EOF) {
                
                // TODO: Analisar caractere por caracter e printar
                // Exemplo de como printar:
                // pw.print("Imprimindo no arquivo, sem quebra de linha no final");
                // pw.println("...Agora imprimindo com quebra de linha");
                // pw.println("no final");
            }
            
        } catch (IOException ex) {
            System.err.println("Erro na manipulação do arquivo: " + ex.getMessage());
        }
    }
}