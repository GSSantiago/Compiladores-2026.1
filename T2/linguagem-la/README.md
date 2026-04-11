# Trabalho 2: Analisador Sintático - Linguagem LA

Este trabalho implementa o **Analisador Sintático** (e Léxico) para a linguagem LA (Linguagem Algorítmica).


## Sobre o Projeto
O compilador foi construído utilizando a ferramenta **ANTLR4** e a linguagem **Java**. 
1. **Análise Léxica:** Realiza a leitura e validação dos tokens. Caso encontre erros léxicos (símbolos não identificados, cadeias literais não fechadas ou comentários não fechados), o erro é reportado e a execução é interrompida.
2. **Análise Sintática:** Estando o arquivo livre de erros léxicos, o parser verifica se a estrutura do código obedece rigorosamente à gramática da linguagem LA. O projeto utiliza um `ErrorListener` customizado para interceptar exceções do ANTLR e formatar as mensagens de erro no padrão exato exigido pelo corretor automático (ex: `Linha X: erro sintatico proximo a Y`).

## Pré-requisitos
* **Java 11** (ou superior)
* **Maven** (Apache Maven)

## Como Compilar
O projeto está configurado para automatizar a geração das classes do ANTLR. No terminal, dentro da pasta raiz do projeto (onde está o arquivo `pom.xml`), execute o seguinte comando:

```bash
mvn clean package
```

### Como Executar

Para executar, utilize o comando abaixo informando o caminho do arquivo (entrada) e o caminho do arquivo onde o resultado será salvo (saída):

```bash
java -jar target/linguagem-la-1.0-SNAPSHOT-jar-with-dependencies.jar <caminho_arquivo_entrada> <caminho_arquivo_saida>
```

