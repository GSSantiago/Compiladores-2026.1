package br.ufscar.dc.compiladores.linguagem.la;

import java.util.HashMap;
import java.util.Map;

import static br.ufscar.dc.compiladores.linguagem.la.LASemanticoUtils.reduzNome;

public class TabelaSimbolos {

    public enum TipoLA {
        INTEIRO,
        REAL,
        INVALIDO,
        LOGICO,
        LITERAL,
        VOID,
        REGISTRO,
        TIPOESTENDIDO
    }

    public enum TipoEntrada {
        VARIAVEL,
        PROCEDIMENTO,
        FUNCAO
    }

    class EntradaTabelaDeSimbolos {

        String nome;
        TipoLA tipo;
        TipoEntrada tipoEnt;

        private EntradaTabelaDeSimbolos(String nome, TipoLA tipo, TipoEntrada tipoEnt) {
            this.nome = nome;
            this.tipo = tipo;
            this.tipoEnt = tipoEnt;
        }
    }

    private final Map<String, EntradaTabelaDeSimbolos> tabela;

    public TabelaSimbolos() {
        this.tabela = new HashMap<>();
    }

    public void adicionar(String nome, TipoLA tipo, TipoEntrada tipoEnt) {
        nome = reduzNome(nome, "[");
        tabela.put(nome, new EntradaTabelaDeSimbolos(nome, tipo, tipoEnt));
    }

    public boolean existe(String nome) {
        nome = reduzNome(nome, "[");
        return tabela.containsKey(nome);
    }

    public TipoLA verificar(String nome) {
        nome = reduzNome(nome, "[");
        return tabela.get(nome).tipo;
    }

}