package br.ufscar.dc.compiladores.linguagem.la;

import br.ufscar.dc.compiladores.linguagem.la.TabelaSimbolos.TipoEntrada;
import br.ufscar.dc.compiladores.linguagem.la.TabelaSimbolos.TipoLA;

// A classe agora estende o LABaseVisitor que foi gerado pela SUA gramática LA.g4
public class Gerador extends LABaseVisitor<Void> {

    // StringBuilder é excelente para montar o código final
    public StringBuilder saida = new StringBuilder();
    
    // Gerenciamento de escopos e tabela de símbolos
    private TabelaSimbolos tabelaAtual = new TabelaSimbolos();
    private Escopos escopos = new Escopos();

    private String mapearTipoParaC(TipoLA tipoLA) {
        if (tipoLA == null) return null;
        switch (tipoLA) {
            case INTEIRO: return "int";
            case REAL: return "float";
            case LITERAL: return "char";
            default: return null;
        }
    }

    private TipoLA mapearStringParaTipoLA(String tipo) {
        switch (tipo) {
            case "literal": return TipoLA.LITERAL;
            case "inteiro": return TipoLA.INTEIRO;
            case "real": return TipoLA.REAL;
            case "logico": return TipoLA.LOGICO;
            default: return TipoLA.INVALIDO;
        }
    }

    private String obterMascaraIOParaC(TipoLA tipoLA) {
        if (tipoLA == null) return null;
        switch (tipoLA) {
            case INTEIRO: return "d";
            case REAL: return "f";
            case LITERAL: return "s";
            default: return null;
        }
    }
    
    @Override
    public Void visitPrograma(LAParser.ProgramaContext ctx) {
        saida.append("#include <stdio.h>\n");
        saida.append("#include <stdlib.h>\n\n");
        
        visitDeclaracoes(ctx.declaracoes());

        saida.append("\nint main() {\n");
        visitCorpo(ctx.corpo());
        saida.append("\nreturn 0;\n");
        saida.append("}\n");

        return null;
    }

    @Override
    public Void visitDeclaracao_local(LAParser.Declaracao_localContext ctx) {
        // Constantes
        if (ctx.valor_constante() != null) {
            saida.append(String.format("#define %s %s\n", ctx.IDENT().getText(), ctx.valor_constante().getText()));
        } 
        // Tipo / Registro
        else if (ctx.tipo() != null) {
            TabelaSimbolos escopoAtual = escopos.obterEscopoAtual();
            escopos.criarNovoEscopo();

            saida.append("typedef struct {\n");
            super.visitRegistro(ctx.tipo().registro());
            escopos.abandonarEscopo();
            
            escopoAtual.adicionar(ctx.IDENT().getText(), TipoLA.REGISTRO, TipoEntrada.VARIAVEL);
            saida.append(String.format("} %s;\n", ctx.IDENT().getText()));
        } 
        // Variáveis Padrão
        else if (ctx.variavel() != null) {
            visitVariavel(ctx.variavel());
        }
        
        return null;
    }

    @Override
    public Void visitVariavel(LAParser.VariavelContext ctx) {
        TabelaSimbolos escopoAtual = escopos.obterEscopoAtual();
        
        if (ctx.tipo().tipo_estendido() != null) {
            String tipoTexto = ctx.tipo().getText();
            boolean ehPonteiro = tipoTexto.startsWith("^");
            if (ehPonteiro) tipoTexto = tipoTexto.substring(1);

            boolean isTipoEstendido = escopoAtual.existe(tipoTexto);
            TipoLA tipoLA = isTipoEstendido ? TipoLA.TIPOESTENDIDO : mapearStringParaTipoLA(tipoTexto);
            String tipoC = isTipoEstendido ? tipoTexto : mapearTipoParaC(tipoLA);
            
            if (ehPonteiro) tipoC += "*";

            for (LAParser.IdentificadorContext idCtx : ctx.identificador()) {
                String nomeVariavel = idCtx.getText();
                escopoAtual.adicionar(nomeVariavel, isTipoEstendido ? TipoLA.REGISTRO : tipoLA, TipoEntrada.VARIAVEL);

                if (tipoLA == TipoLA.LITERAL) {
                    saida.append(String.format("%s %s[80];\n", tipoC, nomeVariavel));
                } else {
                    saida.append(String.format("%s %s;\n", tipoC, nomeVariavel));
                }
            }
        } else {
            escopos.criarNovoEscopo();
            saida.append("struct {\n");
            for (LAParser.VariavelContext varCtx : ctx.tipo().registro().variavel()) {
                visitVariavel(varCtx);
            }
            saida.append("} ").append(ctx.identificador(0).getText()).append(";\n");
            escopos.abandonarEscopo();
            escopoAtual.adicionar(ctx.identificador(0).getText(), TipoLA.REGISTRO, TipoEntrada.VARIAVEL);
        }

        return null;
    }

    @Override
    public Void visitCmdSe(LAParser.CmdSeContext ctx) {
        // Substitui os operadores visuais da linguagem LA pelos do C
        String condicao = ctx.expressao().getText()
                .replace("e", "&&")
                .replace("ou", "||")
                .replace("=", "==")
                .replace("<>>", "!="); // Evita bugs caso o '<>' vire '<>='

        saida.append(String.format("if (%s) {\n", condicao));

        for (LAParser.CmdContext cmd : ctx.cmdEntao) {
            visitCmd(cmd);
        }
        saida.append("}\n");

        if (ctx.cmdSenao != null && !ctx.cmdSenao.isEmpty()) {
            saida.append("else {\n");
            for (LAParser.CmdContext cmd : ctx.cmdSenao) {
                visitCmd(cmd);
            }
            saida.append("}\n");
        }

        return null;
    }

    @Override
    public Void visitCmdLeia(LAParser.CmdLeiaContext ctx) {
        TabelaSimbolos escopo = escopos.obterEscopoAtual();

        for (LAParser.IdentificadorContext idCtx : ctx.identificador()) {
            String nomeVariavel = idCtx.getText();
            // Assumimos que o LASemanticoUtils tenha a lógica ou que você use o escopo para verificar
            TipoLA tipo = escopo.verificar(nomeVariavel); 
            
            if (tipo == TipoLA.LITERAL) {
                saida.append(String.format("gets(%s);\n", nomeVariavel));
            } else {
                String mascara = obterMascaraIOParaC(tipo);
                saida.append(String.format("scanf(\"%%%s\", &%s);\n", mascara, nomeVariavel));
            }
        }
        return null;
    }

    @Override
    public Void visitCmdEscreva(LAParser.CmdEscrevaContext ctx) {
        TabelaSimbolos escopoAtual = escopos.obterEscopoAtual();

        for (LAParser.ExpressaoContext expCtx : ctx.expressao()) {
            String textoExpressao = expCtx.getText();

            if (textoExpressao.startsWith("\"") && textoExpressao.endsWith("\"")) {
                // Impressão direta de String literal
                saida.append(String.format("printf(%s);\n", textoExpressao));
            } else {
                // Necessário verificar o tipo para montar a máscara
                // Obs: Aqui você deve usar o método do seu LASemanticoUtils que verifica o tipo da expressão
                TipoLA tipoDaExpressao = LASemanticoUtils.verificarTipo(escopoAtual, expCtx); 
                
                String mascara = obterMascaraIOParaC(tipoDaExpressao);
                saida.append(String.format("printf(\"%%%s\", %s);\n", mascara, textoExpressao));
            }
        }
        return null;
    }

    @Override
    public Void visitCmdPara(LAParser.CmdParaContext ctx) {
        String variavelIterador = ctx.IDENT().getText();
        String limiteInicial = ctx.exp_aritmetica(0).getText();
        String limiteFinal = ctx.exp_aritmetica(1).getText();

        saida.append(String.format("for(%s = %s; %s <= %s; %s++) {\n", 
                variavelIterador, limiteInicial, variavelIterador, limiteFinal, variavelIterador));

        for (LAParser.CmdContext cmdCtx : ctx.cmd()) {
            visitCmd(cmdCtx);
        }
        saida.append("}\n");

        return null;
    }

    @Override
    public Void visitCmdEnquanto(LAParser.CmdEnquantoContext ctx) {
        String condicao = ctx.expressao().getText()
                .replace("=", "==")
                .replace("<>", "!=");
                
        saida.append(String.format("while(%s) {\n", condicao));

        for (LAParser.CmdContext cmdCtx : ctx.cmd()) {
            visitCmd(cmdCtx);
        }
        saida.append("}\n");
        return null;
    }

    @Override
    public Void visitCmdFaca(LAParser.CmdFacaContext ctx) {
        saida.append("do {\n");
        for (LAParser.CmdContext cmdCtx : ctx.cmd()) {
            visitCmd(cmdCtx);
        }
        
        String condicao = ctx.expressao().getText()
                .replace("=", "==")
                .replace("<>", "!=");
                
        saida.append(String.format("} while(%s);\n", condicao));
        return null;
    }

    @Override
    public Void visitCmdAtribuicao(LAParser.CmdAtribuicaoContext ctx) {
        String ponteiro = ctx.getText().startsWith("^") ? "*" : "";
        String nomeVar = ctx.identificador().getText();
        String expressao = ctx.expressao().getText();

        if (nomeVar.contains(".") && expressao.startsWith("\"")) {
            saida.append(String.format("strcpy(%s, %s);\n", nomeVar, expressao));
        } else {
            saida.append(String.format("%s%s = %s;\n", ponteiro, nomeVar, expressao));
        }
        return null;
    }

    @Override
    public Void visitCmdCaso(LAParser.CmdCasoContext ctx) {
        saida.append(String.format("switch (%s) {\n", ctx.exp_aritmetica().getText()));

        for (LAParser.Item_selecaoContext selecaoCtx : ctx.selecao().item_selecao()) {
            String intervaloTexto = selecaoCtx.constantes().numero_intervalo(0).getText();
            
            // O uso de .split() limpa muito o código comparado ao original
            if (intervaloTexto.contains("..")) {
                String[] limites = intervaloTexto.split("\\.\\.");
                int inicio = Integer.parseInt(limites[0]);
                int fim = Integer.parseInt(limites[1]);
                for (int i = inicio; i <= fim; i++) {
                    saida.append(String.format("case %d:\n", i));
                }
            } else {
                saida.append(String.format("case %s:\n", intervaloTexto));
            }

            for (LAParser.CmdContext cmdCtx : selecaoCtx.cmd()) {
                visitCmd(cmdCtx);
            }
            saida.append("break;\n");
        }

        saida.append("default:\n");
        for (LAParser.CmdContext cmdCtx : ctx.cmd()) {
            visitCmd(cmdCtx);
        }
        saida.append("}\n");

        return null;
    }

    @Override
    public Void visitCmdRetorne(LAParser.CmdRetorneContext ctx) {
        saida.append(String.format("return %s;\n", ctx.expressao().getText()));
        return null;
    }

    @Override
    public Void visitCmdChamada(LAParser.CmdChamadaContext ctx) {
        saida.append(ctx.IDENT().getText()).append("(");
        
        for (int i = 0; i < ctx.expressao().size(); i++) {
            saida.append(ctx.expressao(i).getText());
            if (i < ctx.expressao().size() - 1) {
                saida.append(", ");
            }
        }
        saida.append(");\n");
        return null;
    }
}