package br.ufscar.dc.compiladores.linguagem.la;

import org.antlr.v4.runtime.Token;
import java.util.ArrayList;
import java.util.List;

public class SimLangSemantico extends SimLangBaseVisitor<Void> {

    TabelaSimbolos escopoAtual;
    
    // Variável para rastrear qual Sim estamos analisando no momento
    private String simAtualNome; 

    public SimLangSemantico() {
        this.escopoAtual = new TabelaSimbolos();
    }

    @Override
    public Void visitDeclaracaoSim(SimLangParser.DeclaracaoSimContext ctx) {
        String nomeSim = ctx.NOME_SIM().getText().replace("\"", "");
        
        // Verifica duplicidade na tabela de símbolos
        if (escopoAtual.existeSim(nomeSim)) {
            SimLangSemanticoUtils.adicionarErro(ctx.SIM().getSymbol(), "O Sim '" + nomeSim + "' já foi declarado.");
            return null; // Interrompe para não bugar a leitura
        } else {
            escopoAtual.adicionarSim(nomeSim);
        }

        // Define o Sim atual para os próximos métodos saberem onde salvar os dados
        this.simAtualNome = nomeSim;

        // Desce na árvore para ler a Idade, Aspiração, Traços e Habilidades
        super.visitDeclaracaoSim(ctx);

        // Agora que lemos todos os atributos do bloco, rodamos as regras de negócio
        validarRegras(ctx);

        this.simAtualNome = null; // Limpa para o próximo Sim
        return null;
    }

    // --- MÉTODOS DE COLETA DE DADOS ---

    @Override
    public Void visitAtributoIdade(SimLangParser.AtributoIdadeContext ctx) {
        TabelaSimbolos.RegistroSim sim = escopoAtual.getSim(simAtualNome);
        sim.idade = ctx.IDENTIFICADOR().getText();
        return super.visitAtributoIdade(ctx);
    }

    @Override
    public Void visitAtributoAspiracao(SimLangParser.AtributoAspiracaoContext ctx) {
        TabelaSimbolos.RegistroSim sim = escopoAtual.getSim(simAtualNome);
        sim.aspiracao = ctx.IDENTIFICADOR().getText();
        return super.visitAtributoAspiracao(ctx);
    }

    @Override
    public Void visitListaTracos(SimLangParser.ListaTracosContext ctx) {
        TabelaSimbolos.RegistroSim sim = escopoAtual.getSim(simAtualNome);
        // Pega cada traço separado por vírgula e guarda na lista
        for (var tracoToken : ctx.IDENTIFICADOR()) {
            sim.tracos.add(tracoToken.getText());
        }
        return super.visitListaTracos(ctx);
    }

    @Override
    public Void visitHabilidade(SimLangParser.HabilidadeContext ctx) {
        String nomeHabilidade = ctx.IDENTIFICADOR().getText();
        int nivel = Integer.parseInt(ctx.NUMERO().getText());
        TabelaSimbolos.RegistroSim sim = escopoAtual.getSim(simAtualNome);

        // Regra Semântica 3: Teto de Habilidades
        // Habilidades menores vão até o nível 5, as demais vão até 10.
        int limiteNivel = 10;
        if (nomeHabilidade.equals("Fotografia") || nomeHabilidade.equals("Danca") || nomeHabilidade.equals("Boliche")) {
            limiteNivel = 5;
        }

        if (nivel > limiteNivel || nivel < 1) {
            SimLangSemanticoUtils.adicionarErro(ctx.IDENTIFICADOR().getSymbol(), 
                "Nivel invalido para a habilidade '" + nomeHabilidade + "'. O nivel deve estar entre 1 e " + limiteNivel + ".");
        }

        // Salva a habilidade no mapa para a checagem de idade mais abaixo
        sim.habilidades.put(nomeHabilidade, nivel);

        return super.visitHabilidade(ctx);
    }

    // --- VALIDAÇÃO DAS REGRAS RESTRITIVAS ---

    private void validarRegras(SimLangParser.DeclaracaoSimContext ctx) {
        TabelaSimbolos.RegistroSim sim = escopoAtual.getSim(simAtualNome);
        
        if (sim.idade == null) return; // Se esqueceu a idade (erro de sintaxe), pula.

        // Regra 4: Limite de Traços por Idade
        int limiteTracos = 3; // Jovem Adulto, Adulto e Idoso
        if (sim.idade.equals("Bebe") || sim.idade.equals("Crianca")) {
            limiteTracos = 1;
        } else if (sim.idade.equals("Adolescente")) {
            limiteTracos = 2;
        }

        if (sim.tracos.size() > limiteTracos) {
            SimLangSemanticoUtils.adicionarErro(ctx.SIM().getSymbol(), 
                "Incompatibilidade: Um Sim '" + sim.idade + "' pode ter no maximo " + limiteTracos + 
                " traco(s). Foram declarados " + sim.tracos.size() + ".");
        }

        // Regra 1: Traços Conflitantes
        // Conflito 1: Bom x Maligno
        if ((sim.tracos.contains("Bom") || sim.tracos.contains("Boa")) && 
            (sim.tracos.contains("Maligno") || sim.tracos.contains("Maligna"))) {
            SimLangSemanticoUtils.adicionarErro(ctx.SIM().getSymbol(), 
                "Conflito de Tracos: Um Sim não pode ser 'Bom' e 'Maligno' simultaneamente.");
        }

        // Conflito 2: Ativo x Preguiçoso
        if (sim.tracos.contains("Ativo") && sim.tracos.contains("Preguicoso")) {
            SimLangSemanticoUtils.adicionarErro(ctx.SIM().getSymbol(), 
                "Conflito de Tracos: Um Sim não pode ser 'Ativo' e 'Preguicoso' simultaneamente.");
        }

        // Conflito 3: Devorador de Livros x Odeia Arte/Livros
        if (sim.tracos.contains("Devorador_De_Livros") && 
           (sim.tracos.contains("Odeia_Arte") || sim.tracos.contains("Odeia_Livros"))) {
            SimLangSemanticoUtils.adicionarErro(ctx.SIM().getSymbol(), 
                "Conflito de Tracos: Um 'Devorador_De_Livros' não pode odiar arte ou livros.");
        }

        // Regra 2: Limites de Idade para Habilidades e Aspirações
        if (sim.idade.equals("Bebe") || sim.idade.equals("Crianca")) {
            
            // Bloqueio de habilidades exclusivas de adultos
            if (sim.habilidades.containsKey("Programacao") || sim.habilidades.containsKey("Mixologia")) {
                SimLangSemanticoUtils.adicionarErro(ctx.SIM().getSymbol(), 
                    "Incompatibilidade: Um Sim da idade '" + sim.idade + "' nao possui capacidade para a habilidade de adulto declarada.");
            }
            
            // Bloqueio de aspirações não permitidas (ex: Romance, Carreira)
            if (sim.aspiracao != null && (sim.aspiracao.equals("Chef_De_Sucesso") || sim.aspiracao.equals("Romantico_Serial"))) {
                SimLangSemanticoUtils.adicionarErro(ctx.SIM().getSymbol(), 
                    "Incompatibilidade: A aspiracao '" + sim.aspiracao + "' nao esta disponivel para a idade '" + sim.idade + "'.");
            }
        }
    }
}