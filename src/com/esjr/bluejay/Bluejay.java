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

    public static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) System.exit(65);
    }

    public static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        while (true) { 
            System.out.print(">>> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String src) {
        source = src;
        Scanner scanner = new Scanner(src);
        List<Token> tokens = scanner.scanTokens();

        if (hadError) return;

        for (Token t : tokens) {
            System.out.println(t.toString());
        }

        Parser parser = new Parser(tokens);
        List<Stmt> stmts = parser.parse();

        if (hadError) return;
    }

    static void error(int pos, String message) {
        report(pos, "", message);
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.pos, " at end", message);
        } else {
            report(token.pos, " at '" + token.lexeme + "'", message);
        }
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
        System.err.println(AnsiColors.RED + "Error" + where + ": " + message + AnsiColors.BLUE + "\n" + line + " | " + AnsiColors.RESET + source.split("\n")[line-1] + "\n   " + " ".repeat(col) + AnsiColors.BLUE +  "^-- Here." + AnsiColors.RESET);
        hadError = true;
    }
}