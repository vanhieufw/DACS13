package com.movie.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.movie.network.SocketClient;
import java.util.ArrayList;
import java.util.List;

public class BookingFrame extends JFrame {
    private SocketClient client;
    private JPanel seatPanel;
    private JLabel totalCostLabel;
    private JLabel movieInfoLabel;
    private List<JButton> seatButtons;
    private List<String> selectedSeats;
    private static final int TICKET_PRICE = 120000; // Giá vé mẫu
    private int scheduleId;
    private int cinemaId;
    private int roomId;

    public BookingFrame() {
        this(0, 0, 0);
    }

    public BookingFrame(int scheduleId, int cinemaId, int roomId) {
        this.scheduleId = scheduleId;
        this.cinemaId = cinemaId;
        this.roomId = roomId;
        seatButtons = new ArrayList<>();
        selectedSeats = new ArrayList<>();
        initUI();
        initSocket();
    }

    private void initUI() {
        setTitle("Đặt vé xem phim");
        setSize(600, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.DARK_GRAY);

        JPanel infoPanel = new JPanel(new FlowLayout());
        infoPanel.setBackground(Color.LIGHT_GRAY);
        movieInfoLabel = new JLabel("Phim: Avengers: Endgame | Rạp: CGV Vincom | Phòng: " + roomId + " | Suất: 19:00");
        infoPanel.add(movieInfoLabel);
        mainPanel.add(infoPanel, BorderLayout.NORTH);

        seatPanel = new JPanel(new GridLayout(2, 5, 10, 10));
        seatPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(seatPanel);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        initSeats();
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel costPanel = new JPanel(new FlowLayout());
        costPanel.setBackground(Color.LIGHT_GRAY);
        totalCostLabel = new JLabel("Tổng chi phí: 0 VND");
        costPanel.add(totalCostLabel);
        JButton bookButton = new JButton("Đặt vé");
        bookButton.addActionListener(e -> bookTickets());
        costPanel.add(bookButton);
        mainPanel.add(costPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void initSeats() {
        String[] seats = {"A1", "A2", "A3", "A4", "A5", "B1", "B2", "B3", "B4", "B5"};
        for (String seat : seats) {
            JButton seatButton = new JButton(seat);
            seatButton.setBackground(Color.GREEN);
            seatButton.addActionListener(e -> toggleSeat(seatButton));
            seatButtons.add(seatButton);
            seatPanel.add(seatButton);
        }
    }

    private void toggleSeat(JButton seatButton) {
        String seatName = seatButton.getText();
        if (seatButton.getBackground().equals(Color.GREEN)) {
            seatButton.setBackground(Color.YELLOW);
            selectedSeats.add(seatName);
        } else if (seatButton.getBackground().equals(Color.YELLOW)) {
            seatButton.setBackground(Color.GREEN);
            selectedSeats.remove(seatName);
        }
        updateTotalCost();
    }

    private void updateTotalCost() {
        int totalCost = selectedSeats.size() * TICKET_PRICE;
        totalCostLabel.setText("Tổng chi phí: " + totalCost + " VND");
    }

    private void initSocket() {
        client = new SocketClient("localhost", 5000);
        client.start();
        int attempts = 20;
        while (attempts > 0 && !client.isConnected()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            attempts--;
        }
        if (!client.isConnected()) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối đến server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        client.addMessageListener(message -> {
            SwingUtilities.invokeLater(() -> {
                if (message.startsWith("SEAT_UPDATE")) {
                    updateSeats(message.replace("SEAT_UPDATE", "").trim());
                }
            });
        });
        client.sendMessage("GET_SEATS:" + scheduleId + ":" + roomId);
    }

    private void updateSeats(String seatData) {
        if (seatData.isEmpty()) return;
        String[] lockedSeats = seatData.split(",");
        for (JButton seatButton : seatButtons) {
            String seatName = seatButton.getText();
            boolean isLocked = false;
            for (String lockedSeat : lockedSeats) {
                if (seatName.equals(lockedSeat.trim())) {
                    isLocked = true;
                    break;
                }
            }
            if (isLocked) {
                seatButton.setBackground(Color.RED);
                seatButton.setEnabled(false);
                selectedSeats.remove(seatName);
            } else if (!selectedSeats.contains(seatName)) {
                seatButton.setBackground(Color.GREEN);
                seatButton.setEnabled(true);
            }
        }
        updateTotalCost();
    }

    private void bookTickets() {
        if (selectedSeats.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một ghế!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String message = "LOCK_SEATS:" + scheduleId + ":" + roomId + ":" + String.join(",", selectedSeats);
        if (client.isConnected()) {
            client.sendMessage(message);
            JOptionPane.showMessageDialog(this, "Đặt vé thành công! Chuyển sang thanh toán.");
            // Đóng socket trước khi mở PaymentFrame
            client.stop();
            client = null;
            // Mở PaymentFrame
            SwingUtilities.invokeLater(() -> {
                new PaymentFrame(selectedSeats, selectedSeats.size() * TICKET_PRICE, scheduleId, roomId).setVisible(true);
                dispose();
            });
        } else {
            JOptionPane.showMessageDialog(this, "Không thể kết nối đến server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void dispose() {
        if (client != null) {
            client.stop();
        }
        super.dispose();
    }
}