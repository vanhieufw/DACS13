package com.movie.bus;

import com.movie.dao.BookingHistoryDAO;
import com.movie.dao.TicketDAO;
import com.movie.model.BookingHistory;
import com.movie.model.Seat;
import com.movie.model.Ticket;
import com.movie.network.SocketClient;
import com.movie.network.ThreadManager;

import javax.swing.JOptionPane;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Business logic class for handling ticket booking and payment operations.
 */
public class TicketBUS {
    private final TicketDAO ticketDAO = new TicketDAO();
    private final BookingHistoryDAO bookingHistoryDAO = new BookingHistoryDAO();

    /**
     * Processes a payment and books tickets for the specified seats.
     * @param customerID The ID of the customer.
     * @param showtimeID The ID of the showtime.
     * @param seats The list of seats to book.
     * @param totalPrice The total price for the booking.
     * @return A message indicating the result of the operation.
     * @throws SQLException If a database error occurs.
     */
    public String processPayment(int customerID, int showtimeID, List<Seat> seats, double totalPrice) throws SQLException {
        if (customerID <= 0 || showtimeID <= 0 || seats == null || seats.isEmpty() || totalPrice < 0) {
            JOptionPane.showMessageDialog(null,
                    "Thông tin đặt vé không hợp lệ",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return "Thông tin đặt vé không hợp lệ";
        }

        // Check if any seat is already booked
        for (Seat seat : seats) {
            if (ticketDAO.isSeatBooked(seat.getSeatID(), showtimeID)) {
                String message = "Ghế " + seat.getSeatNumber() + " đã được đặt!";
                JOptionPane.showMessageDialog(null,
                        message,
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return message;
            }
        }

        try {
            for (Seat seat : seats) {
                // Book ticket
                Ticket ticket = new Ticket();
                ticket.setCustomerID(customerID);
                ticket.setShowtimeID(showtimeID);
                ticket.setSeatID(seat.getSeatID());
                ticket.setPrice(totalPrice / seats.size());
                ticketDAO.bookTicket(ticket);

                // Add booking history
                BookingHistory history = new BookingHistory();
                history.setCustomerID(customerID);
                history.setTicketID(ticket.getTicketID());
                history.setBookingDate(new Date());
                // Assuming MovieTitle, RoomName, and SeatNumber are set elsewhere or retrieved
                history.setMovieTitle("Unknown"); // Placeholder; replace with actual logic
                history.setRoomName("Unknown");   // Placeholder; replace with actual logic
                history.setSeatNumber(seat.getSeatNumber());
                history.setPrice(totalPrice / seats.size());
                bookingHistoryDAO.addBooking(history);
            }

            // Notify via socket in a separate thread
            ThreadManager.execute(() -> {
                try {
                    SocketClient client = new SocketClient("localhost", 8080);
                    client.sendMessage("Đặt vé thành công cho khách hàng ID: " + customerID);
                } catch (Exception e) {
                    System.err.println("Error sending socket message: " + e.getMessage());
                }
            });

            JOptionPane.showMessageDialog(null,
                    "Thanh toán thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            return "Thanh toán thành công!";
        } catch (SQLException e) {
            System.err.println("Error processing payment for customer " + customerID + ": " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Không thể xử lý thanh toán: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }

    /**
     * Retrieves the booking history for a specific customer.
     * @param customerID The ID of the customer.
     * @return A list of booking history records for the customer.
     * @throws SQLException If a database error occurs.
     */
    public List<BookingHistory> getBookingHistory(int customerID) throws SQLException {
        if (customerID <= 0) {
            JOptionPane.showMessageDialog(null,
                    "ID khách hàng không hợp lệ",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            throw new IllegalArgumentException("ID khách hàng không hợp lệ");
        }

        try {
            return bookingHistoryDAO.getBookingsByCustomer(customerID);
        } catch (SQLException e) {
            System.err.println("Error retrieving booking history for customer " + customerID + ": " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Không thể tải lịch sử đặt vé: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }
}