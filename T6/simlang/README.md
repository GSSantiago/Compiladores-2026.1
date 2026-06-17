# Trabalho 6: SimLang



## Sobre o Projeto


### Verificações Implementadas:

## Pré-requisitos
* **Java 11** ou superior
* **Maven** (Apache Maven)
* **ANTLR 4.11.1**

## Como Compilar
Sempre que fizer alterações na gramática (`.g4`) ou nos arquivos Java, limpe o cache e recompile o projeto:

```bash
mvn clean compile
```
## Como Executar

Para rodar a classe principal e processar as análises léxica, sintática e semântica, utilize o plugin `exec:java` do Maven diretamente pelo terminal.

### Ambiente Linux / Mac / Git Bash
Execute o comando padrão do Maven:
```bash
mvn exec:java -Dexec.mainClass="br.ufscar.dc.compiladores.linguagem.la.Principal"
```
### Ambiente Windows
```bash
mmvn exec:java "-Dexec.mainClass=br.ufscar.dc.compiladores.linguagem.la.Principal"
```