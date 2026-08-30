package response;

public class ErrorResponse {
    private final boolean success;
    private final String message;

    public ErrorResponse(String message) {
        this.success = false;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
