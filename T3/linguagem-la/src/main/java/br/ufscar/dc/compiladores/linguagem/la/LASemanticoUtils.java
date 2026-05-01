package br.ufscar.dc.compiladores.linguagem.la;

// Importações básicas para o funcionamento do programa.
import br.ufscar.dc.compiladores.linguagem.la.TabelaSimbolos.TipoLA;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.Token;

public class LASemanticoUtils {

    // Lista de erros semânticos encontrados
    public static List<String> errosSemanticos = new ArrayList<>();

    // Adiciona erro na lista se ele ainda não existir
    public static void adicionaErroSemantico(Token tok, String mensagem) {
        int linha = tok.getLine();
        if (!errosSemanticos.contains("Linha " + linha + ": " + mensagem)) 
            errosSemanticos.add(String.format("Linha %d: %s", linha, mensagem));
    }
    
    // Verifica compatibilidade entre tipos numéricos (podendo passar inteiro para real se pelo menos um for real)
    public static boolean verificaCompatibilidade(TipoLA T1, TipoLA T2) {
        boolean flag = false;
        
        if (T1 == TipoLA.INTEIRO && T2 == TipoLA.REAL)
            flag = true;
        else if (T1 == TipoLA.REAL && T2 == TipoLA.INTEIRO)
            flag = true;
        else if (T1 == TipoLA.REAL && T2 == TipoLA.REAL)
            flag = true;
        
        return flag;
    }
    
    // Verifica se os tipos podem ser usados em operações lógicas
    public static boolean verificaCompatibilidadeLogica(TipoLA T1, TipoLA T2) {
        boolean flag = false;
        
        if (T1 == TipoLA.INTEIRO && T2 == TipoLA.REAL)
            flag = true;
        else if (T1 == TipoLA.REAL && T2 == TipoLA.INTEIRO)
            flag = true;

        return flag;
    }
                    
    public static TipoLA verificarTipo(TabelaSimbolos tabela, LAParser.Exp_aritmeticaContext ctx) {
        // Inicializa com o tipo do primeiro termo
        TipoLA tipoRetorno = verificarTipo(tabela, ctx.termo().get(0));
                
        for (var termoArit : ctx.termo()) {
            TipoLA tipoAtual = verificarTipo(tabela, termoArit);
            
            // Define como REAL se houver compatibilidade entre tipos numéricos
            if ((verificaCompatibilidade(tipoAtual, tipoRetorno)) && (tipoAtual != TipoLA.INVALIDO))
                tipoRetorno = TipoLA.REAL;
            else
                tipoRetorno = tipoAtual;
        }

        return tipoRetorno;
    }

    public static TipoLA verificarTipo(TabelaSimbolos tabela, LAParser.TermoContext ctx) {
        TipoLA tipoRetorno = verificarTipo(tabela, ctx.fator().get(0));
                
        for (LAParser.FatorContext fatorArit : ctx.fator()) {
            TipoLA tipoAtual = verificarTipo(tabela, fatorArit);
            
            if ((verificaCompatibilidade(tipoAtual, tipoRetorno)) && (tipoAtual != TipoLA.INVALIDO))
                tipoRetorno = TipoLA.REAL;
            else
                tipoRetorno = tipoAtual;
        }
        
        return tipoRetorno;
    }

    public static TipoLA verificarTipo(TabelaSimbolos tabela, LAParser.FatorContext ctx) {
        TipoLA tipoRetorno = null;
        
        for (LAParser.ParcelaContext parcela : ctx.parcela())
            tipoRetorno = verificarTipo(tabela, parcela);

        return tipoRetorno;
    }

    public static TipoLA verificarTipo(TabelaSimbolos tabela, LAParser.ParcelaContext ctx) {
        // Direciona para parcela unária ou não unária
        if (ctx.parcela_unario() != null)
            return verificarTipo(tabela, ctx.parcela_unario());
        else
            return verificarTipo(tabela, ctx.parcela_nao_unario());
    }

    public static TipoLA verificarTipo(TabelaSimbolos tabela, LAParser.Parcela_unarioContext ctx) {
        TipoLA tipoRetorno;
        String nome;
        
        if (ctx.identificador() != null) {
            nome = ctx.identificador().getText();
            
            // Direciona para parcela unária ou não unária
            if (tabela.existe(nome))
                tipoRetorno = tabela.verificar(nome);
            else {
                TabelaSimbolos tabelaAux = LASemantico.escoposAninhados.percorrerEscoposAninhados().get(LASemantico.escoposAninhados.percorrerEscoposAninhados().size() - 1);
                if (!tabelaAux.existe(nome)) {
                    adicionaErroSemantico(ctx.identificador().getStart(), "identificador " + ctx.identificador().getText() + " nao declarado");
                    tipoRetorno = TipoLA.INVALIDO;
                } else 
                    tipoRetorno = tabelaAux.verificar(nome);
            }
        } else if (ctx.NUM_INT() != null)
            tipoRetorno = TipoLA.INTEIRO;
        else if (ctx.NUM_REAL() != null)
            tipoRetorno = TipoLA.REAL;
        else
            tipoRetorno = verificarTipo(tabela, ctx.expressao().get(0));

        return tipoRetorno;
    }

    public static TipoLA verificarTipo(TabelaSimbolos tabela, LAParser.Parcela_nao_unarioContext ctx) {
        TipoLA tipoRetorno;
        String nome;

        if (ctx.identificador() != null) {
            nome = ctx.identificador().getText();
        
            if (!tabela.existe(nome)) {
                adicionaErroSemantico(ctx.identificador().getStart(), "identificador " + ctx.identificador().getText() + " nao declarado");
                tipoRetorno = TipoLA.INVALIDO;
            } else 
                tipoRetorno = tabela.verificar(ctx.identificador().getText());
        } else
            tipoRetorno = TipoLA.LITERAL;

        return tipoRetorno;
    }

    public static TipoLA verificarTipo(TabelaSimbolos tabela, LAParser.ExpressaoContext ctx) {
        TipoLA tipoRetorno = verificarTipo(tabela, ctx.termo_logico(0));

        // Verifica se os tipos da expressão lógica são consistentes
        for (LAParser.Termo_logicoContext termoLogico : ctx.termo_logico()) {
            TipoLA tipoAtual = verificarTipo(tabela, termoLogico);
            if (tipoRetorno != tipoAtual && tipoAtual != TipoLA.INVALIDO)
                tipoRetorno = TipoLA.INVALIDO;
        }

        return tipoRetorno;
    }

    public static TipoLA verificarTipo(TabelaSimbolos tabela, LAParser.Termo_logicoContext ctx) {
        TipoLA tipoRetorno = verificarTipo(tabela, ctx.fator_logico(0));

        for (LAParser.Fator_logicoContext fatorLogico : ctx.fator_logico()) {
            TipoLA tipoAtual = verificarTipo(tabela, fatorLogico);
            if (tipoRetorno != tipoAtual && tipoAtual != TipoLA.INVALIDO)
                tipoRetorno = TipoLA.INVALIDO;
        }
        return tipoRetorno;
    }

    public static TipoLA verificarTipo(TabelaSimbolos tabela, LAParser.Fator_logicoContext ctx) {
        TipoLA tipoRetorno = verificarTipo(tabela, ctx.parcela_logica());
        return tipoRetorno;

    }

    public static TipoLA verificarTipo(TabelaSimbolos tabela, LAParser.Parcela_logicaContext ctx) {
        TipoLA tipoRetorno;

        if (ctx.exp_relacional() != null)
            tipoRetorno = verificarTipo(tabela, ctx.exp_relacional());
         else
            tipoRetorno = TipoLA.LOGICO;

        return tipoRetorno;

    }

    public static TipoLA verificarTipo(TabelaSimbolos tabela, LAParser.Exp_relacionalContext ctx) {
        TipoLA tipoRetorno = verificarTipo(tabela, ctx.exp_aritmetica().get(0));

        if (ctx.exp_aritmetica().size() > 1) {
            TipoLA tipoAtual = verificarTipo(tabela, ctx.exp_aritmetica().get(1));

            // Define como LOGICO se os tipos comparados forem compatíveis
            if (tipoRetorno == tipoAtual || verificaCompatibilidadeLogica(tipoRetorno, tipoAtual))
                tipoRetorno = TipoLA.LOGICO;
            else
                tipoRetorno = TipoLA.INVALIDO;
        }

        return tipoRetorno;

    }

    // Busca tipo de uma variável pelo nome na tabela
    public static TipoLA verificarTipo(TabelaSimbolos tabela, String nomeVar) {
        return tabela.verificar(nomeVar);
    }
}