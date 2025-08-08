package com.example.Impression.exception;

public class AnnonceException extends RuntimeException {

    private final String errorCode;

    public AnnonceException(String message) {
        super(message);
        this.errorCode = "ANNONCE_ERROR";
    }

    public AnnonceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AnnonceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "ANNONCE_ERROR";
    }

    public String getErrorCode() {
        return errorCode;
    }

    public static class AnnonceNotFoundException extends AnnonceException {
        public AnnonceNotFoundException(String message) {
            super(message, "ANNONCE_NOT_FOUND");
        }
    }

    public static class AnnonceUnauthorizedException extends AnnonceException {
        public AnnonceUnauthorizedException(String message) {
            super(message, "ANNONCE_UNAUTHORIZED");
        }
    }

    public static class AnnonceValidationException extends AnnonceException {
        public AnnonceValidationException(String message) {
            super(message, "ANNONCE_VALIDATION_ERROR");
        }
    }
}