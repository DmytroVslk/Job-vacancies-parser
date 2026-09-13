package api.response;

public record ErrorResponse (boolean success, String message){

    public ErrorResponse (String message){
        this(false, message);
    }
}
