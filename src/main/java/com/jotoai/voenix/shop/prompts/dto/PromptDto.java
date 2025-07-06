package com.jotoai.voenix.shop.prompts.dto;

import java.time.LocalDateTime;
import java.util.Objects;

public class PromptDto {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public PromptDto() {
    }

    public PromptDto(Long id, String title, String content, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Builder pattern
    public static PromptDtoBuilder builder() {
        return new PromptDtoBuilder();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromptDto promptDto = (PromptDto) o;
        return Objects.equals(id, promptDto.id) &&
                Objects.equals(title, promptDto.title) &&
                Objects.equals(content, promptDto.content) &&
                Objects.equals(createdAt, promptDto.createdAt) &&
                Objects.equals(updatedAt, promptDto.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, content, createdAt, updatedAt);
    }

    // toString
    @Override
    public String toString() {
        return "PromptDto{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    // Builder class
    public static class PromptDtoBuilder {
        private Long id;
        private String title;
        private String content;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        PromptDtoBuilder() {
        }

        public PromptDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public PromptDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public PromptDtoBuilder content(String content) {
            this.content = content;
            return this;
        }

        public PromptDtoBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PromptDtoBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public PromptDto build() {
            return new PromptDto(id, title, content, createdAt, updatedAt);
        }
    }
}