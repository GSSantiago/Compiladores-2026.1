package main.java.br.ufscar.dc.compiladores.linguagem.la;

import static br.ufscar.dc.compiladores.t3.LASemanticoUtils.verificarTipo;
import static br.ufscar.dc.compiladores.t3.LASemanticoUtils.adicionaErroSemantico;
import static br.ufscar.dc.compiladores.t3.LASemanticoUtils.verificaCompatibilidade;
import br.ufscar.dc.compiladores.t3.TabelaDeSimbolos.TipoT3;
import org.antlr.v4.runtime.Token;

public class LASemantico extends LABaseVisitor<Void> {

    // Renomeado de 'tabela' para 'tabelaAtual' para ser mais descritivo
    TabelaDeSimbolos tabelaAtual;

    // Mantido como estático para que os Utils consigam acessar
    static Escopos escoposAninhados = new Escopos();

    // Método para registrar variáveis com nomes de parâmetros alterados
    public void registrarVariavel(String nomeVar, String strTipo, Token tNome, Token tTipo) {
        TabelaDeSimbolos escopoDestino = escoposAninhados.obterEscopoAtual();
        TipoT3 tipoEnum;

        // Uso de switch expression (se estiver usando Java 12+) ou apenas simplificação
        switch (strTipo) {
            case "literal":  tipoEnum = TipoT3.LITERAL; break;
            case "inteiro":  tipoEnum = TipoT3.INTEIRO; break;
            case "real":     tipoEnum = TipoT3.REAL;    break;
            case "logico":   tipoEnum = TipoT3.LOGICO;  break;
            default:         tipoEnum = TipoT3.INVALIDO; break;
        }

        if (tipoEnum == TipoT3.INVALIDO) {
            adicionaErroSemantico(tTipo, "tipo " + strTipo + " nao declarado");
        }

        if (!escopoDestino.existe(nomeVar)) {
            escopoDestino.adicionar(nomeVar, tipoEnum);
        } else {
            adicionaErroSemantico(tNome, "identificador " + nomeVar + " ja declarado anteriormente");
        }
    }

    @Override
    public Void visitPrograma(LAParser.ProgramaContext ctx) {
        tabelaAtual = new TabelaDeSimbolos();
        return super.visitPrograma(ctx);
    }

    @Override
    public Void visitDeclaracoes(LAParser.DeclaracoesContext ctx) {
        // Atualiza a referência da tabela local antes de iterar
        tabelaAtual = escoposAninhados.obterEscopoAtual();
        return super.visitDeclaracoes(ctx);
    }

    @Override
    public Void visitDeclaracao_local(LAParser.Declaracao_localContext ctx) {
        tabelaAtual = escoposAninhados.obterEscopoAtual();

        if (ctx.getText().contains("declare")) {
            String tipoDesejado = ctx.variavel().tipo().getText();
            Token tokenTipo = ctx.variavel().tipo().getStart();

            for (LAParser.IdentificadorContext idCtx : ctx.variavel().identificador()) {
                registrarVariavel(idCtx.getText(), tipoDesejado, idCtx.getStart(), tokenTipo);
            }
        }
        return super.visitDeclaracao_local(ctx);
    }

    @Override
    public Void visitCmdLeia(LAParser.CmdLeiaContext ctx) {
        tabelaAtual = escoposAninhados.obterEscopoAtual();
        for (LAParser.IdentificadorContext id : ctx.identificador()) {
            if (!tabelaAtual.existe(id.getText())) {
                adicionaErroSemantico(id.getStart(), "identificador " + id.getText() + " nao declarado");
            }
        }
        return super.visitCmdLeia(ctx);
    }

    @Override
    public Void visitCmdAtribuicao(LAParser.CmdAtribuicaoContext ctx) {
        tabelaAtual = escoposAninhados.obterEscopoAtual();
        
        TipoT3 tipoExpressao = verificarTipo(tabelaAtual, ctx.expressao());
        String nomeVar = ctx.identificador().getText();
        Token tokenVar = ctx.identificador().getStart();

        if (tipoExpressao != TipoT3.INVALIDO) {
            if (!tabelaAtual.existe(nomeVar)) {
                adicionaErroSemantico(tokenVar, "identificador " + nomeVar + " nao declarado");
            } else {
                TipoT3 tipoVariavel = verificarTipo(tabelaAtual, nomeVar);
                
                // Lógica de compatibilidade simplificada
                boolean compativel = (tipoVariavel == tipoExpressao) || 
                                     verificaCompatibilidade(tipoVariavel, tipoExpressao);

                if (!compativel) {
                    adicionaErroSemantico(tokenVar, "atribuicao nao compativel para " + nomeVar);
                }
            }
        }
        return super.visitCmdAtribuicao(ctx);
    }
}