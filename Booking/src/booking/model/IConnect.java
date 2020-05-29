package booking.model;


/**
 * IConnect provides methods for managing a connection to a data source.
 *
 * 
 */
public interface IConnect {

    /**
     * Establish a connection to the data source being managed
     * @throws ConnectionException
     */
    public void connect() throws ConnectionException;

    /**
     * Initialise the connection being managed. If the data source is a
     * database, queries are constructed at this point
     * @throws ConnectionException
     */
    public void initialise() throws ConnectionException;

    /**
     * Disestablish the connection being managed
     * @throws ConnectionException
     */
    public void disconnect() throws ConnectionException;
}
