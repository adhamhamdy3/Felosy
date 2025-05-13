package felosy.authentication;

public class AuthenticationResponse {
    private final AuthenticationResult result;
    private final String message;
    private final User user;

    public AuthenticationResponse(AuthenticationResult result, String message, User user) {
        this.result = result;
        this.message = message;
        this.user = user;
    }

    public AuthenticationResult getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }
}