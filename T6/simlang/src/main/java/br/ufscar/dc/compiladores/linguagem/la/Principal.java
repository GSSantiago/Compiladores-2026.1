package br.ufscar.dc.compiladores.linguagem.la;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import java.io.File;
import java.io.IOException;

public class Principal {
    public static void main(String[] args) {
        File caminho = new File(args.length > 0 ? args[0] : "casos_de_teste");

        if (caminho.isDirectory()) {
            System.out.println("📂 Testando todos os arquivos da pasta: " + caminho.getPath());
            File[] arquivos = caminho.listFiles((dir, name) -> name.endsWith(".sim"));
            
            if (arquivos != null) {
                for (File arquivo : arquivos) {
                    testarArquivo(arquivo.getPath());
                }
            }
        } else {
            testarArquivo(caminho.getPath());
        }
    }

    private static void testarArquivo(String arquivoTeste) {
        System.out.println("\n--------------------------------------------------");
        System.out.println("🚀 Testando: " + arquivoTeste);
        
        try {
            CharStream cs = CharStreams.fromFileName(arquivoTeste);
            SimLangLexer lexer = new SimLangLexer(cs);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            SimLangParser parser = new SimLangParser(tokens);
            
            SimLangParser.ProgramaContext arvore = parser.programa();

            // >>> A REGRA DE OURO QUE IMPEDE O CRASH <<<
            // Se o ANTLR achou erro de digitação/sintaxe, aborta antes de chamar o Visitor!
            if (parser.getNumberOfSyntaxErrors() > 0) {
                System.out.println("❌ Erros Léxicos/Sintáticos encontrados pelo ANTLR. Análise semântica abortada para este arquivo.");
                return; 
            }

            SimLangSemanticoUtils.errosSemanticos.clear();
            SimLangSemantico semantico = new SimLangSemantico();
            semantico.visit(arvore);

            if (!SimLangSemanticoUtils.errosSemanticos.isEmpty()) {
                System.out.println("❌ ERROS SEMÂNTICOS ENCONTRADOS:");
                for (String erro : SimLangSemanticoUtils.errosSemanticos) {
                    System.out.println(erro);
                }
            } else {
                System.out.println("✅ SUCESSO! Nenhum erro semântico encontrado.");
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }
}