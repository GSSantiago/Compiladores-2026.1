lexer grammar LALexer;

//PALAVRAS-CHAVE
ALGORITMO : 'algoritmo';
FIM_ALGORITMO : 'fim_algoritmo';
DECLARE : 'declare';
LEIA : 'leia';
ESCREVA : 'escreva';
RETORNE : 'retorne';
SE : 'se';
ENTAO : 'entao';
SENAO : 'senao';
FIM_SE : 'fim_se';
CASO : 'caso';
SEJA : 'seja';
FIM_CASO : 'fim_caso';
PARA : 'para';
ATE : 'ate';
FACA : 'faca';
FIM_PARA : 'fim_para';
ENQUANTO : 'enquanto';
FIM_ENQUANTO : 'fim_enquanto';
TIPO : 'tipo';
REGISTRO : 'registro';
FIM_REGISTRO : 'fim_registro';
CONSTANTE : 'constante';
PROCEDIMENTO : 'procedimento';
FIM_PROCEDIMENTO : 'fim_procedimento';
FUNCAO : 'funcao';
FIM_FUNCAO : 'fim_funcao';
VAR : 'var';

//TIPOS E CONSTANTES LÓGICAS
INTEIRO : 'inteiro';
REAL : 'real';
LITERAL : 'literal';
LOGICO : 'logico';
VERDADEIRO : 'verdadeiro';
FALSO : 'falso';

//OPERADORES LÓGICOS
E : 'e';
OU : 'ou';
NAO : 'nao';

//NÚMEROS
NUM_REAL : [0-9]+ '.' [0-9]* { _input.LA(1) != '.' }? ;
NUM_INT : [0-9]+ ;

//SÍMBOLOS COMPOSTOS
ATRIBUICAO : '<-';
INTERVALO : '..';
OP_REL : '<=' | '>=' | '<>' | '<' | '>' | '=';

//SÍMBOLOS SIMPLES
OP_ARIT : '+' | '-' | '*' | '/';
PERCENT : '%';
PONTO : '.';
PONTEIRO : '^';
ENDERECO : '&';
ABRE_PAR : '(';
FECHA_PAR : ')';
ABRE_COL : '[';
FECHA_COL : ']';
VIRGULA : ',';
DOIS_PONTOS : ':';

//COMENTÁRIO E ESPAÇOS
WS : [ \t\r\n]+ -> skip ;
COMENTARIO : '{' .*? '}' -> skip ;
COMENTARIO_NAO_FECHADO : '{' ~'}'* ;

//CADEIA
CADEIA : '"' ( ~["\r\n] )* '"' ;
CADEIA_NAO_FECHADA : '"' ( ~["\r\n] )* ;

//IDENTIFICADOR
IDENT : [a-zA-Z_][a-zA-Z0-9_]* ;

//ERRO
ERRO : . ;