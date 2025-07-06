package com.jotoai.voenix.shop.prompts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;

public class CreatePromptRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;
    
    private String content;

    // Constructors
    public CreatePromptRequest() {
    }

    public CreatePromptRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // Builder pattern
    public static CreatePromptRequestBuilder builder() {
        return new CreatePromptRequestBuilder();
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
        CreatePromptRequest that = (CreatePromptRequest) o;
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
        return "CreatePromptRequest{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

    // Builder class
    public static class CreatePromptRequestBuilder {
        private String title;
        private String content;

        CreatePromptRequestBuilder() {
        }

        public CreatePromptRequestBuilder title(String title) {
            this.title = title;
            return this;
        }

        public CreatePromptRequestBuilder content(String content) {
            this.content = content;
            return this;
        }

        public CreatePromptRequest build() {
            return new CreatePromptRequest(title, content);
        }
    }
}