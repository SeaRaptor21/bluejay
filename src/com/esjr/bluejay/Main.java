package com.esjr.bluejay;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length == 1) {
            try {
                Bluejay.runFile(args[0]);
            } catch (IOException e) {
                System.out.println("No such file or directory.");
            }
        } else if (args.length == 0) {
            try {
                Bluejay.runPrompt();
            } catch (IOException e) {
                System.out.println("An error occured.");
            }
        } else {
            System.out.println("Someone should maybe make a usage message.");
        }
    }
}