# Trabalho 6: SimLang - Compilador de Mods para The Sims 4

## Desenvolvedores do Grupo

* **Guilherme de Souza Santiago** - RA: 790847
* **Maria Eduarda Moura Crusco** - RA: 823060

Video demonstrando a linguagem: https://youtu.be/XPv2DG-X1K8
## Sobre o Projeto

A **SimLang** é uma *Domain-Specific Language* (DSL) declarativa projetada para automatizar e facilitar a criação de mods de personagens para o jogo **The Sims 4**. Em vez de digitar manualmente dezenas de *cheats* no console do jogo para configurar as personalidades e talentos de um Sim, o usuário escreve as características desejadas em um arquivo de texto limpo e legível.

Após o processamento (que inclui análises léxica, sintática e semântica), o compilador atua como um gerador de código, traduzindo o arquivo da DSL diretamente para um **script Python (.py)** nativo da engine do The Sims 4. Esse script empacota todos os atributos em um único comando executável dentro do jogo.

### Verificações Implementadas:

Para garantir que o código gerado não cause erros ou corrompa o jogo, a análise semântica implementa regras de negócio baseadas no universo de The Sims:

1. **Limite de Traços por Idade:** Verifica se a quantidade de traços informada respeita o limite do jogo (ex: bebês possuem apenas 1 traço, adultos possuem 3).
2. **Conflito de Personalidade:** Impede a associação de traços mutuamente exclusivos no mesmo Sim (ex: tentar ser simultaneamente *Bom* e *Maligno* ou *Ativo* e *Preguiçoso*).
3. **Restrições de Habilidades por Idade:** Bloqueia a atribuição de habilidades incompatíveis com a faixa etária do Sim (ex: crianças não podem aprender *Mixologia* ou *Programação*).
4. **Limites de Nível de Habilidade:** Valida se o nível numérico da habilidade informada está dentro do teto permitido (a maioria das habilidades vai até 10, mas habilidades menores, como *Fotografia*, vão apenas até 5).
5. **Restrições de Aspiração:** Impede que Sims muito jovens recebam aspirações exclusivas da fase adulta (como *Romântico Serial*).
6. **Unicidade de Declaração:** Impede que dois Sims com o mesmo nome exato sejam declarados no mesmo escopo.

---

## Exemplo de Uso (Caso de Teste)

Para utilizar a linguagem, crie um arquivo com a extensão **`.sim`**. A sintaxe exige a declaração de blocos contendo os identificadores de Idade, Aspiração, Traços e Habilidades:

```text
Sim "Maria Eduarda" {
    Idade: Jovem_Adulto;
    Aspiracao: Conhecimento;
    Traco: Genio, Alegre, Ambiciosa;
    Habilidades: Programacao:8, Logica:7;
}
```

Se nenhuma regra semântica for violada, o compilador gerará automaticamente um arquivo de mesma nomenclatura, mas com a extensão **`.py`**, contendo o script traduzido.

---

## Pré-requisitos

* **Java 11** ou superior
* **Maven** (Apache Maven)
* **ANTLR 4.11.1**
* **Python 3.7** (Opcional, essa é a versão que o The Sims 4 utiliza para lógica geral)
* Jogo **The Sims 4** (Opcional, apenas para testar a injeção do mod final)

---

## Como Compilar

Sempre que fizer alterações na gramática (`.g4`) ou implementar novas lógicas nos arquivos Java (`Visitor` ou `Utils`), limpe o cache e recompile o projeto executando na raiz:

```bash
mvn clean compile


```

---

## Como Executar

Para rodar a classe principal e processar os casos de teste completos, utilize os seguintes comandos:

Execute o comando:

```bash
mvn clean package
```

Após a geração do arquivo java, execute o seguinte comando:

```bash
java -jar target/simlang-1.0-SNAPSHOT-jar-with-dependencies.jar casos_de_teste/sim_valido_completo.sim
```

---

## Como Utilizar o Mod Gerado no Jogo (Geração de Código)

Para que a engine do The Sims 4 reconheça o seu mod de forma nativa, o script gerado precisa ser compilado para *bytecode* Python (`.pyc`) e empacotado corretamente. Siga os passos abaixo:

1. **Compilar para `.pyc`:** A engine do jogo exige estritamente a versão 3.7 do Python.
```powershell
py -3.7 -m compileall -b .\sim_valido_completo.py
```


2. **Empacotar o arquivo gerado:** Compacte **apenas** o arquivo `.pyc` recém-gerado em um formato ZIP.
```powershell
Compress-Archive -Path .\sim_valido_completo.pyc -DestinationPath .\simlang.zip -Force
```


3. **Renomear para a extensão de script do jogo:** Modifique a extensão para que o The Sims 4 o identifique como um mod.
```powershell
Rename-Item .\simlang.zip .\simlang.ts4script -Force
```


4. **Instalar o Mod:** Mova o arquivo `simlang.ts4script` para a pasta de Mods do jogo, localizada por padrão em:
`Documentos\Electronic Arts\The Sims 4\Mods`
5. **Habilitar Mods nas Configurações do Jogo:**
* Abra o The Sims 4.
* Acesse **Opções de Jogo** > **Outro**.
* Marque as caixas **Habilitar Conteúdo Personalizado e Modificações** e **Modificações de Script Permitidas**.
* Salve e **reinicie o jogo**.


6. **Executar o Script in-game:**
* Inicie o seu save ou crie uma família nova.
* Certifique-se de estar no **Modo Simulação** e selecione o Sim no qual deseja aplicar os atributos.
* Pressione `Ctrl + Shift + C` para abrir o console de cheats.
* Digite o comando gerado pelo seu script (ex: `aplicar_simlang`) e pressione **Enter**. O jogo aplicará os traços e habilidades instantaneamente.


```
