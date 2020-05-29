package booking.model;

import java.util.List;

/**
 * IQuery provides a generic interface for performing both selections and
 * commands on a data source. The supported queries are specified as an enum,
 * which is provided as the parameter Q. The type of the list elements returned
 * by a selection method and the type of the data transfer object provided to a
 * command methods are specified as the parameter T.
 *
 * 
 * @param <Q> enum specifying supported queries
 * @param <T> the type of list objects returned by selection methods and the of
 * the argument for command methods
 */
public interface IQuery<Q, T> {

    /**
     * Performs a selection query on the underlying data source. Note that
     * different queries will have different numbers of parameters and types for
     * those parameters.
     *
     * @param q The enum value for the selection
     * @param o The parameters for the selection specified as a vararg list of
     * type Object. No type conversion is required because of boxing.
     * @return a list of data transfer objects of type T
     * @throws QueryException
     */
     public List<T> select(Q q, Object... o) throws QueryException;

    /**
     * Performs a command query (insert, delete, update ... )
     *
     * @param q The enum value for the command
     * @param t The data transfer object containing the data for the command.
     * @return The number of records in the data source that were impacted by
     * the command
     * @throws QueryException
     */
    int command(Q q, T t) throws QueryException;
}
