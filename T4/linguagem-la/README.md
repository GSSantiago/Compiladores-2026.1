# Trabalho 4: Analisador Semântico II - Linguagem LA

Este trabalho consiste na implementação da segunda parte do Analisador Semântico para a linguagem LA, expandindo as capacidades do compilador desenvolvido para a disciplina de Construção de Compiladores.

## Sobre o Projeto
Nesta etapa, o analisador semântico foi aprimorado para lidar com estruturas de dados complexas e controle de fluxo de sub-rotinas. A implementação continua utilizando o padrão **Visitor** do ANTLR4, integrando-se à base desenvolvida nas etapas anteriores.

### Verificações Implementadas:
Além das validações do T3, o analisador agora detecta e reporta os seguintes erros:
* **Sub-rotinas:** Incompatibilidade entre argumentos e parâmetros formais (número, ordem e tipo) em chamadas de procedimentos e funções.
* **Registros e Vetores:** Validação de acesso a campos de registros e índices de vetores, incluindo verificações de escopo.
* **Ponteiros:** Verificação de atribuições compatíveis entre ponteiros e endereços de memória.
* **Comando de Retorno:** Detecção do uso do comando `retorne` em escopos não permitidos (fora de funções).
* **Erros de Identificadores:** Verificação de identificadores já declarados ou não declarados, agora abrangendo ponteiros, registros e funções.

O analisador **não interrompe a execução** ao encontrar um erro, garantindo que todas as falhas semânticas do código-fonte sejam reportadas no arquivo de saída.

## Pré-requisitos
* **Java 11** ou superior
* **Maven** (Apache Maven)
* **ANTLR 4.11.1**

## Como Compilar
Para gerar as classes do ANTLR e empacotar o projeto em um arquivo JAR com todas as dependências, execute o comando abaixo na raiz do projeto:
```bash
mvn package
```

## Como Executar

Após a compilação, o JAR será gerado na pasta `target`. Utilize o comando abaixo passando os arquivos de entrada e saída:
```bash
java -jar target/linguagem-la-1.0-SNAPSHOT-jar-with-dependencies.jar <caminho_entrada> <caminho_saida>
```
