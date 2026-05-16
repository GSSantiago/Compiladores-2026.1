package br.ufscar.dc.compiladores.linguagem.la;

import static br.ufscar.dc.compiladores.t4.LASemantico.dadosFuncaoProcedimento;
import static br.ufscar.dc.compiladores.t4.LASemantico.escoposAninhados;
import br.ufscar.dc.compiladores.linguagem.la.TabelaSimbolos.TipoLA;
import java.util.ArrayList;
import java.util.HashMap;
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

        for (LAParser.ParcelaContext parcela : ctx.parcela()) {
            tipoRetorno = verificarTipo(tabela, parcela);

           //Se for registro, reduz o nome e verifica de novo
            if (tipoRetorno == TipoLA.REGISTRO) {
                String nome = parcela.getText();
                nome = reduzNome(nome, "(");
                tipoRetorno = verificarTipo(tabela, nome);
            }
        }
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
        TipoLA tipoRetorno = null;
        String nome;
        
        if (ctx.identificador() != null) {            
            // Lida com vetores
            if (!ctx.identificador().dimensao().exp_aritmetica().isEmpty())
                nome = ctx.identificador().IDENT().get(0).getText();
            else
                nome = ctx.identificador().getText();
            
            if (tabela.existe(nome)) {
                tipoRetorno = tabela.verificar(nome);
            }
            else {
                TabelaSimbolos tabelaAux = escoposAninhados.obterEscopoAtual();
                
                if (!tabelaAux.existe(nome)) {
                    adicionaErroSemantico(ctx.identificador().getStart(), "identificador " + ctx.identificador().getText() + " nao declarado");
                    tipoRetorno = TipoLA.INVALIDO;
                } else {
                    tipoRetorno = tabelaAux.verificar(nome);
                }
            }
        // Validação de chamadas de Função e Procedimento
        } else if (ctx.IDENT() != null) {
            if (dadosFuncaoProcedimento.containsKey(ctx.IDENT().getText())) {
                List<TipoLA> aux = dadosFuncaoProcedimento.get(ctx.IDENT().getText());

                // Verifica se a quantidade de parâmetros bate
                if (aux.size() == ctx.expressao().size()) {
                    for (int i = 0; i < ctx.expressao().size(); i++) {
                        if (aux.get(i) != verificarTipo(tabela, ctx.expressao().get(i))) {
                            adicionaErroSemantico(ctx.expressao().get(i).getStart(), "incompatibilidade de parametros na chamada de " + ctx.IDENT().getText());
                        }
                    }
                    tipoRetorno = aux.get(aux.size() - 1);
                } else {
                    adicionaErroSemantico(ctx.IDENT().getSymbol(), "incompatibilidade de parametros na chamada de " + ctx.IDENT().getText());
                }
            } else {
                tipoRetorno = TipoLA.INVALIDO;
            }
        } else if (ctx.NUM_INT() != null) {
            tipoRetorno = TipoLA.INTEIRO;
        } else if (ctx.NUM_REAL() != null) {
            tipoRetorno = TipoLA.REAL;
        } else {
            tipoRetorno = verificarTipo(tabela, ctx.expressao().get(0));
        }

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

    // Retorna o TipoLA correto, removendo o símbolo de ponteiro (^) se existir
    public static TipoLA confereTipo(HashMap<String, ArrayList<String>> tabela, String tipoRetorno) {
        TipoLA tipoAux;
        // Remove ponteiro
        if (tipoRetorno.charAt(0) == '^') {
            tipoRetorno = tipoRetorno.substring(1);
        }

        // Verifica primeiro se é um registro (struct) salvo na tabela
        if (tabela.containsKey(tipoRetorno)) {
            tipoAux = TipoLA.REGISTRO;
        } else {
            // Caso não seja registro, valida qual o retorno
            switch (tipoRetorno) {
                case "real":
                    tipoAux = TipoLA.REAL;
                    break;
                case "inteiro":
                    tipoAux = TipoLA.INTEIRO;
                    break;
                case "logico":
                    tipoAux = TipoLA.LOGICO;
                    break;
                case "literal":
                    tipoAux = TipoLA.LITERAL;
                    break;
                default:
                    tipoAux = TipoLA.INVALIDO;
                    break;
            }
        }

        return tipoAux;
    }

    // Verificação padrão de tipos de variáveis a partir da tabela.
    public static TipoLA verificarTipo(TabelaSimbolos tabela, String nomeVar) {
        return tabela.verificar(nomeVar);
    }

    // Extrai direto o texto do IDENT base
    public static TipoLA verificarTipo(TabelaSimbolos tabela, LAParser.IdentificadorContext ctx) {
        String nomeVar = ctx.IDENT().get(0).getText();
        return tabela.verificar(nomeVar);
    }

    // Limpa o nome do identificador
    public static String reduzNome(String nome, String simbolo) {

        if (nome.contains(simbolo)) {
            boolean continua = true;
            String nomeAux;

            int cont = 0;
            while (continua) {
                nomeAux = nome.substring(cont);
                if (nomeAux.startsWith(simbolo)) continua = false;
                else cont++;
            }
            nome = nome.substring(0, cont);
        }
        return nome;
    }
}