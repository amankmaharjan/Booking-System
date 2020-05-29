/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package booking;

import booking.Presenter.BookingPresenter;
import booking.model.BookingModel;
import booking.model.ConnectionException;
import booking.view.BookingUIInterface;
// Main class to load the application
public class Booking {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Create the model. Exit the application if connection be made to the 
        BookingModel pqm = new BookingModel();
        try {
            // intialsiing connection
            pqm.connect();
            // intialising paramaterised query
            pqm.initialise();
        } catch (ConnectionException e) {
            System.err.println(e.getMessage());
            e.getCause().printStackTrace();
            System.exit(1);
        }
        // Create the presenter and view and inject their dependencies. Note 
        // there is a circular dependency beetween the presenter and the view, so
        // an explicit binding method (bind()) is required.
        BookingPresenter pp = new BookingPresenter(pqm, pqm);
        BookingUIInterface pv = new BookingUIInterface(pp);
        pp.bind(pv);
        // Start the application
        pv.setVisible(true);
    }

}
