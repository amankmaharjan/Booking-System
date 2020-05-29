package booking.view;

import java.util.List;

/**
 * IView provides a generic interface for the display of browsable records
 * @param <T> the type for the record that is to be displayed
 *  * @param <U> the type of table usage

 */
public interface IView<T,U> {
     // IQuery implementation
    /**
     * Perform a display the records 
     *
     * @param T  record
     * @return a List of Book objects that match query specification
     * @throws QueryException
     */
    void displayRecord( T r);
     // IQuery implementation
    /**
     * Perform a display on the message
     *
     * @param m  display the message
   
     */
    void displayMessage( String m );
     // IQuery implementation
    /**
     * Perform a browsing button enable and diable
     *
     * */
    void setBrowsing( boolean b );
     // IQuery implementation
    /**
     * Perform a displaying of error
     * @param e the selection as specified in the Query enum

     */
    void displayError( String e );
     // IQuery implementation
    /**
     * Perform a selection on the booking
     *
     * @param u list of the  booking
     */
    void displayTableUsage(List<U> u);
     // IQuery implementation
    /**
     * Perform a after check availability which button to enable
     * @param b boolean
     * @param size table size
     * 
     */
    void afterCheckAvailability(boolean b,int size);    /**
     * Perform a resetting on the booking UI
     */
    void reset();
    /**
     * 
     * @param b boolean
     */
    void setCancelButton(boolean b);
}

