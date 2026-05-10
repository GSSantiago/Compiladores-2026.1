package br.ufscar.dc.compiladores.linguagem.la;

import static br.ufscar.dc.compiladores.linguagem.la.LASemanticoUtils.verificarTipo;
import static br.ufscar.dc.compiladores.linguagem.la.LASemanticoUtils.adicionaErroSemantico;
import static br.ufscar.dc.compiladores.linguagem.la.LASemanticoUtils.verificaCompatibilidade;
import br.ufscar.dc.compiladores.linguagem.la.TabelaSimbolos.TipoLA;
import org.antlr.v4.runtime.Token;

public class LASemantico extends LABaseVisitor<Void> {

    // Renomeado de 'tabela' para 'tabelaAtual' para ser mais descritivo
    TabelaSimbolos tabelaAtual;

    // Mantido como estático para que os Utils consigam acessar
    static Escopos escoposAninhados = new Escopos();

    // Método para registrar variáveis com nomes de parâmetros alterados
    public void registrarVariavel(String nomeVar, String strTipo, Token tNome, Token tTipo) {
        TabelaSimbolos escopoDestino = escoposAninhados.obterEscopoAtual();
        TipoLA tipoEnum;

        // Uso de switch expression
        switch (strTipo) {
            case "literal":  tipoEnum = TipoLA.LITERAL; break;
            case "inteiro":  tipoEnum = TipoLA.INTEIRO; break;
            case "real":     tipoEnum = TipoLA.REAL;    break;
            case "logico":   tipoEnum = TipoLA.LOGICO;  break;
            default:         tipoEnum = TipoLA.INVALIDO; break;
        }

        if (tipoEnum == TipoLA.INVALIDO) {
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
        tabelaAtual = new TabelaSimbolos();
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
    public Void visitCmdEscreva(LAParser.CmdEscrevaContext ctx) {
        tabelaAtual = escoposAninhados.obterEscopoAtual();

        for (LAParser.ExpressaoContext expressao : ctx.expressao())
            verificarTipo(tabelaAtual, expressao);

        return super.visitCmdEscreva(ctx);
    }

    @Override
    public Void visitCmdAtribuicao(LAParser.CmdAtribuicaoContext ctx) {
        tabelaAtual = escoposAninhados.obterEscopoAtual();
        
        TipoLA tipoExpressao = verificarTipo(tabelaAtual, ctx.expressao());
        String nomeVar = ctx.identificador().getText();
        Token tokenVar = ctx.identificador().getStart();

        if (tipoExpressao != TipoLA.INVALIDO) {
            if (!tabelaAtual.existe(nomeVar)) {
                adicionaErroSemantico(tokenVar, "identificador " + nomeVar + " nao declarado");
            } else {
                TipoLA tipoVariavel = verificarTipo(tabelaAtual, nomeVar);
                
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