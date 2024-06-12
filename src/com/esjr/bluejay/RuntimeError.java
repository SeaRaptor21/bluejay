package com.esjr.bluejay;

class RuntimeError extends RuntimeException {
    final Token token;

    RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }

    RuntimeError(String message) {
        super(message);
        this.token = null;
    }

    public static class NameError extends RuntimeError {
        NameError(Token token, String message) {
            super(token, message);
        }

        NameError(String message) {
            super(message);
        }
    }

    public static class AttributeError extends RuntimeError {
        AttributeError(Token token, String message) {
            super(token, message);
        }

        AttributeError(String message) {
            super(message);
        }
    }

    public static class TypeError extends RuntimeError {
        TypeError(Token token, String message) {
            super(token, message);
        }

        TypeError(String message) {
            super(message);
        }
    }
}