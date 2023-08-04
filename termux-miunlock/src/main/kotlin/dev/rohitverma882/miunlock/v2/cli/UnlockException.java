package dev.rohitverma882.miunlock.v2.cli;

import java.util.Objects;

import dev.rohitverma882.miunlock.v2.inet.CustomHttpException;
import dev.rohitverma882.miunlock.v2.xiaomi.XiaomiProcedureException;

public class UnlockException extends Exception {
    public static final UnlockException ABORT_EXCEPTION = new UnlockException("The unlocking was aborted by the user", Code.ABORTED);
    private final Code code;
    private boolean waitCommand;
    private Object[] params;

    public UnlockException(InterruptedException e) {
        this("InterruptedException: " + e.getMessage(), Code.INTERNAL_ERROR, e);
    }

    public UnlockException(XiaomiProcedureException exeption) {
        this("Xiaomi procedure failed: " + exeption.getMessage(), Code.XIAOMI_EXCEPTION, exeption);
    }

    public UnlockException(CustomHttpException exception) {
        this("Internet connection error: " + exception.getMessage(), Code.CONNECTION_ERROR, exception);
    }

    public UnlockException(String message, Code code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public UnlockException(String message, Code code, String cause) {
        this(message, code, new Throwable(cause));
    }

    public UnlockException(String message, Code code) {
        this(message, code, (Throwable) null);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " - " + this.getCode() + " - " + this.getMessage();
    }

    public boolean isWaitCommand() {
        return waitCommand;
    }

    public Object[] getParams() {
        return params;
    }

    public Code getCode() {
        return code;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof UnlockException o)) {
            return false;
        }
        if (!Objects.equals(this.getCode(), o.getCode())) {
            return false;
        }
        return Objects.equals(this.getMessage(), o.getMessage());
    }

    public enum Code {
        INTERNAL_ERROR, INFO_RETRIVE_FAILED, XIAOMI_EXCEPTION, CONNECTION_ERROR, ABORTED, IO_ERROR, HASH_FAILED, UNLOCK_ERROR, WAIT_DEVICE_TIMEOUT;
        private final String key;

        Code(String key) {
            this.key = key;
        }

        Code() {
            this.key = this.name().toLowerCase();
        }

        @Override
        public String toString() {
            return key;
        }
    }
}
