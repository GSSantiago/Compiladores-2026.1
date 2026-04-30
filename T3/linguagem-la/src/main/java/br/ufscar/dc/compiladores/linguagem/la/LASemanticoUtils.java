package br.ufscar.dc.compiladores.linguagem.la;

// Importações básicas para o funcionamento do programa.
import br.ufscar.dc.compiladores.linguagem.la.TabelaSimbolos.TipoLA;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.Token;

public class LASemanticoUtils {

    // Criação da lista que armazenará os erros identificados pelo analisador.
    public static List<String> errosSemanticos = new ArrayList<>();

    // Método auxiliar utilizado para adicionar um novo erro identificado na lista.
    public static void adicionaErroSemantico(Token tok, String mensagem) {
        int linha = tok.getLine();
        
        // Verifica se o erro já foi identificado para poder adicioná-lo à lista.
        if (!errosSemanticos.contains("Linha " + linha + ": " + mensagem)) 
            errosSemanticos.add(String.format("Linha %d: %s", linha, mensagem));
    }
    
    // Método auxiliar que verifica a compatibilidade entre operadores aritméticos.
    // Caso a operação envolva pelo menos um valor real, a operação deve ser tratada
    // como uma operação entre números reais, mesmo que um deles seja um inteiro.
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
    
    // Método auxiliar que verifica a compatibilidade entre operadores para tratá-los
    // como uma operação lógica.
    public static boolean verificaCompatibilidadeLogica(TipoLA T1, TipoLA T2) {
        boolean flag = false;
        
        if (T1 == TipoLA.INTEIRO && T2 == TipoLA.REAL)
            flag = true;
        else if (T1 == TipoLA.REAL && T2 == TipoLA.INTEIRO)
            flag = true;

        return flag;
    }
                    
    public static TipoLA verificarTipo(TabelaSimbolos tabela, LAParser.Exp_aritmeticaContext ctx) {
        // A variável que será retornada ao fim da execução é inicializada com o tipo
        // do primeiro elemento que será verificado, para fins de comparação.
        TipoLA tipoRetorno = verificarTipo(tabela, ctx.termo().get(0));
                
        for (var termoArit : ctx.termo()) {
            // Esta outra variável recebe os tipos dos outros termos da expressão.
            TipoLA tipoAtual = verificarTipo(tabela, termoArit);
            
            // Com o auxílio do método declarado anteriormente, o programa verifica se deve tratar a
            // verificação atual como uma operação entre números reais.
            if ((verificaCompatibilidade(tipoAtual, tipoRetorno)) && (tipoAtual != TipoLA.INVALIDO))
                tipoRetorno = TipoLA.REAL;
            else
                tipoRetorno = tipoAtual;
        }

        return tipoRetorno;
    }

    public static TipoLA verificarTipo(TabelaSimbolos tabela, LAParser.TermoContext ctx) {
        // A variável que será retornada ao fim da execução é inicializada com o tipo
        // do primeiro elemento que será verificado, para fins de comparação.
        TipoLA tipoRetorno = verificarTipo(tabela, ctx.fator().get(0));
                
        for (LAParser.FatorContext fatorArit : ctx.fator()) {
            // Esta outra variável recebe os tipos dos outros termos da expressão.
            TipoLA tipoAtual = verificarTipo(tabela, fatorArit);
            
            // Com o auxílio do método declarado anteriormente, o programa verifica se deve tratar a
            // verificação atual como uma operação entre números reais.
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
        // Identifica se é uma parcela unária ou não unária.
        if (ctx.parcela_unario() != null)
            return verificarTipo(tabela, ctx.parcela_unario());
        else
            return verificarTipo(tabela, ctx.parcela_nao_unario());
    }

    public static TipoLA verificarTipo(TabelaSimbolos tabela, LAParser.Parcela_unarioContext ctx) {
        TipoLA tipoRetorno;
        String nome;
        
        if (ctx.identificador() != null) {
            // Obtém o nome da variável atual.
            nome = ctx.identificador().getText();
            
            // Caso a variável já tenha sido declarada, apenas retorna o tipo associado a ela.
            if (tabela.existe(nome))
                tipoRetorno = tabela.verificar(nome);
            // Caso contrário, utiliza uma tabela auxiliar para prosseguir com a verificação. Se a variável não
            // tiver sido declarada, utiliza o método adicionaErroSemantico para verificar se o erro já foi
            // exibido e, caso ainda não tenha sido, o adiciona à lista.
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

        // Utiliza uma lógica semelhante à verificação de tipo anterior, verificando a existência da variável
        // e tentando adicioná-la à lista de erros.
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

        // Para expressões lógicas, a ideia resume-se apenas em verificar se os tipos analisados
        // são diferentes.
        for (LAParser.Termo_logicoContext termoLogico : ctx.termo_logico()) {
            TipoLA tipoAtual = verificarTipo(tabela, termoLogico);
            if (tipoRetorno != tipoAtual && tipoAtual != TipoLA.INVALIDO)
                tipoRetorno = TipoLA.INVALIDO;
        }

        return tipoRetorno;
    }

    public static TipoLA verificarTipo(TabelaSimbolos tabela, LAParser.Termo_logicoContext ctx) {
        TipoLA tipoRetorno = verificarTipo(tabela, ctx.fator_logico(0));

        // Para expressões lógicas, a ideia resume-se apenas em verificar se os tipos analisados
        // são diferentes.
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

            // Semelhante ao que foi feito com as expressões aritméticas, ocorre uma verificação
            // para saber se a expressão atual pode ser tratada como uma operação lógica.
            if (tipoRetorno == tipoAtual || verificaCompatibilidadeLogica(tipoRetorno, tipoAtual))
                tipoRetorno = TipoLA.LOGICO;
            else
                tipoRetorno = TipoLA.INVALIDO;
        }

        return tipoRetorno;

    }

    // Verificação padrão de tipos de variáveis a partir da tabela.
    public static TipoLA verificarTipo(TabelaSimbolos tabela, String nomeVar) {
        return tabela.verificar(nomeVar);
    }
}