package dev.aparikh.jsonplaceholder.model;

/**
 * Generic wrapper class for API responses.
 * @param <T> The type of data contained in the response
 */
public class ApiResponse<T> {
    private String status;
    private T data;
    private String message;

    // Default constructor
    public ApiResponse() {
    }

    // Constructor with all fields
    public ApiResponse(String status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    // Static factory methods for common response types
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", data, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", null, message);
    }

    // Getters and setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}