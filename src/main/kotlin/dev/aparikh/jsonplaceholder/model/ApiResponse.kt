package dev.aparikh.jsonplaceholder.model

/**
 * Generic wrapper class for API responses.
 * @param T The type of data contained in the response
 */
data class ApiResponse<T>(
    var status: String? = null,
    var data: T? = null,
    var message: String? = null
) {
    companion object {
        // Static factory methods for common response types
        @JvmStatic
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse("success", data, null)
        }

        @JvmStatic
        fun <T> error(message: String): ApiResponse<T> {
            return ApiResponse("error", null, message)
        }
    }
}