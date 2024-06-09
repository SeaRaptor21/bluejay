package com.esjr.bluejay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Bluejay {
    private static String source;
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    public static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        while (true) {
            StringBuilder sb = new StringBuilder();
            System.out.print(AnsiColors.BLUE + ">>> " + AnsiColors.RESET);
            String line = reader.readLine();
            if (line == null) {
                System.out.println();
                return;
            }
            sb.append(line);
            while (line.length() > 0) {
                System.out.print(AnsiColors.BLUE + "... " + AnsiColors.RESET);
                line = reader.readLine();
                if (line == null) {
                    System.out.println();
                    return;
                }
                sb.append("\n"+line);
            }
            run(sb.toString());
            hadError = false;
        }
    }

    private static void run(String src) {
        source = src;
        Scanner scanner = new Scanner(src);
        List<Token> tokens = scanner.scanTokens();

        if (hadError || tokens.size() <= 0) return;

        // for (Token t : tokens) {
        //     System.out.println(t.toString());
        // }

        Parser parser = new Parser(tokens);
        List<Stmt> stmts = parser.parse();

        if (hadError) return;

        Interpreter interpreter = new Interpreter();
        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(stmts);

        if (hadError) return;

        interpreter.interpret(stmts);

        // AstPrinter printer = new AstPrinter();
        // for (Stmt stmt : stmts) {
        //     System.out.println(printer.visit(stmt));
        // }
    }

    static void error(int pos, String message) {
        report(pos, "", message);
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF || token.type == TokenType.EOS && token.lexeme == "") {
            report(token.pos, " at end", message);
        } else {
            report(token.pos, " at '" + token.lexeme.replace("\n","\\n") + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        if (error.token == null) {
            System.err.println(AnsiColors.RED + error.getClass().getSimpleName() + ": " + error.getMessage() + AnsiColors.RESET);
        } else {
            int line = ((int)source.substring(0,error.token.pos).chars().filter(ch -> ch == '\n').count())+1;
            String[] lines;
            if (error.token.pos >= source.length()-1) {
                lines = source.split("\n");
            } else {
                lines = source.substring(0,error.token.pos+1).split("\n");
            }
            int col = lines[lines.length-1].length();
            System.err.println(AnsiColors.RED + error.getClass().getSimpleName() + ": " + error.getMessage() + AnsiColors.BLUE + "\n" + line + " | " + AnsiColors.RESET + source.split("\n")[line-1] + "\n  " + " ".repeat((int)Math.floor(Math.log10(line))+1+col) + AnsiColors.BLUE +  "^-- Here." + AnsiColors.RESET);
        }
        hadRuntimeError = true;
    }

    private static void report(int pos, String where, String message) {
        int line = ((int)source.substring(0,pos).chars().filter(ch -> ch == '\n').count())+1;
        String[] lines;
        if (pos >= source.length()-1) {
            lines = source.split("\n");
        } else {
            lines = source.substring(0,pos+1).split("\n");
        }
        int col = lines[lines.length-1].length();
        System.err.println(AnsiColors.RED + "SyntaxError" + where + ": " + message + AnsiColors.BLUE + "\n" + line + " | " + AnsiColors.RESET + source.split("\n")[line-1] + "\n  " + " ".repeat((int)Math.floor(Math.log10(line))+1+col) + AnsiColors.BLUE +  "^-- Here." + AnsiColors.RESET);
        hadError = true;
    }
}