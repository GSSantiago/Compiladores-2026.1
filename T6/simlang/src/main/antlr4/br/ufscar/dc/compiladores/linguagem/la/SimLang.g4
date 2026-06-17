grammar SimLang;

// REGRAS SINTÁTICAS

// Ponto de entrada: zero ou mais declarações de Sim seguidas de EOF
programa
    : declaracaoSim* EOF
    ;

// Bloco principal de um personagem
declaracaoSim
    : SIM NOME_SIM ABRE_CHAVE atributos FECHA_CHAVE
    ;

// Conjunto de atributos dentro de um bloco Sim (cada um é obrigatório exatamente uma vez;
// a obrigatoriedade e unicidade serão verificadas na análise semântica)
atributos
    : atributo*
    ;

atributo
    : atributoIdade
    | atributoAspiracao
    | atributoTracos
    | atributoHabilidades
    ;

// Idade: Adulto, Jovem_Adulto, Crianca, Bebe, etc.
atributoIdade
    : IDADE DOIS_PONTOS IDENTIFICADOR PONTO_VIRGULA
    ;

// Aspiracao: apenas uma por Sim
atributoAspiracao
    : ASPIRACAO DOIS_PONTOS IDENTIFICADOR PONTO_VIRGULA
    ;

// Tracos: lista separada por vírgula (Regras semânticas 1 e 4)
atributoTracos
    : TRACO DOIS_PONTOS listaTracos PONTO_VIRGULA
    ;

listaTracos
    : IDENTIFICADOR (VIRGULA IDENTIFICADOR)*
    ;

// Habilidades: lista de pares Nome:Nivel (Regras semânticas 2 e 3)
atributoHabilidades
    : HABILIDADES DOIS_PONTOS listaHabilidades PONTO_VIRGULA
    ;

listaHabilidades
    : habilidade (VIRGULA habilidade)*
    ;

// Par habilidade e nível numérico — ex: Logica:10
habilidade
    : IDENTIFICADOR DOIS_PONTOS NUMERO
    ;


// REGRAS LÉXICAS — PALAVRAS RESERVADAS


SIM         : 'Sim' ;
TRACO       : 'Traco' ;
HABILIDADES : 'Habilidades' ;
IDADE       : 'Idade' ;
ASPIRACAO   : 'Aspiracao' ;


// REGRAS LÉXICAS — SÍMBOLOS E PONTUAÇÕES

ABRE_CHAVE    : '{' ;
FECHA_CHAVE   : '}' ;
DOIS_PONTOS   : ':' ;
PONTO_VIRGULA : ';' ;
VIRGULA       : ',' ;


//REGRAS LÉXICAS — LITERAIS E IDENTIFICADORES

// Nome do Sim entre aspas duplas — ex: "Cesar Augusto"
NOME_SIM      : '"' ~('"')+ '"' ;

// Nomes de traços, habilidades, idades, aspirações — ex: Genio, Logica, Jovem_Adulto
IDENTIFICADOR : [a-zA-ZÀ-ÿ_][a-zA-ZÀ-ÿ0-9_]* ;

// Nível numérico de habilidade — ex: 8
NUMERO        : [0-9]+ ;


//CANAIS IGNORADOS

WS         : [ \t\r\n]+    -> skip ;
COMENTARIO : '//' ~[\r\n]* -> skip ;
