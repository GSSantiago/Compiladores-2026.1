package br.ufscar.dc.compiladores.linguagem.la;

import java.util.HashMap;
import java.util.Map;

public class SimLangGeradorPython extends SimLangBaseVisitor<Void> {

    public StringBuilder saida = new StringBuilder();
    private TabelaSimbolos tabela;
    private String simAtualNome;

    private Map<String, String> mapTracos = new HashMap<>();
    private Map<String, String> mapHabilidades = new HashMap<>();

    public SimLangGeradorPython(TabelaSimbolos tabela) {
        this.tabela = tabela;

        mapTracos.put("Genio", "trait_Genius");
        mapTracos.put("Alegre", "trait_Cheerful");
        mapTracos.put("Ambiciosa", "trait_Ambitious");
        mapTracos.put("Ambicioso", "trait_Ambitious");
        mapTracos.put("Seguro", "trait_SelfAssured");
        mapTracos.put("Focado", "trait_Focused");

        mapHabilidades.put("Programacao", "Major_Programming");
        mapHabilidades.put("Logica", "Major_Logic");
        mapHabilidades.put("Pesquisa", "Major_ResearchDebate");
        mapHabilidades.put("Carisma", "Major_Charisma");
    }

    @Override
    public Void visitPrograma(SimLangParser.ProgramaContext ctx) {
        saida.append("import sims4.commands\n\n");
        saida.append("@sims4.commands.Command('aplicar_simlang', command_type=sims4.commands.CommandType.Live)\n");
        saida.append("def aplicar_simlang(_connection=None):\n");
        saida.append("    output = sims4.commands.CheatOutput(_connection)\n");
        saida.append("    output('Aplicando mods gerados pela SimLang...')\n");
        
        // A PROVA REAL: Injete 50.000 simoleons! Se isso funcionar, o seu compilador venceu.
        saida.append("    sims4.commands.client_cheat('motherlode', _connection)\n\n");

        super.visitPrograma(ctx);

        saida.append("    output('Todos os atributos foram aplicados com sucesso!')\n");
        return null;
    }

    @Override
    public Void visitDeclaracaoSim(SimLangParser.DeclaracaoSimContext ctx) {
        simAtualNome = ctx.NOME_SIM().getText().replace("\"", "");
        TabelaSimbolos.RegistroSim sim = tabela.getSim(simAtualNome);

        saida.append("    # Configurando o Sim: ").append(simAtualNome).append("\n");

        for (String traco : sim.tracos) {
            String cheatTraco = mapTracos.getOrDefault(traco, "trait_" + traco);
            saida.append("    sims4.commands.client_cheat('traits.equip_trait ")
                 .append(cheatTraco).append("', _connection)\n");
        }

        for (Map.Entry<String, Integer> hab : sim.habilidades.entrySet()) {
            String cheatHab = mapHabilidades.getOrDefault(hab.getKey(), "Major_" + hab.getKey());
            saida.append("    sims4.commands.client_cheat('stats.set_skill_level ")
                 .append(cheatHab).append(" ").append(hab.getValue()).append("', _connection)\n");
        }
        saida.append("\n");

        return null;
    }
}