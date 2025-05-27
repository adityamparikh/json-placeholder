package dev.aparikh.jsonplaceholder.model;

import java.io.Serializable;

/**
 * Model class representing a post from the JSONPlaceholder API.
 */
public class Post implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private String title;
    private String body;

    // Default constructor
    public Post() {
    }

    // Constructor with all fields
    public Post(Long id, Long userId, String title, String body) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.body = body;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
