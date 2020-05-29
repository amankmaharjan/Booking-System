/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package booking.model;

/**
 *
 * domain class for storing cusotmer records
 */
public class Customer {
    // fields for id, name , email, addrees and phones
    private int id;
    private String name;
    private String email;
    private String address;
    private String phone;

    // No arg customer
    public Customer() {
    }

    // parametrised constructuor
    public Customer(int id, String name, String email, String address, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.address = address;
        this.phone = phone;
    }

   
// returns id
    public int getId() {
        return id;
    }
// sets id
    public void setId(int id) {
        this.id = id;
    }
// returns name
    public String getName() {
        return name;
    }
// sets name
    public void setName(String name) {
        this.name = name;
    }
// return email
    public String getEmail() {
        return email;
    }
// set email
    public void setEmail(String email) {
        this.email = email;
    }
// return address
    public String getAddress() {
        return address;
    }
// sets address
    public void setAddress(String address) {
        this.address = address;
    }
// return phones
    public String getPhone() {
        return phone;
    }
//sets phone
    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "Customer{" + "id=" + id + ", name=" + name + ", email=" + email + ", address=" + address + ", phone=" + phone + '}';
    }
    
}
