package dev.aparikh.jsonplaceholder.model

import java.io.Serializable

/**
 * Model class representing a post from the JSONPlaceholder API.
 */
data class Post(
    var id: Long? = null,
    var userId: Long? = null,
    var title: String? = null,
    var body: String? = null
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}