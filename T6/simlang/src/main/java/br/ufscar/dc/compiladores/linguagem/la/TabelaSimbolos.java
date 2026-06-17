package br.ufscar.dc.compiladores.linguagem.la;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabelaSimbolos {
    
    // Deixei as variáveis publicas para facilitar o acesso pelo Visitor
    public class RegistroSim {
        public String nome;
        public String idade; // Usando String para facilitar a comparação nas regras
        public String aspiracao;
        public List<String> tracos;
        public boolean temProgramacao; // Flag para a regra do Bebê
        
        public RegistroSim(String nome) {
            this.nome = nome;
            this.tracos = new ArrayList<>();
            this.temProgramacao = false;
        }
    }

    private final Map<String, RegistroSim> tabela;

    public TabelaSimbolos() {
        this.tabela = new HashMap<>();
    }

    public void adicionarSim(String nome) {
        tabela.put(nome, new RegistroSim(nome));
    }

    public boolean existeSim(String nome) {
        return tabela.containsKey(nome);
    }
    
    public RegistroSim getSim(String nome) {
        return tabela.get(nome);
    }
}