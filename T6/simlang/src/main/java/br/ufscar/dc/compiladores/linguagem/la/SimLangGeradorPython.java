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

        mapTracos.put("Genio", "Genius");
        mapTracos.put("Alegre", "Cheerful");
        mapTracos.put("Ambiciosa", "Ambitious");
        mapTracos.put("Ambicioso", "Ambitious");
        mapTracos.put("Seguro", "SelfAssured");
        mapTracos.put("Focado", "Focused");

        mapHabilidades.put("Programacao", "Major_Programming");
        mapHabilidades.put("Logica", "Major_Logic");
        mapHabilidades.put("Pesquisa", "Major_ResearchDebate");
        mapHabilidades.put("Carisma", "Major_Charisma");
    }

    @Override
    public Void visitPrograma(SimLangParser.ProgramaContext ctx) {
        saida.append("import sims4.commands\n\n");
        saida.append("@sims4.commands.Command('simlang_ping', command_type=sims4.commands.CommandType.Live, console_type=sims4.commands.CommandType.Cheat)\n");
        saida.append("def simlang_ping(_connection=None):\n");
        saida.append("    output = sims4.commands.CheatOutput(_connection)\n");
        saida.append("    output('SimLang carregado com sucesso.')\n\n");
        saida.append("@sims4.commands.Command('aplicar_simlang', 'simlang', command_type=sims4.commands.CommandType.Live, console_type=sims4.commands.CommandType.Cheat)\n");
        saida.append("def aplicar_simlang(_connection=None):\n");
        saida.append("    output = sims4.commands.CheatOutput(_connection)\n");
        saida.append("    def executar_comando(comando):\n");
        saida.append("        sims4.commands.client_cheat('|' + comando, _connection)\n\n");
        saida.append("    output('Aplicando mods gerados pela SimLang...')\n");
        saida.append("    try:\n");
        saida.append("        executar_comando('testingcheats on')\n");
        saida.append("        executar_comando('motherlode')\n\n");

        super.visitPrograma(ctx);

        saida.append("        output('Todos os atributos foram aplicados com sucesso!')\n");
        saida.append("    except Exception as exc:\n");
        saida.append("        output('Erro ao aplicar SimLang: {}'.format(exc))\n");
        return null;
    }

    @Override
    public Void visitDeclaracaoSim(SimLangParser.DeclaracaoSimContext ctx) {
        simAtualNome = ctx.NOME_SIM().getText().replace("\"", "");
        TabelaSimbolos.RegistroSim sim = tabela.getSim(simAtualNome);

        saida.append("        # Configurando o Sim: ").append(simAtualNome).append("\n");

        for (String traco : sim.tracos) {
            String cheatTraco = mapTracos.getOrDefault(traco, traco);
            saida.append("        executar_comando('traits.equip_trait ")
                 .append(cheatTraco).append("')\n");
        }

        for (Map.Entry<String, Integer> hab : sim.habilidades.entrySet()) {
            String cheatHab = mapHabilidades.getOrDefault(hab.getKey(), "Major_" + hab.getKey());
            saida.append("        executar_comando('stats.set_skill_level ")
                 .append(cheatHab).append(" ").append(hab.getValue()).append("')\n");
        }
        saida.append("\n");

        return null;
    }
}