package client.config;

public class RequestFailureException extends Exception{

    public RequestFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestFailureException(String message) {
        super(message);
    }
}
