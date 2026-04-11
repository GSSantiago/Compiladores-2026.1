## Trabalho 1
O projeto consiste em um **Analisador Léxico** desenvolvido para a linguagem LA (Linguagem Algorítmica).

## Requisitos
* **Java 11** ou superior
* **Maven**

## Como Compilar
No terminal, dentro da pasta raiz do projeto, execute os seguintes comandos:

1. **Gerar os fontes do ANTLR:**
   ```bash
   mvn antlr4:antlr4
2. **Compilar o Java e gerar o executável (JAR):**
   ```bash
   mvn compiler:compile assembly:single
  Nota: O arquivo executável será gerado em: target/linguagem-la-1.0-SNAPSHOT-jar-with-dependencies.jar

3. **Como Executar:**
   ```bash
   java -jar target/linguagem-la-1.0-SNAPSHOT-jar-with-dependencies.jar "entrada.txt" "saida.txt"
