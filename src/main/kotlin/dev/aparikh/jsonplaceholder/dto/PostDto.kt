package dev.aparikh.jsonplaceholder.dto

import dev.aparikh.jsonplaceholder.model.Post

/**
 * Data Transfer Object for Post to avoid classloader issues.
 */
data class PostDto(
    val id: Long?,
    val userId: Long?,
    val title: String?,
    val body: String?
) {
    /**
     * Constructor to create a PostDto from a Post.
     *
     * @param post The Post object to convert
     */
    constructor(post: Post) : this(
        id = post.id,
        userId = post.userId,
        title = post.title,
        body = post.body
    )
}