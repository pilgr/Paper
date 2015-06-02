package io.paperdb;

public class PaperDbException extends RuntimeException {
    public PaperDbException(String detailMessage) {
        super(detailMessage);
    }

    public PaperDbException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
