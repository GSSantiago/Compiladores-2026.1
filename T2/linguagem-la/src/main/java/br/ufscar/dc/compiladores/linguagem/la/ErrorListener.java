package br.ufscar.dc.compiladores.linguagem.la;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.io.PrintWriter;
import java.util.BitSet;

public class ErrorListener implements ANTLRErrorListener {
    PrintWriter pw;

    // Construtor que recebe o arquivo de saída
    public ErrorListener(PrintWriter pw) {
        this.pw = pw;
    }

    @Override
    public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) { }

    @Override
    public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) { }

    @Override
    public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) { }

    @Override
    public void syntaxError(Recognizer<?,?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        // Obtém o token exato onde o erro ocorreu
        Token t = (Token) offendingSymbol;
        String texto = t.getText();

        // Tratamento específico para o fim inesperado do arquivo (EOF)
        if (texto.equals("<EOF>")) {
            texto = "EOF";
            pw.println("Linha "+ line +": erro sintatico proximo a EOF\nFim da compilacao");
            pw.close(); // Fecha o arquivo pois já imprimiu o fim da compilação
            return;
        }

        // Descobrir o nome do token na gramática
        String nomeToken = LALexer.VOCABULARY.getDisplayName(t.getType());
        int linha = t.getLine();

        // Captura erro léxico de comentário não fechado
        if (nomeToken.equals("COMENTARIO_NAO_FECHADO")) {
            pw.println("Linha " + linha + ": comentario nao fechado");
        }
        // Captura erro léxico de aspas não fechadas
        else if (nomeToken.equals("CADEIA_NAO_FECHADA")) {
            pw.println("Linha " + linha + ": cadeia literal nao fechada");
        }
        // Captura erro léxico de símbolo inválido/desconhecido
        else if (nomeToken.equals("ERRO")) {
            pw.println("Linha " + linha + ": " + texto + " - simbolo nao identificado");
        }
        // Captura erros sintáticos gerais
        else {
            pw.println("Linha " + line + ": erro sintatico proximo a " + texto);
        }

        // Imprime a mensagem de encerramento padrão do corretor
        pw.println("Fim da compilacao");

        // Lança exceção para parar o parser imediatamente no primeiro erro
        throw new ParseCancellationException("Erro Sintático Encontrado");
    }
}