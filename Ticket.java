package com.movie.model;

public class Ticket {
    private int ticketID;
    private int customerID;
    private int showtimeID;
    private int seatID;
    private double price;

    public Ticket() {}

    public int getTicketID() { return ticketID; }
    public void setTicketID(int ticketID) { this.ticketID = ticketID; }

    public int getCustomerID() { return customerID; }
    public void setCustomerID(int customerID) { this.customerID = customerID; }

    public int getShowtimeID() { return showtimeID; }
    public void setShowtimeID(int showtimeID) { this.showtimeID = showtimeID; }

    public int getSeatID() { return seatID; }
    public void setSeatID(int seatID) { this.seatID = seatID; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}