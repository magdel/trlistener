package ru.netradar.web.web.domain;


public class ErrorMessage {

    private String techMessage;
    private long errorId;

    public String getTechMessage() {
        return techMessage;
    }

    public void setTechMessage(String techMessage) {
        this.techMessage = techMessage;
    }

    public long getErrorId() {
        return errorId;
    }

    public void setErrorId(long errorId) {
        this.errorId = errorId;
    }
}
