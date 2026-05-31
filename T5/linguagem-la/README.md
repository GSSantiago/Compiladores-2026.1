# Trabalho 5: Gerador de Código em C - Linguagem LA

Este projeto é a etapa final (**T5**) da disciplina de *Construção de Compiladores*. O objetivo deste trabalho é unificar todas as fases anteriores (Analisador Léxico, Sintático e Semântico) e adicionar a etapa de **Geração de Código**, traduzindo programas escritos na Linguagem Algorítmica (**LA**) para a linguagem **C**.

    Guilherme de Souza Santiago - RA: 790847
    Maria Eduarda Moura Crusco - RA: 823060


---

## Sobre o Projeto

O compilador funciona como um pipeline completo (Léxico -> Sintático -> Semântico -> Gerador). O fluxo de execução obedece às seguintes regras:

1. **Análise de Erros:** O programa verifica erros léxicos, sintáticos e semânticos. Se houver **qualquer erro** em alguma dessas etapas, o compilador aborta a geração de código e imprime os erros encontrados no arquivo de saída.
2. **Geração de Código (Tradução):** Se o código LA de entrada não possuir erros, o compilador atua como um tradutor utilizando o padrão **Visitor** do ANTLR4, gerando um código equivalente e válido em linguagem **C**.

### Principais Traduções Implementadas

* Conversão de tipos LA (*inteiro*, *real*, *literal*, *logico*) para tipos nativos em C (**int**, **float**, **char[]**, **int**).
* Tratamento de I/O com geração correta de máscaras para **printf** e **scanf** ou **gets**.
* Mapeamento de operadores lógicos relacionais (*e*, *ou*, *nao*, *<>*) para seus equivalentes em C (**&&**, **||**, **!**, **!=**).
* Tradução de estruturas de controle e repetição (*se*, *enquanto*, *faca...ate*, *caso*, *para*).
* Suporte à tradução de sub-rotinas (Procedimentos para **void** e Funções com retorno).
* Suporte a Registros (convertidos para **structs** e **typedef structs** no C) e Ponteiros.

---

## Pré-requisitos de Instalação

Para compilar e executar o projeto, você precisará ter instalado em sua máquina:

* **Java 11** ou superior
* **Maven** (Apache Maven 3.6+)
* **GCC** (GNU Compiler Collection)

---

## Como Compilar o Compilador (Java)

O projeto utiliza o **Maven** para gerenciamento de dependências e automação do build. Para gerar as classes do ANTLR e empacotar o projeto em um arquivo JAR executável, abra o terminal na raiz do projeto (onde está o arquivo *pom.xml*) e execute:

```bash
mvn clean package

```

O build irá gerar um executável chamado **linguagem-la-1.0-SNAPSHOT-jar-with-dependencies.jar** dentro da pasta **target/**.

---

## Como Executar o Compilador

O compilador exige obrigatoriamente a passagem de **dois argumentos** via linha de comando: o arquivo *.txt* contendo o código LA de entrada e o arquivo *.c* (ou *.txt* em caso de erros) onde a saída será salva.

Execute o seguinte comando:

```bash
java -jar target/linguagem-la-1.0-SNAPSHOT-jar-with-dependencies.jar <caminho_do_arquivo_de_entrada> <caminho_do_arquivo_de_saida>

```

Exemplo prático:

```bash
java -jar target/linguagem-la-1.0-SNAPSHOT-jar-with-dependencies.jar entrada/programa.txt saida/programa.c

```

---

