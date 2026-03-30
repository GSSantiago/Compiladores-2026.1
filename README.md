# Compiladores-2026.1

Guilherme de Souza Santiago RA: 790847
Maria Eduarda Moura Crusco RA: 823060 

Trabalho 1 
Este projeto é um Analisador Léxico para a linguagem LA, desenvolvido como parte da disciplina de Construção de Compiladores 1.
Requisitos
  -Java 11 ou superior
  -Maven
Como compilar
  Gerar código do ANTLR
    -mvn antlr4:antlr4
  Compilar o Java e gerar o JA
    -mvn compiler:compile assembly:single
  
  O arquivo executável será gerado em: target/linguagem-la-1.0-SNAPSHOT-jar-with-dependencies.jar

Como Executar
Use o comando abaixo passando o arquivo de entrada e o de saída: 
  -java -jar target/linguagem-la-1.0-SNAPSHOT-jar-with-dependencies.jar "entrada.txt" "saida.txt"
