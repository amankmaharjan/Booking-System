package booking.model;


/**
 *
 * Class for the query exception
 */
public class QueryException extends Exception {

    public QueryException(String message,Throwable cause) {
        super(message,cause);
    }
}
