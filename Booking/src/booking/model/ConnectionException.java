package booking.model;


/**
 *
 * Class for the connection exception
 */
public class ConnectionException extends RuntimeException {
    
    public ConnectionException( String message, Throwable cause ) {
        super(message,cause);
    }
    
}
