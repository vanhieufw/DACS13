package com.movie.dao;

import com.movie.model.Ticket;
import com.movie.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TicketDAO {
    public void bookTicket(Ticket ticket) throws SQLException {
        String query = "INSERT INTO Ticket (CustomerID, ShowtimeID, SeatID, Price) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ticket.getCustomerID());
            stmt.setInt(2, ticket.getShowtimeID());
            stmt.setInt(3, ticket.getSeatID());
            stmt.setDouble(4, ticket.getPrice());
            stmt.executeUpdate();
        }
    }

    public boolean isSeatBooked(int seatID, int showtimeID) throws SQLException {
        String query = "SELECT COUNT(*) FROM Ticket WHERE SeatID = ? AND ShowtimeID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, seatID);
            stmt.setInt(2, showtimeID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}