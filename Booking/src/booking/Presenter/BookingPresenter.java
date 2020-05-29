/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package booking.Presenter;

import booking.model.Book;
import booking.model.BookingModel;
import booking.model.Customer;
import booking.model.IConnect;
import booking.model.IQuery;
import booking.model.QueryException;
import booking.view.IView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// Presenter class for booking
public class BookingPresenter {

    // Field declarations
    IView view; // Interface for binding view
    IQuery queries; // Interface for query
    IConnect connector; // connector for datbase
    static int MAXPEOPLE = 10; // maximum no people allowed in the booking
    static int MAX_TABLE_SIZE = 20;// maximum no of table in the booking
    static String BOOKED = "booked";// booking status
    static String QUEUED = "queued";// booking status constant
    ViewModel viewModel;

    public BookingPresenter(IQuery iq, IConnect ic) {
        // intialise model access
        queries = iq;
        connector = ic;
        // initialise the browsing context
        viewModel = new ViewModel();

    }

    /* Design note
     * ViewModel functionality was originally part of the presenter class, as in the 
     * previous version of AddressBook. However, I refactored it into its own 
     * (inner) class, as I was thinking about  the future and how this case study would 
     * fit with JavaFX rather than Swing. As to why its an inner class - the rationale
     * is that its just a helper class for the presenter.
     */
    private static class ViewModel {

        List<Book> model;
        Book current;
        int index;
        int n;

        ViewModel() {
        }

        void set(List<Book> m) {
            model = m;
            index = 0;
            n = model.size();
            current = model.get(index);
        }

        IndexedBooking previous() {
            if (--index < 0) {
                index = n - 1;
            }
            return new IndexedBooking(model.get(index), index + 1, n);
        }

        IndexedBooking next() {
            if (++index > n - 1) {
                index = 0;
            }
            return new IndexedBooking(model.get(index), index + 1, n);
        }

        IndexedBooking current() {
            return new IndexedBooking(model.get(index), index + 1, n);
        }
    }

    /**
     * Set the view dependency for the presenter
     *
     * @param iv the view
     */
    public void bind(IView iv) {
        view = iv;
    }

    /**
     * method that adds customer
     *
     * @param name customer name
     * @param address customer address.
     * @param email customer email
     * @param phone customer phone
     */
    public void addCustomer(String name, String address, String email, String phone) {
        // checking the empty string
        if (name.equals("") || address.equals("") || email.equals("") || phone.equals("")) {
            String error = "Name or address or email or phone is missing";
            view.displayError(error);
            throw new IllegalArgumentException(error);
        }
        try {
            // The id field  will be created by the model
            Customer c = new Customer(-1, name, address, email, phone);
            Book b = new Book();
            b.setCustomer(c);
            // executing the insert customer command
            int result = queries.command(BookingModel.Query.INSERT_CUSTOMER, b);
            if (result == 1) {
                view.displayMessage("Customer added");

            } else {
                view.displayMessage("Customer not added");
            }

            view.setBrowsing(false);
        } catch (QueryException e) {
            view.displayError(e.getCause() + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * method that check the booking is available for specific date and people
     *
     * @param date date of the booking
     * @param noOfPeople no of people in booking
     */
    public void checkAvailability(Date date, String noOfPeople) {
        // checking the empty string
        if (date == null || noOfPeople.equals("")) {
            String error = "Date or  people size is missing";
            view.displayError(error);
            throw new IllegalArgumentException(error);
        }
        // check no of people
        checkPeopleSize(noOfPeople);

        // table size needed
        int tableSizeNeeded = 0;
        try {
            //
            Book b = new Book();
            b.setCreationDate(new java.sql.Date(date.getTime()));
            // get booked table size
            int bookedTableSize = queries.command(BookingModel.Query.TABLE_IN_USE_SIZE_BY_DAY, b);
            int availableSize = MAX_TABLE_SIZE - bookedTableSize;
            int peopleSize = Integer.parseInt(noOfPeople);

            //calculate total table for people
            if (peopleSize >= 1 && peopleSize <= 4) {
                tableSizeNeeded = 1;
            }
            if (peopleSize >= 5 && peopleSize <= 6) {
                tableSizeNeeded = 2;
            }
            if (peopleSize >= 7 && peopleSize <= 8) {
                tableSizeNeeded = 3;
            }
            if (peopleSize >= 9 && peopleSize <= 10) {
                tableSizeNeeded = 4;
            }
            if (peopleSize < 1 || peopleSize > 10) {
                tableSizeNeeded = 21;
            }

            // check if it fits or not
            if (tableSizeNeeded <= availableSize) {
                String message = "Congratulations!! It is available:table size:" + tableSizeNeeded;
                this.view.displayMessage(message);
                this.view.afterCheckAvailability(true, tableSizeNeeded);
            } else {
                String message = "Table unavailable:People size exceed the available table:" + availableSize;
                this.view.displayError(message);
                this.view.afterCheckAvailability(false, 0);
                System.out.println(message);
            }

        } catch (QueryException | NumberFormatException e) {
            e.printStackTrace();

        }
    }

    /**
     * Method that creates booking
     *
     * @param date date of the booking
     * @param peopleSize no of people in booking
     * @param tableSize size of the table
     */
    public void createBooking(Date date, String peoplSize, String tableSize) {
        // checking the empty string
        if (date == null || peoplSize.equals("") || tableSize.equals("")) {
            String error = "Date or  people size or table size  is missing";
            view.displayError(error);
            throw new IllegalArgumentException(error);
        }
        // check max people allowance
        checkPeopleSize(peoplSize);
        //CREATE BOOKING
        Book b = new Book();
        b.setBooking_status(BOOKED);
        b.setCreationDate(new java.sql.Date(date.getTime()));
        b.setNoOfPeople(Integer.parseInt(peoplSize));
        b.setNoOfTable(Integer.parseInt(tableSize));
        try {
            // INSERT BOOKING
            int result = queries.command(BookingModel.Query.INSERT_BOOKING, b);
            if (result == 1) {
                view.displayMessage("Booking added sucessfully");
                view.afterCheckAvailability(false, 0);

            } else {
                view.displayMessage("Booking not added");
                view.afterCheckAvailability(false, 0);
            }
        } catch (QueryException ex) {
            Logger.getLogger(BookingPresenter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Method that check s the people size from the gui
     *
     * @param noOfPeople no of people
     */
    public void checkPeopleSize(String noOfPeople) {
        // check max people allowance
        if (Integer.parseInt(noOfPeople) > MAXPEOPLE) {
            String error = "People size must be less than" + (MAXPEOPLE+1);
            this.view.displayError(error);
            this.view.afterCheckAvailability(false, 0);
            throw new IllegalArgumentException(error);

        }
    }
    /**
     * Method that sends  to queue
     *  @param date  date of the booking
     *  @param tableSize  size of the table
     * @param noOfPeople no of people
     */

    public void sendToQueue(Date date, String peopleSize, String tableSize) {
        // checking the empty string
        if (date == null || peopleSize.equals("") || tableSize.equals("")) {
            String error = "Date or  people size or table size  is missing";
            view.displayError(error);
            throw new IllegalArgumentException(error);
        }
        // check people size
        checkPeopleSize(peopleSize);
        Book b = new Book();
        b.setBooking_status(QUEUED);
        b.setCreationDate(new java.sql.Date(date.getTime()));
        b.setNoOfPeople(Integer.parseInt(peopleSize));
        b.setNoOfTable(Integer.parseInt(tableSize));
        try {
            // Insert the booking
            int result = queries.command(BookingModel.Query.INSERT_BOOKING, b);
            if (result == 1) {
                view.displayMessage("Booking send to Waiting list sucessfully");
                view.afterCheckAvailability(false, 0);

            } else {
                view.displayError("Booking not added to Waiting list");
                view.afterCheckAvailability(false, 0);

            }
        } catch (QueryException ex) {
            Logger.getLogger(BookingPresenter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

      /**
     *  Method that find booking by name and date
     *  @param date  date of the booking
     *  @param tableSize  size of the table
     * @param noOfPeople no of people
     */

    // 
    public void findBookingByNameDate(String customerName, Date date) {
        // checking empty fields
        if (customerName.equals("") || date == null) {
            String error = "Customer Name or  date is missing";
            view.displayError(error);
            throw new IllegalArgumentException(error);
        }
        Book b = new Book();
        Customer c = new Customer();
        c.setName(customerName);
        b.setCustomer(c);
        b.setCreationDate(new java.sql.Date(date.getTime()));
        try {
            // get booking list
            List<Book> bookList = queries.select(BookingModel.Query.FIND_BOOKING_NAME_DATE, customerName, date);
            this.displayCurrentRecord(bookList);
            if (bookList.size() > 0) {
                this.view.displayMessage("Record found:1");
                this.view.setCancelButton(true);
            } else {
                this.view.setCancelButton(false);
            }

        } catch (QueryException ex) {
            Logger.getLogger(BookingPresenter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
      /**
     * Method that deletes the booking
     *  @param id  date of the booking
     */
    public void deleteBooking(String id) {
        if (id.equals("")) {
            String error = "Id is missing";
            view.displayError(error);
            throw new IllegalArgumentException(error);
        }
        Book b = new Book();
        b.setId(Integer.parseInt(id));
        try {
            // Cancel the booking
            int result = queries.command(BookingModel.Query.DELETE_BOOKING, b);
            if (result > 0) {
                this.view.displayMessage("Booking cancelled sucessfully");
                this.view.reset();
                this.view.setCancelButton(false);
            }

        } catch (QueryException ex) {
            Logger.getLogger(BookingPresenter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

      /**
     * Method that gets all booking based on date
     *  @param startingdate  date of the booking
     */
    public void getAllBooking(Date startingdate) {
        if (startingdate == null) {
            String error = "Starting date is missing";
            view.displayError(error);
            throw new IllegalArgumentException(error);
        }
        java.sql.Date startDate = new java.sql.Date(startingdate.getTime());
        // get end date
        java.sql.Date endDate = new java.sql.Date(addDays(startingdate, 7).getTime());

        try { // Get all bookings
            List<Book> bookList = queries.select(BookingModel.Query.GET_ALL_BOOKINGS, startDate, endDate);
            this.displayCurrentRecord(bookList);
        } catch (QueryException ex) {
            Logger.getLogger(BookingPresenter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
     /**
     * Method that list the current record  
     *  @param results  date of current records
     */
    private void displayCurrentRecord(List results) {
        if (results.isEmpty()) {
            view.displayMessage("No records found");
            view.setBrowsing(false);
            return;
        }
        viewModel.set(results);
        view.displayRecord(viewModel.current());
        view.setBrowsing(true);
    }

    /**
     * For the records being browsed, make the previous record the current
     * record and display it, together with its position in the browsing
     * context. If the current record is the first record, the last record will
     * become the current record.
     */
    public void showPrevious() {
        view.displayRecord(viewModel.previous());
    }

    /**
     * For the records being browsed, make the next record the current record
     * and display it, together with its position in the browsing context. If
     * the current record is the last record, the first record will become the
     * current record.
     */
    public void showNext() {
        view.displayRecord(viewModel.next());
    }

    // get date by adding the date
    public Date addDays(Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, days);
        return new Date(c.getTimeInMillis());
    }
  /**
     * Method that finds the table usage 
     *  @param starting  date of booking
     */
    public void getTableUsage(Date startingdate) {
        java.sql.Date startDate = new java.sql.Date(startingdate.getTime());
        // get 7 days end date
        java.sql.Date endDate = new java.sql.Date(addDays(startingdate, 7).getTime());

        try {
            List<Book> bookList = queries.select(BookingModel.Query.GET_TABLE_USAGE, startDate, endDate);
            List<Book> newBookList = new ArrayList<>();
            //fill list with date;
            for (int i = 0; i < 7; i++) {
                Book b = new Book();
                b.setCreationDate(new java.sql.Date(addDays(startingdate, i).getTime()));
                for (Book book : bookList) {
                    if (b.getCreationDate().equals(book.getCreationDate())) {
                        b.setNoOfTable(book.getNoOfTable());
                        break;
                    } else {
                        b.setNoOfTable(0);
                    }
                }
                newBookList.add(b);
            }
            this.view.displayTableUsage(newBookList);
        } catch (QueryException ex) {
            Logger.getLogger(BookingPresenter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
