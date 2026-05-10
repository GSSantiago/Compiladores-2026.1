package br.ufscar.dc.compiladores.linguagem.la;

import java.util.LinkedList;
import java.util.List;

public final class Escopos {

    private final LinkedList<TabelaSimbolos> pilhaDeTabelas;

    public Escopos() {
        pilhaDeTabelas = new LinkedList<>();
        criarNovoEscopo();
    }

    public void criarNovoEscopo() {
        pilhaDeTabelas.push(new TabelaSimbolos());
    }

    public TabelaSimbolos obterEscopoAtual() {
        return pilhaDeTabelas.peek();
    }

    public List<TabelaSimbolos> percorrerEscoposAninhados() {
        return pilhaDeTabelas;
    }
}