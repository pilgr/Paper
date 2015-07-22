package io.paperdb;

public class NonExistedValueException extends PaperDbException {

    public NonExistedValueException(String detailMessage) {
        super(detailMessage);
    }

    public NonExistedValueException(Throwable throwable) {
        super(throwable);
    }

    public NonExistedValueException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
