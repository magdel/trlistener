package ru.netradar.web.web.domain;

public class CommonResponse<T> {

    private ErrorMessage error;
    private T result;

    public CommonResponse() {
    }

    public CommonResponse(T result) {
        this.result = result;
    }

    public ErrorMessage getError() {
        return error;
    }

    public void setError(ErrorMessage error) {
        this.error = error;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}



