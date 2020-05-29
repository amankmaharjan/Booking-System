package booking.model;

import java.sql.Date;



//Booking class for the storing booked records
public class Book {
    // Field declaration for id, no of people,customer, no of Table, booking satus, creationDate
    private int id; // booking identifier
    private int noOfPeople; // no of people in booking
    private Customer customer;// customer of booking
    private int noOfTable; // no of tabl in booking
    private String booking_status; // booking status either booked or queued
    private Date creationDate; // booking date

    //no argument constructor
    public Book() {
    }

  // get no of People
    public int getNoOfPeople() {
        return noOfPeople;
    }
// set no of People
    public void setNoOfPeople(int noOfPeople) {
        this.noOfPeople = noOfPeople;
    }
// get customer
    public Customer getCustomer() {
        return customer;
    }
// set customer
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
// get no of table
    public int getNoOfTable() {
        return noOfTable;
    }
// set no of taBLE
    public void setNoOfTable(int noOfTable) {
        this.noOfTable = noOfTable;
    }
// GET BOOKING STATUS
    public String getBooking_status() {
        return booking_status;
    }
//set booking status
    public void setBooking_status(String booking_status) {
        this.booking_status = booking_status;
    }
//get creation date
    public Date getCreationDate() {
        return creationDate;
    }
// set creation date
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
// get id
    public int getId() {
        return id;
    }
// set id
    public void setId(int id) {
        this.id = id;
    }
// to string to display the  field  of the booking
    @Override
    public String toString() {
        return "Book{" + "id=" + id + ", noOfPeople=" + noOfPeople + ", customer=" + customer + ", noOfTable=" + noOfTable + ", booking_status=" + booking_status + ", creationDate=" + creationDate + '}';
    }

}
