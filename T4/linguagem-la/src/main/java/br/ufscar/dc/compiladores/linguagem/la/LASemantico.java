package br.ufscar.dc.compiladores.linguagem.la;

import static br.ufscar.dc.compiladores.linguagem.la.LASemanticoUtils.verificarTipo;
import static br.ufscar.dc.compiladores.linguagem.la.LASemanticoUtils.adicionaErroSemantico;
import static br.ufscar.dc.compiladores.linguagem.la.LASemanticoUtils.verificaCompatibilidade;
import static br.ufscar.dc.compiladores.t4.LASemanticoUtils.confereTipo;
import br.ufscar.dc.compiladores.t4.TabelaSimbolos.TipoEntrada;
import br.ufscar.dc.compiladores.linguagem.la.TabelaSimbolos.TipoLA;
import org.antlr.v4.runtime.Token;
import java.util.ArrayList;
import java.util.HashMap;

public class LASemantico extends LABaseVisitor<Void> {

    // Renomeado de 'tabela' para 'tabelaAtual' para ser mais descritivo
    TabelaSimbolos tabelaAtual;

    // Mantido como estático para que os Utils consigam acessar
    static Escopos escoposAninhados = new Escopos();

    static HashMap<String, ArrayList<TipoLA>> dadosFuncaoProcedimento = new HashMap<>();
    
    HashMap<String, ArrayList<String>> tabelaRegistro = new HashMap<>();

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
    public Void visitDeclaracao_local(LAParser.Declaracao_localContext ctx) {
        tabela = escoposAninhados.obterEscopoAtual();

        String tipoVariavel;
        String nomeVariavel;
                

        if (ctx.getText().contains("declare")) {

            if (ctx.variavel().tipo().registro() != null) {

                for (LAParser.IdentificadorContext ic : ctx.variavel().identificador()) {
                    adicionaSimboloTabela(ic.getText(), "registro", ic.getStart(), null, TipoEntrada.VARIAVEL);

                    for (LAParser.VariavelContext vc : ctx.variavel().tipo().registro().variavel()) {
                        tipoVariavel = vc.tipo().getText();
                        
                        for (LAParser.IdentificadorContext icr : vc.identificador())
                            adicionaSimboloTabela(ic.getText() + "." + icr.getText(), tipoVariavel, icr.getStart(), vc.tipo().getStart(), TipoEntrada.VARIAVEL);
                    }
                }

            } else {
                tipoVariavel = ctx.variavel().tipo().getText(); 

                if (tabelaRegistro.containsKey(tipoVariavel)) {
                    ArrayList<String> variaveisRegistro = tabelaRegistro.get(tipoVariavel);
                    
                    for (LAParser.IdentificadorContext ic : ctx.variavel().identificador()) {
                        nomeVariavel = ic.IDENT().get(0).getText();
                        
                        if (tabela.existe(nomeVariavel) || tabelaRegistro.containsKey(nomeVariavel)) {
                            adicionaErroSemantico(ic.getStart(), "identificador " + nomeVariavel + " ja declarado anteriormente");
                        } else {  
                            adicionaSimboloTabela(nomeVariavel, "registro", ic.getStart(), ctx.variavel().tipo().getStart(), TipoEntrada.VARIAVEL);                            

                            for (int i = 0; i < variaveisRegistro.size(); i = i + 2) {
                                adicionaSimboloTabela(nomeVariavel + "." + variaveisRegistro.get(i), variaveisRegistro.get(i+1), ic.getStart(), ctx.variavel().tipo().getStart(), TipoEntrada.VARIAVEL);
                            }
                        }
                    }

                } else {
                    for (LAParser.IdentificadorContext ident : ctx.variavel().identificador()) {
                        nomeVariavel = ident.getText();
                        

                        if (dadosFuncaoProcedimento.containsKey(nomeVariavel))
                            adicionaErroSemantico(ident.getStart(), "identificador " + nomeVariavel + " ja declarado anteriormente");
                        else
                            adicionaSimboloTabela(nomeVariavel, tipoVariavel, ident.getStart(), ctx.variavel().tipo().getStart(), TipoEntrada.VARIAVEL); 
                    }
                }
            }

        } else if (ctx.getText().contains("tipo")) {
            
            if (ctx.tipo().registro() != null) {
                ArrayList<String> variaveisRegistro = new ArrayList<>();
                
                for (LAParser.VariavelContext vc : ctx.tipo().registro().variavel()) {
                    tipoVariavel = vc.tipo().getText();
                    
                    for (LAParser.IdentificadorContext ic : vc.identificador()) {
                        variaveisRegistro.add(ic.getText());
                        variaveisRegistro.add(tipoVariavel);
                    }
                }
                tabelaRegistro.put(ctx.IDENT().getText(), variaveisRegistro);
            }

        } else if (ctx.getText().contains("constante"))
            adicionaSimboloTabela(ctx.IDENT().getText(), ctx.tipo_basico().getText(), ctx.IDENT().getSymbol(), ctx.IDENT().getSymbol(), TipoEntrada.VARIAVEL);
        
        return super.visitDeclaracao_local(ctx);
    }

    @Override
    public Void visitDeclaracao_global(LAParser.Declaracao_globalContext ctx) {

        escoposAninhados.criarNovoEscopo();
        
        tabela = escoposAninhados.obterEscopoAtual();


        ArrayList<TipoLA> tiposVariaveis = new ArrayList<>();
        ArrayList<String> variaveisRegistro;
                
        String tipoVariavel;
        TipoLA tipoAux;

        if (ctx.getText().contains("procedimento")) {
            
            for (LAParser.ParametroContext parametro : ctx.parametros().parametro()) {

                if (parametro.tipo_estendido().tipo_basico_ident().tipo_basico() != null) {

                    adicionaSimboloTabela(parametro.identificador().get(0).getText(), parametro.tipo_estendido().tipo_basico_ident().tipo_basico().getText(), parametro.getStart(), parametro.getStart(), TipoEntrada.VARIAVEL);
                    

                    tipoVariavel = parametro.tipo_estendido().getText();
                    tipoAux = confereTipo(tabelaRegistro, tipoVariavel);
                    tiposVariaveis.add(tipoAux);

                } else if (tabelaRegistro.containsKey(parametro.tipo_estendido().tipo_basico_ident().IDENT().getText())) {

                    variaveisRegistro = tabelaRegistro.get(parametro.tipo_estendido().tipo_basico_ident().IDENT().getText());


                    tipoVariavel = parametro.tipo_estendido().getText();
                    tipoAux = confereTipo(tabelaRegistro, tipoVariavel);
                    tiposVariaveis.add(tipoAux);

                    for (LAParser.IdentificadorContext ic : parametro.identificador())

                        for (int i = 0; i < variaveisRegistro.size(); i = i + 2)
                            adicionaSimboloTabela(ic.getText() + "." + variaveisRegistro.get(i), variaveisRegistro.get(i + 1), ic.getStart(), ic.getStart(), TipoEntrada.VARIAVEL);                       
                } else
                    adicionaErroSemantico(parametro.getStart(), "tipo nao declarado");                       
            }
            for (LAParser.CmdContext c : ctx.cmd())    
                if (c.cmdRetorne() != null)  
                    adicionaErroSemantico(c.getStart(), "comando retorne nao permitido nesse escopo");    

            dadosFuncaoProcedimento.put(ctx.IDENT().getText(), tiposVariaveis);

        } else if (ctx.getText().contains("funcao")) {
            for (LAParser.ParametroContext parametro : ctx.parametros().parametro()) {
                
                if (parametro.tipo_estendido().tipo_basico_ident().tipo_basico() != null) {
                
                    adicionaSimboloTabela(parametro.identificador().get(0).getText(), parametro.tipo_estendido().tipo_basico_ident().tipo_basico().getText(), parametro.getStart(), parametro.getStart(), TipoEntrada.VARIAVEL);

                    tipoVariavel = parametro.tipo_estendido().getText();
                    tipoAux = confereTipo(tabelaRegistro, tipoVariavel);
                    tiposVariaveis.add(tipoAux);
                } else if (tabelaRegistro.containsKey(parametro.tipo_estendido().tipo_basico_ident().IDENT().getText())) {

                    variaveisRegistro = tabelaRegistro.get(parametro.tipo_estendido().tipo_basico_ident().IDENT().getText());

                    tipoVariavel = parametro.tipo_estendido().tipo_basico_ident().IDENT().getText();
                    tipoAux = confereTipo(tabelaRegistro, tipoVariavel);
                    tiposVariaveis.add(tipoAux);
                    
                    for (LAParser.IdentificadorContext ic : parametro.identificador())
                        for (int i = 0; i < variaveisRegistro.size(); i = i + 2)
                                adicionaSimboloTabela(ic.getText() + "." + variaveisRegistro.get(i), variaveisRegistro.get(i + 1), ic.getStart(), ic.getStart(), TipoEntrada.VARIAVEL);
                } else
                    adicionaErroSemantico(parametro.getStart(), "tipo nao declarado");
            }

            dadosFuncaoProcedimento.put(ctx.IDENT().getText(), tiposVariaveis);
        }
        
        super.visitDeclaracao_global(ctx);

        escoposAninhados.abandonarEscopo();

        if (ctx.getText().contains("procedimento"))      
            adicionaSimboloTabela(ctx.IDENT().getText(), "void", ctx.getStart(), ctx.getStart(), TipoEntrada.PROCEDIMENTO);
        else if (ctx.getText().contains("funcao"))
            adicionaSimboloTabela(ctx.IDENT().getText(), ctx.tipo_estendido().tipo_basico_ident().tipo_basico().getText(), ctx.getStart(), ctx.getStart(), TipoEntrada.FUNCAO);

        return null;
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
    public Void visitCmdEnquanto(LAParser.CmdEnquantoContext ctx) {
        tabela = escoposAninhados.obterEscopoAtual();
        
        TipoLA tipo = verificarTipo(tabela, ctx.expressao());
        
        return super.visitCmdEnquanto(ctx);
    }

    @Override
    public Void visitCmdSe(LAParser.CmdSeContext ctx) {
        tabela = escoposAninhados.obterEscopoAtual();
        
        TipoLA tipo = verificarTipo(tabela, ctx.expressao());
        
        return super.visitCmdSe(ctx);
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