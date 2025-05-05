package dev.aparikh.jsonplaceholder.dto;

import dev.aparikh.jsonplaceholder.model.Post;

/**
 * Data Transfer Object for Post to avoid classloader issues.
 */
public class PostDto {
    private final Long id;
    private final Long userId;
    private final String title;
    private final String body;

    /**
     * Constructor to create a PostDto from a Post.
     *
     * @param post The Post object to convert
     */
    public PostDto(Post post) {
        this.id = post.getId();
        this.userId = post.getUserId();
        this.title = post.getTitle();
        this.body = post.getBody();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }
}