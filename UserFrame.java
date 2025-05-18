package com.movie.ui;

import com.movie.bus.MovieBUS;
import com.movie.bus.RoomBUS;
import com.movie.bus.TicketBUS;
import com.movie.model.BookingHistory;
import com.movie.model.Movie;
import com.movie.model.Room;
import com.movie.model.Showtime;
import com.movie.network.ThreadManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class UserFrame extends JFrame {
    private int customerID;
    private MovieBUS movieBUS = new MovieBUS();
    private RoomBUS roomBUS = new RoomBUS();
    private TicketBUS ticketBUS = new TicketBUS();
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JLabel timeLabel;

    public UserFrame(int customerID) {
        this.customerID = customerID;
        initUI();
        startClock();
    }

    private void initUI() {
        setTitle("Giao diện người dùng - Hệ thống bán vé xem phim");
        setSize(1000, 700);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(40, 40, 40));

        JButton moviesButton = new JButton("Danh sách phim");
        JButton historyButton = new JButton("Lịch sử đặt vé");
        JButton logoutButton = new JButton("Đăng xuất");

        styleButton(moviesButton);
        styleButton(historyButton);
        styleButton(logoutButton);

        moviesButton.addActionListener(e -> showPanel("Movies"));
        historyButton.addActionListener(e -> showPanel("History"));
        logoutButton.addActionListener(e -> dispose());

        sidebar.add(Box.createVerticalStrut(30));
        sidebar.add(moviesButton);
        sidebar.add(Box.createVerticalStrut(15));
        sidebar.add(historyButton);
        sidebar.add(Box.createVerticalStrut(15));
        sidebar.add(logoutButton);

        // Content area
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.add(createMoviesPanel(), "Movies");
        contentPanel.add(createHistoryPanel(), "History");

        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);
        setVisible(true);
    }

    private void startClock() {
        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timeLabel.setForeground(Color.BLACK);
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        timePanel.add(timeLabel);
        add(timePanel, BorderLayout.NORTH);

        new Thread(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            while (true) {
                timeLabel.setText(sdf.format(new java.util.Date()));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void styleButton(JButton button) {
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        button.setBackground(new Color(60, 60, 60));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(80, 80, 80));
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(new Color(60, 60, 60));
            }
        });
    }

    private void showPanel(String panelName) {
        cardLayout.show(contentPanel, panelName);
    }

    private JPanel createMoviesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245));
        JLabel titleLabel = new JLabel("Danh sách phim", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel movieListPanel = new JPanel();
        movieListPanel.setLayout(new BoxLayout(movieListPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(movieListPanel);
        panel.add(scrollPane, BorderLayout.CENTER);

        loadMovies(movieListPanel);

        return panel;
    }

    private void loadMovies(JPanel movieListPanel) {
        try {
            movieListPanel.removeAll();
            List<Movie> movies = movieBUS.getAllMovies();
            for (Movie movie : movies) {
                JPanel moviePanel = new JPanel(new BorderLayout());
                moviePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                moviePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

                JLabel posterLabel = new JLabel();
                if (movie.getPoster() != null && !movie.getPoster().isEmpty()) {
                    posterLabel.setIcon(new ImageIcon(new ImageIcon(movie.getPoster()).getImage().getScaledInstance(200, 140, Image.SCALE_SMOOTH)));
                }
                posterLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                moviePanel.add(posterLabel, BorderLayout.WEST);

                JPanel infoPanel = new JPanel(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(5, 5, 5, 5);
                gbc.anchor = GridBagConstraints.WEST;

                JLabel titleLabel = new JLabel(movie.getTitle());
                titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
                infoPanel.add(titleLabel, gbc);

                JTextArea descriptionArea = new JTextArea(movie.getDescription());
                descriptionArea.setLineWrap(true);
                descriptionArea.setWrapStyleWord(true);
                descriptionArea.setEditable(false);
                descriptionArea.setBackground(infoPanel.getBackground());
                descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                gbc.gridy = 1;
                infoPanel.add(descriptionArea, gbc);

                JLabel roomLabel = new JLabel("Phòng chiếu: Đang tải...");
                gbc.gridy = 2;
                infoPanel.add(roomLabel, gbc);

                JLabel priceLabel = new JLabel("Giá vé: Đang tải...");
                gbc.gridy = 3;
                infoPanel.add(priceLabel, gbc);

                JButton bookButton = new JButton("Đặt vé");
                gbc.gridy = 4; gbc.gridwidth = 1;
                infoPanel.add(bookButton, gbc);

                moviePanel.add(infoPanel, BorderLayout.CENTER);

                // Load room and price info
                ThreadManager.execute(() -> {
                    try {
                        List<Room> rooms = roomBUS.getAllRooms();
                        Room movieRoom = rooms.stream()
                                .filter(r -> r.getMovieTitle() != null && r.getMovieTitle().equals(movie.getTitle()))
                                .findFirst()
                                .orElse(null);
                        if (movieRoom != null) {
                            roomLabel.setText("Phòng chiếu: " + movieRoom.getRoomName());
                            priceLabel.setText("Giá vé: " + String.format("%,.0f VND", movieRoom.getPrice()));
                            bookButton.setEnabled(movieRoom.getStatus().equals("Đang chiếu") || movieRoom.getStatus().equals("Chuẩn bị chiếu"));
                        } else {
                            roomLabel.setText("Phòng chiếu: Không có");
                            priceLabel.setText("Giá vé: Không có");
                            bookButton.setEnabled(false);
                        }
                    } catch (SQLException ex) {
                        roomLabel.setText("Phòng chiếu: Lỗi tải dữ liệu");
                        priceLabel.setText("Giá vé: Lỗi tải dữ liệu");
                    }
                });

                bookButton.addActionListener(e -> {
                    try {
                        Room movieRoom = roomBUS.getAllRooms().stream()
                                .filter(r -> r.getMovieTitle() != null && r.getMovieTitle().equals(movie.getTitle()))
                                .findFirst()
                                .orElse(null);
                        if (movieRoom != null) {
                            new BookingFrame(customerID, movieRoom.getRoomID(), movie.getMovieID()).setVisible(true);
                        } else {
                            JOptionPane.showMessageDialog(this, "Phim này hiện không có phòng chiếu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Không thể mở giao diện đặt vé: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                });

                movieListPanel.add(moviePanel);
            }
            movieListPanel.revalidate();
            movieListPanel.repaint();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải danh sách phim: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245));
        JLabel titleLabel = new JLabel("Lịch sử đặt vé", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel historyPanel = new JPanel();
        historyPanel.setLayout(new BoxLayout(historyPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(historyPanel);
        panel.add(scrollPane, BorderLayout.CENTER);

        loadBookingHistory(historyPanel);

        return panel;
    }

    private void loadBookingHistory(JPanel historyPanel) {
        try {
            historyPanel.removeAll();
            List<BookingHistory> historyList = ticketBUS.getBookingHistory(customerID);
            for (BookingHistory history : historyList) {
                JPanel historyItem = new JPanel(new BorderLayout());
                historyItem.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                historyItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                String bookingInfo = String.format("Phim: %s - Phòng: %s - Ghế: %s - Giá: %,d VND - Ngày đặt: %s",
                        history.getMovieTitle(), history.getRoomName(), history.getSeatNumber(),
                        (int) history.getPrice(), sdf.format(history.getBookingDate()));
                JLabel infoLabel = new JLabel(bookingInfo);
                infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                historyItem.add(infoLabel, BorderLayout.CENTER);

                historyPanel.add(historyItem);
            }
            historyPanel.revalidate();
            historyPanel.repaint();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải lịch sử đặt vé: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}