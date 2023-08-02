package dev.rohitverma882.miunlock.v2.inet;

public class CustomHttpException extends Exception {
    public CustomHttpException(String message) {
        super(message);
    }

    public CustomHttpException(String message, Throwable cause) {
        super(message, cause);
    }
}