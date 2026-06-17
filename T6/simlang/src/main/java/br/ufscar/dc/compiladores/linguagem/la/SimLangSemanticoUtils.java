package br.ufscar.dc.compiladores.linguagem.la;

import org.antlr.v4.runtime.Token;
import java.util.ArrayList;
import java.util.List;

public class SimLangSemanticoUtils {
    
    // Lista estática que vai acumular todos os erros encontrados durante a compilação
    public static List<String> errosSemanticos = new ArrayList<>();

    // Método utilitário para formatar a mensagem de erro com a linha correta
    public static void adicionarErro(Token token, String mensagem) {
        int linha = token.getLine();
        errosSemanticos.add("Linha " + linha + ": " + mensagem);
    }
}