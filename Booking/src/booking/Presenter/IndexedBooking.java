package booking.Presenter;

import booking.model.Book;




/**
 * IndexedBook provides a wrapper for a Book record that provides
 * information relating to its position in the browsing context maintained by
 * BookPresenter.
 *
 * @author Dennis
 */
public class IndexedBooking {

    private final Book p;
    private final int i;
    private final int n;

    /**
     * Create a wrapper for a Book object
     *
     * @param p the object to be wrapped
     * @param i the position of the object in the browsing context
     * @param n the number of objects in the browsing context
     */
    public IndexedBooking(Book p, int i, int n) {
        this.p = p;
        this.i = i;
        this.n = n;
    }

    /**
     * @return the person object being wrapped
     */
    public Book getBook() {
        return p;
    }

    /**
     * @return the position of the wrapped person object in the browsing context
     */
    public int getIndex() {
        return i;
    }

    /**
     * @return the number of objects in the browsing context
     */
    public int getSize() {
        return n;
    }
    
}
