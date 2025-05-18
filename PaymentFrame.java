package com.movie.ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PaymentFrame extends JFrame {
    private List<String> seats;
    private int totalCost;
    private int scheduleId;
    private int roomId;

    public PaymentFrame(List<String> seats, int totalCost, int scheduleId, int roomId) {
        this.seats = seats;
        this.totalCost = totalCost;
        this.scheduleId = scheduleId;
        this.roomId = roomId;
        initUI();
    }

    private void initUI() {
        setTitle("Thanh toán");
        setSize(400, 300);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.LIGHT_GRAY);

        // Thông tin đơn hàng
        JPanel infoPanel = new JPanel(new GridLayout(4, 1));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.add(new JLabel("Ghế đã chọn: " + String.join(", ", seats)));
        infoPanel.add(new JLabel("Tổng chi phí: " + totalCost + " VND"));
        infoPanel.add(new JLabel("Lịch chiếu: " + scheduleId + " | Phòng: " + roomId));
        infoPanel.add(new JLabel("Phương thức thanh toán: Momo (giả định)"));
        mainPanel.add(infoPanel, BorderLayout.CENTER);

        // Nút xác nhận
        JButton confirmButton = new JButton("Xác nhận thanh toán");
        confirmButton.addActionListener(e -> confirmPayment());
        mainPanel.add(confirmButton, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private void confirmPayment() {
        // Chạy tác vụ nặng trong luồng riêng để không chặn giao diện
        Thread paymentThread = new Thread(() -> {
            try {
                // Giả lập xử lý thanh toán (có thể gọi API thanh toán tại đây)
                Thread.sleep(500); // Giả lập thời gian xử lý

                // Xuất hóa đơn XML
                exportInvoiceToXML();

                // Cập nhật giao diện sau khi thanh toán thành công
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Thanh toán thành công! Hóa đơn đã được xuất.");
                    dispose();
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Lỗi khi thanh toán: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                });
            }
        });
        paymentThread.start();
    }

    private void exportInvoiceToXML() throws Exception {
        // Tạo thư mục invoices nếu chưa tồn tại
        File invoiceDir = new File("invoices");
        if (!invoiceDir.exists()) {
            invoiceDir.mkdirs();
        }

        // Tạo tên tệp dựa trên thời gian
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "invoices/invoice_" + timestamp + ".xml";

        // Tạo tài liệu XML
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        // Tạo phần tử gốc <invoice>
        Element rootElement = doc.createElement("invoice");
        doc.appendChild(rootElement);

        // Thêm thông tin hóa đơn
        Element invoiceId = doc.createElement("invoiceId");
        invoiceId.appendChild(doc.createTextNode("INV-" + timestamp));
        rootElement.appendChild(invoiceId);

        Element dateTime = doc.createElement("dateTime");
        dateTime.appendChild(doc.createTextNode(LocalDateTime.now().toString()));
        rootElement.appendChild(dateTime);

        Element schedule = doc.createElement("scheduleId");
        schedule.appendChild(doc.createTextNode(String.valueOf(scheduleId)));
        rootElement.appendChild(schedule);

        Element room = doc.createElement("roomId");
        room.appendChild(doc.createTextNode(String.valueOf(roomId)));
        rootElement.appendChild(room);

        Element seatsElement = doc.createElement("seats");
        for (String seat : seats) {
            Element seatElement = doc.createElement("seat");
            seatElement.appendChild(doc.createTextNode(seat));
            seatsElement.appendChild(seatElement);
        }
        rootElement.appendChild(seatsElement);

        Element total = doc.createElement("totalCost");
        total.appendChild(doc.createTextNode(String.valueOf(totalCost)));
        rootElement.appendChild(total);

        Element paymentMethod = doc.createElement("paymentMethod");
        paymentMethod.appendChild(doc.createTextNode("Momo"));
        rootElement.appendChild(paymentMethod);

        // Ghi tài liệu XML ra tệp
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(fileName));
        transformer.transform(source, result);

        System.out.println("Hóa đơn đã được xuất: " + fileName);
    }
}