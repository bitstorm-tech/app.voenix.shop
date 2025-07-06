package com.jotoai.voenix.shop.prompts.dto;

import jakarta.validation.constraints.Size;

import java.util.Objects;

public class UpdatePromptRequest {
    
    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;
    
    private String content;

    // Constructors
    public UpdatePromptRequest() {
    }

    public UpdatePromptRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // Builder pattern
    public static UpdatePromptRequestBuilder builder() {
        return new UpdatePromptRequestBuilder();
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdatePromptRequest that = (UpdatePromptRequest) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, content);
    }

    // toString
    @Override
    public String toString() {
        return "UpdatePromptRequest{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

    // Builder class
    public static class UpdatePromptRequestBuilder {
        private String title;
        private String content;

        UpdatePromptRequestBuilder() {
        }

        public UpdatePromptRequestBuilder title(String title) {
            this.title = title;
            return this;
        }

        public UpdatePromptRequestBuilder content(String content) {
            this.content = content;
            return this;
        }

        public UpdatePromptRequest build() {
            return new UpdatePromptRequest(title, content);
        }
    }
}