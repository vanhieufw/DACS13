package com.movie.bus;

import com.movie.dao.ShowtimeDAO;
import com.movie.model.Showtime;
import com.movie.model.Movie;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ShowtimeBUS {
    private ShowtimeDAO showtimeDAO = new ShowtimeDAO();
    private MovieBUS movieBUS = new MovieBUS();

    public void addShowtime(int movieID, int roomID, Date showDate, int staffID) throws SQLException {
        Showtime showtime = new Showtime();
        showtime.setMovieID(movieID);
        showtime.setRoomID(roomID);
        showtime.setShowDate(showDate);
        showtime.setStaffID(staffID);
        showtime.setStatus("Không chiếu"); // Mặc định khi thêm suất chiếu mới
        showtimeDAO.addShowtime(showtime);
    }

    public void updateShowtimeStatus(int showtimeID, String status) throws SQLException {
        showtimeDAO.updateShowtimeStatus(showtimeID, status);
    }

    public List<Showtime> getAllShowtimes() throws SQLException {
        List<Showtime> showtimes = showtimeDAO.getAllShowtimes();
        updateShowtimeStatuses(showtimes);
        return showtimes;
    }

    private void updateShowtimeStatuses(List<Showtime> showtimes) throws SQLException {
        long currentTime = System.currentTimeMillis(); // Thời gian hiện tại: 18-05-2025, 21:54
        for (Showtime showtime : showtimes) {
            String currentStatus = showtime.getStatus();
            // Chỉ cập nhật nếu trạng thái chưa phải là "Ẩn"
            if (currentStatus != null && !currentStatus.equals("Ẩn")) {
                // Kiểm tra nếu suất chiếu chưa có phim
                if (showtime.getMovieID() == 0 || showtime.getShowDate() == null) {
                    if (!currentStatus.equals("Không chiếu")) {
                        showtimeDAO.updateShowtimeStatus(showtime.getShowtimeID(), "Không chiếu");
                        showtime.setStatus("Không chiếu");
                    }
                    continue;
                }

                long showTime = showtime.getShowDate().getTime();
                Movie movie = movieBUS.getMovieById(showtime.getMovieID());
                long duration = (movie != null) ? movie.getDuration() * 60 * 1000L : 0; // Chuyển phút thành ms, mặc định 0 nếu không có phim

                if (currentTime < showTime) {
                    if (!currentStatus.equals("Sắp công chiếu")) {
                        showtimeDAO.updateShowtimeStatus(showtime.getShowtimeID(), "Sắp công chiếu");
                        showtime.setStatus("Sắp công chiếu");
                    }
                } else if (currentTime >= showTime && currentTime < showTime + duration) {
                    if (!currentStatus.equals("Đang chiếu")) {
                        showtimeDAO.updateShowtimeStatus(showtime.getShowtimeID(), "Đang chiếu");
                        showtime.setStatus("Đang chiếu");
                    }
                } else if (currentTime >= showTime + duration) {
                    if (!currentStatus.equals("Đã chiếu xong")) {
                        showtimeDAO.updateShowtimeStatus(showtime.getShowtimeID(), "Đã chiếu xong");
                        showtime.setStatus("Đã chiếu xong");
                    }
                }
            }
        }
    }
}