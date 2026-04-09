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
            LALexer lex = new LALexer(cs);
            
            Token t = null;
            while ((t = lex.nextToken()).getType() != Token.EOF) {
                String nomeToken = LALexer.VOCABULARY.getDisplayName(t.getType());
                String texto = t.getText();
                int linha = t.getLine();

                //Erro de comentário aberto que nunca fecha
                if (nomeToken.equals("COMENTARIO_NAO_FECHADO")) {
                    pw.println("Linha " + linha + ": comentario nao fechado");
                    break; 
                } 
                
                //Erro de aspas abertas sem fechamento 
                else if (nomeToken.equals("CADEIA_NAO_FECHADA")) {
                    pw.println("Linha " + linha + ": cadeia literal nao fechada");
                    break;
                } 
                
                //Erro de símbolo inválido
                else if (nomeToken.equals("ERRO")) {
                    pw.println("Linha " + linha + ": " + texto + " - simbolo nao identificado");
                    break; 
                }

                if (nomeToken.equals("IDENT") || nomeToken.equals("CADEIA") || 
                    nomeToken.equals("NUM_INT") || nomeToken.equals("NUM_REAL")) {
                    pw.println("<'" + texto + "'," + nomeToken + ">");
                } 
                else {
                    pw.println("<'" + texto + "','" + texto + "'>");
                }
            }
            
        } catch (IOException ex) {
            System.err.println("Erro na manipulação do arquivo: " + ex.getMessage());
        }
    }
}