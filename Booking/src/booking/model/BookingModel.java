/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package booking.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Class that performs the booking related database operations
 */
public class BookingModel implements IConnect, IQuery<BookingModel.Query, Book> {

    /**
     * The Query enum specifies the queries that are supported by this manager
     * relate to booking details
     */
    public static enum Query {

        INSERT_CUSTOMER, TABLE_IN_USE_SIZE_BY_DAY, INSERT_BOOKING, GET_LATEST_CUSTOMER, FIND_BOOKING_NAME_DATE, GET_CUSTOMER_NAME, GET_CUSTOMER_ID, DELETE_BOOKING, GET_ALL_BOOKINGS, GET_TABLE_USAGE;

    };

    // Database details for the address book being managed
    private static final String URL = "jdbc:derby://localhost:1527/booking"; // url for database connecton
    private static final String USERNAME = "booking";// database user name 
    private static final String PASSWORD = "booking";// database password

    /* 
     * We use enummaps to map queries (enum values) to SQL commands and prepared 
     * statements in a typesafe manner. Hashmaps could be used to the same effect,
     * but they would be less eficient, not that it matters. You could use arrays
     * indexed by enum.ordinal() or by int constants, but you then lose type safety 
     * among other things.
     */
    private EnumMap<Query, String> sqlCommands
            = new EnumMap<>(BookingModel.Query.class);
    private EnumMap<Query, PreparedStatement> statements
            = new EnumMap<>(BookingModel.Query.class);

    // The connection to the Booking
    private Connection connection = null;

    /**
     * Create an instance of the booking model
     */
    public BookingModel() {
        // Specify the queries that are supported
        sqlCommands.put(Query.INSERT_CUSTOMER, "INSERT INTO Customer ( Name,Address,Email,Phone ) VALUES ( ?, ?, ?, ? )");
        sqlCommands.put(Query.TABLE_IN_USE_SIZE_BY_DAY, "select SUM(NOOFTABLE)from BOOKING.BOOK WHERE CREATIONDATE=?");
        sqlCommands.put(Query.GET_LATEST_CUSTOMER, "select  ID,NAME,EMAIL,ADDRESS,PHONE from BOOKING.CUSTOMER ORDER BY ID DESC fetch first 1 rows only");
        sqlCommands.put(Query.INSERT_BOOKING, "INSERT INTO BOOK (CREATIONDATE,PEOPLE,NOOFTABLE,CUSTOMERID,STATUS ) VALUES ( ?, ?, ?, ? ,?)");
        sqlCommands.put(Query.FIND_BOOKING_NAME_DATE, "select  ID,CREATIONDATE,PEOPLE,NOOFTABLE,CUSTOMERID,STATUS from BOOKING.BOOK  WHERE  CREATIONDATE=?  AND CUSTOMERID=? fetch first 1 rows only");
        sqlCommands.put(Query.GET_CUSTOMER_NAME, "select ID,NAME,EMAIL,ADDRESS,PHONE from BOOKING.CUSTOMER WHERE NAME=?  fetch first 1 rows only");
        sqlCommands.put(Query.GET_CUSTOMER_ID, "select ID,NAME,EMAIL,ADDRESS,PHONE from BOOKING.CUSTOMER WHERE id=?  fetch first 1 rows only");
        sqlCommands.put(Query.DELETE_BOOKING, "DELETE FROM BOOKING.BOOK WHERE ID = ?");
        sqlCommands.put(Query.GET_ALL_BOOKINGS, "select ID,CREATIONDATE,PEOPLE,NOOFTABLE,CUSTOMERID,STATUS from BOOKING.BOOK WHERE STATUS='booked' AND  CREATIONDATE  >=? AND CREATIONDATE <=?");
        sqlCommands.put(Query.GET_TABLE_USAGE, "select CREATIONDATE ,SUM(NOOFTABLE) AS TABLEUSE  from BOOKING.BOOK WHERE STATUS='booked' AND  CREATIONDATE  >=? AND CREATIONDATE <=? GROUP BY CREATIONDATE");

    }

    // IConnct implementation
    /**
     * Connect to the booking
     *
     * @throws ConnectionException
     */
    @Override
    public void connect() throws ConnectionException {
        // Connect to the address book database
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            throw new ConnectionException("Unable to open data source", e);
        }
    }

    /**
     * Perform any initialization that is needed before queries can be
     * performed.
     *
     * @throws ConnectionException
     */
    @Override
    public void initialise() throws ConnectionException {
        // Create prepared statements for each query
        try {
            for (Query q : Query.values()) {
                statements.put(q, connection.prepareStatement(sqlCommands.get(q)));
            }
        } catch (SQLException e) {
            throw new ConnectionException("Unable to initialise data source", e);
        }
    }

    /**
     * Disconnect from the address book
     *
     * @throws ConnectionException
     */
    @Override
    public void disconnect() throws ConnectionException {
        // Close the connection 
        // In Java 9, we can do try( connection ), but I am running Java 8
        try (Connection c = connection) {
            // connection is closed automatically with try with resources
            // close prepared statements first
            for (Query q : Query.values()) {
                statements.get(q).close();
            }
        } catch (SQLException e) {
            throw new ConnectionException("Unable to close data source", e);
        }
    }

    // IQuery implementation
    /**
     * Perform a selection on the booking
     *
     * @param q the selection as specified in the Query enum
     * @param p parameters for the query specified as a varags of type Object
     * @return a List of Book objects that match query specification
     * @throws QueryException
     */
    @Override
    public List<Book> select(Query q, Object... p) throws QueryException {
        switch (q) {
            case FIND_BOOKING_NAME_DATE:
                return findBookingByNameDate(p);
            case GET_ALL_BOOKINGS:
                return getAllBookings(p);
            case GET_TABLE_USAGE:
                return getTableUsage(p);
        }
        return null;
    }

    /**
     * Perform a command (insert, delete, update ... ) on the booking
     *
     * @param q the command as specified in the Query enum
     * @param p a Book object containing the data for the command
     * @return the number of records in the booking impacted on by the command
     * @throws QueryException
     */
    @Override
    public int command(Query q, Book b) throws QueryException {
        switch (q) {
            case INSERT_CUSTOMER:
                return addCustomer(b.getCustomer());
            case TABLE_IN_USE_SIZE_BY_DAY:
                return getTableSizeInUseByDate(b);
            case INSERT_BOOKING:
                return createBooking(b);
            case DELETE_BOOKING:
                return deleteBooking(b);
        }
        // Should never happen
        return -1;
    }

    /**
     * Method that adds customer
     *
     * @param c customer object
     * @return the number of records in the booking impacted on by the command
     * @throws QueryException
     */
    private int addCustomer(Customer c) throws QueryException {
//            Look up prepared statement
        PreparedStatement ps = statements.get(Query.INSERT_CUSTOMER);
        // insert customer attributes into prepared statement
        try {
            System.out.println(c);
            ps.setString(1, c.getName());
            ps.setString(2, c.getAddress());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getPhone());
            System.out.println(ps.toString());
            // insert the new entry; returns # of rows updated
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw (new QueryException("Unable to perform insert command", e));
        }
    }

    /**
     * Method that get gets the table size in use by date
     *
     * @param b book object
     * @return the number table size in use command
     * @throws QueryException
     */

    private int getTableSizeInUseByDate(Book b) {
        int size = -1;
        // get query
        PreparedStatement ps = statements.get(Query.TABLE_IN_USE_SIZE_BY_DAY);

        try {
            System.out.println(b);
            ps.setString(1, b.getCreationDate().toString());
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                System.out.println("size:" + resultSet.getInt(1));
                size = resultSet.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(BookingModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return size;
    }

    /**
     * Method that creates booking
     *
     * @param b book object
     * @return the number records inserted command
     * @throws QueryException
     */
    private int createBooking(Book b) {
        // get latest customer
        Customer c = getLatestCustomer();
        //assing customer
        b.setCustomer(c);
        //insert booking

        PreparedStatement ps = statements.get(Query.INSERT_BOOKING);
        // insert person attributes into prepared statement
        try {
            System.out.println(c);
            ps.setString(1, b.getCreationDate().toString());
            ps.setInt(2, b.getNoOfPeople());
            ps.setInt(3, b.getNoOfTable());
            ps.setInt(4, b.getCustomer().getId());
            ps.setString(5, b.getBooking_status());

            System.out.println(ps.toString());
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Method that get latest customer
     *
     * @return latest entered customer command
     * @throws QueryException
     */
    private Customer getLatestCustomer() {
        // get customer query
        PreparedStatement ps = statements.get(Query.GET_LATEST_CUSTOMER);
        Customer c = new Customer();
        try {

            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                c.setId(resultSet.getInt(1));
                c.setName(resultSet.getString(2));
                c.setEmail(resultSet.getString(3));
                c.setAddress(resultSet.getString(4));
                c.setPhone(resultSet.getString(5));
            }
        } catch (SQLException ex) {
            Logger.getLogger(BookingModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("latest customer:" + c);
        return c;
    }

    /**
     * Method that find the booking by name
     *
     * @param p customer name and date object
     * @return list of the booking records command
     * @throws QueryException
     */
    private List<Book> findBookingByNameDate(Object[] p) {
        // get customer by name
        Customer customer = findCustomerByName(p[0].toString());
        List<Book> bookList = new ArrayList<Book>();
        // get booking query
        PreparedStatement ps = statements.get(Query.FIND_BOOKING_NAME_DATE);
        java.util.Date d = (java.util.Date) p[1];
        try {
            ps.setDate(1, new java.sql.Date(d.getTime()));
            ps.setInt(2, customer.getId());
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Book b = new Book();
                b.setId(resultSet.getInt(1));
                b.setCreationDate(resultSet.getDate(2));
                b.setNoOfPeople(resultSet.getInt(3));
                b.setNoOfTable(resultSet.getInt(4));
                b.setBooking_status(resultSet.getString(5));
                b.setCustomer(customer);
                bookList.add(b);
            }
        } catch (SQLException ex) {
            Logger.getLogger(BookingModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return bookList;

    }

    /**
     * Method that return customer by name
     *
     * @param customername customer name object
     * @return customer command
     * @throws QueryException
     */
    private Customer findCustomerByName(String customerName) {
//  get customer query
        PreparedStatement ps = statements.get(Query.GET_CUSTOMER_NAME);
        Customer c = new Customer();
        try {
            ps.setString(1, customerName);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                c.setId(resultSet.getInt(1));
                c.setName(resultSet.getString(2));
                c.setEmail(resultSet.getString(3));
                c.setAddress(resultSet.getString(4));
                c.setPhone(resultSet.getString(5));
            }
        } catch (SQLException ex) {
            Logger.getLogger(BookingModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return c;
    }
    /**
     *Method that performs deletion of booking
     *
     * @param b booking id
     * @return  whether the booking is deleted  or not
     * 
     */
    private int deleteBooking(Book b) {

        PreparedStatement ps = statements.get(Query.DELETE_BOOKING);
        try {
            ps.setInt(1, b.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    /**
     *Method that returns all booking
     *
     * @param p customer id
     * @return  list of all booking
     * @throws QueryException
     */
    private List<Book> getAllBookings(Object[] p) {
        PreparedStatement ps = statements.get(Query.GET_ALL_BOOKINGS);
        List<Book> bookList = new ArrayList<Book>();
        try {
            ps.setDate(1, (java.sql.Date) p[0]);
            ps.setDate(2, (java.sql.Date) p[1]);

            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Book b = new Book();
                b.setId(resultSet.getInt(1));
                b.setCreationDate(resultSet.getDate(2));
                b.setNoOfPeople(resultSet.getInt(3));
                b.setNoOfTable(resultSet.getInt(4));
                b.setCustomer(getCustomerByID(resultSet.getInt(5)));
                b.setBooking_status(resultSet.getString(6));
                bookList.add(b);
            }
        } catch (SQLException ex) {
            Logger.getLogger(BookingModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return bookList;
    }
      /**
     * Method that returns customer by id
     *
     * @param id customer id
     * @return customer
     * @throws QueryException
     */
    private Customer getCustomerByID(int id) {
        // get customer query
        PreparedStatement ps = statements.get(Query.GET_CUSTOMER_ID);
        Customer c = new Customer();
        try {
            ps.setInt(1, id);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                c.setId(resultSet.getInt(1));
                c.setName(resultSet.getString(2));
                c.setEmail(resultSet.getString(3));
                c.setAddress(resultSet.getString(4));
                c.setPhone(resultSet.getString(5));
            }
        } catch (SQLException ex) {
            Logger.getLogger(BookingModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return c;

    }

    /**
     * Method that returns the list of table usage
     *
     * @param p start and end date
     * @return list of customer
     * @throws QueryException
     */
    private List<Book> getTableUsage(Object[] p) {
        //get table usage query
        PreparedStatement ps = statements.get(Query.GET_TABLE_USAGE);
        List<Book> bookList = new ArrayList<Book>();
        try {
            ps.setDate(1, (java.sql.Date) p[0]);
            ps.setDate(2, (java.sql.Date) p[1]);

            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Book b = new Book();
                b.setCreationDate(resultSet.getDate(1));
                b.setNoOfTable(resultSet.getInt(2));
                bookList.add(b);
            }
        } catch (SQLException ex) {
            Logger.getLogger(BookingModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        // return booking list
        return bookList;
    }

}
