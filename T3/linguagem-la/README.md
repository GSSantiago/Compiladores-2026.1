# Trabalho 3: Analisador Semântico - Linguagem LA

Este trabalho consiste na implementação do Analisador Semântico para a linguagem LA, como parte do desenvolvimento do compilador para a disciplina de Construção de Compiladores.

## Sobre o Projeto
O analisador semântico é responsável por garantir que o código, embora sintaticamente correto, respeite as regras lógicas e de tipagem da linguagem. Esta implementação utiliza o padrão **Visitor** do ANTLR4 para percorrer a árvore sintática e realizar as validações necessárias.

### Verificações Implementadas:
O analisador detecta e reporta os seguintes erros:
*   **Erros de Identificadores:** Detecção de variáveis, constantes ou funções já declaradas no mesmo escopo ou uso de identificadores não declarados.
*   **Erros de Tipo:** Identificação de tipos inexistentes ou não declarados.
*   **Incompatibilidade de Atribuição:** Validação de tipos em atribuições e expressões (ex: impedir operações entre tipos incompatíveis como `literal` e `logico`).

O diferencial desta etapa é que o analisador **não interrompe a execução** no primeiro erro encontrado, reportando todas as falhas semânticas até o fim do arquivo.

## Pré-requisitos
*   **Java 11** ou superior
*   **Maven** (Apache Maven)
*   **ANTLR 4.11.1**

## Como Compilar
Para gerar as classes do ANTLR e empacotar o projeto em um arquivo JAR, execute o comando abaixo na raiz do projeto:
```bash
mvn package
```

## Como Executar

Após a compilação, o JAR será gerado na pasta `target`. Utilize o comando abaixo passando os arquivos de entrada e saída:
```bash
java -jar target/linguagem-algoritmica-1.0-SNAPSHOT-jar-with-dependencies.jar <caminho_entrada> <caminho_saida>
```
