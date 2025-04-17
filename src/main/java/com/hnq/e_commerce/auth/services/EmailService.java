package com.hnq.e_commerce.auth.services;

import com.hnq.e_commerce.auth.entities.User;

import com.hnq.e_commerce.entities.Order;
import com.hnq.e_commerce.entities.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    // Gửi email xác thực tài khoản
    public void sendVerificationEmail(User user) {
        String subject = "Verify Your Email";
        String senderName = "HNQ Shop";
        String mailContent = "Hello " + user.getEmail() + ",\n";
        mailContent += "Your verification code is: " + user.getVerificationCode() + "\n";
        mailContent += "Please enter this code to verify your email.\n";
        mailContent += senderName;

        sendEmail(user.getEmail(), subject, mailContent);
    }

    // Gửi email xác nhận đơn hàng thành công với thông tin chi tiết
    public void sendOrderConfirmation(User user, Order order) {
        String subject = "🛍️ Order Confirmation - HNQ Shop";
        String senderName = "🛒 HNQ Shop";
        StringBuilder mailContent = new StringBuilder();

        mailContent.append("👋 Hello ").append(user.getFirstName()).append(",\n\n")
                .append("🎉 Thank you for your order! Below are your order details:\n\n")
                .append("🆔 **Order ID:** ").append(order.getId()).append("\n")
                .append("📅 **Order Date:** ").append(order.getOrderDate()).append("\n\n");

        mailContent.append("🛍️ **Order Items:**\n");
        mailContent.append("---------------------------------------\n");
        for (OrderItem item : order.getOrderItemList()) {
            mailContent.append("🔹 *").append(item.getProduct().getName()).append("*\n")
                    .append("   - Quantity: ").append(item.getQuantity()).append("\n")
                    .append("   - Price: $").append(item.getItemPrice()).append("\n\n");
        }
        mailContent.append("---------------------------------------\n");
        mailContent.append("💰 **Total Amount:** *$").append(order.getTotalAmount()).append("*\n\n");

        mailContent.append("📍 **Shipping Address:**\n")
                .append(order.getAddress().getName()).append("\n")
                .append(order.getAddress().getStreet()).append("\n")
                .append(order.getAddress().getCity()).append(", ")
                .append(order.getAddress().getDistrict()).append(" ")
                .append(order.getAddress().getZipCode()).append("\n")
                .append("📞 Phone: ").append(order.getAddress().getPhoneNumber()).append("\n\n");

        mailContent.append("📦 Your order is being processed and will be shipped soon!\n");
        mailContent.append("📲 You can check your order status anytime in your [account dashboard](https://yourwebsite.com/orders).\n\n");

        mailContent.append("Thank you for shopping with us! 💖\n\n");
        mailContent.append("Best regards,\n").append(senderName);

        sendEmail(user.getEmail(), subject, mailContent.toString());
    }


    // Gửi email hóa đơn với đường dẫn tải file PDF
    public void sendInvoiceEmail(User user, String invoicePdfUrl, Order order) {
        String subject = "Your Invoice from HNQ Shop";
        String senderName = "HNQ Shop";
        StringBuilder mailContent = new StringBuilder();

        mailContent.append("Hello ").append(user.getEmail()).append(",\n\n");
        mailContent.append("Thank you for your purchase. Your invoice details are below:\n");
        mailContent.append("Order ID: ").append(order.getId()).append("\n\n");

        mailContent.append("🛒 Order Items:\n");
        for (OrderItem item : order.getOrderItemList()) {
            mailContent.append("- ").append(item.getProduct().getName())
                    .append(" (x").append(item.getQuantity()).append(") - $")
                    .append(item.getItemPrice()).append("\n");
        }

        mailContent.append("\nTotal Amount: $").append(order.getTotalAmount()).append("\n\n");
        mailContent.append("📄 Download your invoice here: ").append(invoicePdfUrl).append("\n\n");

        mailContent.append(senderName);

         sendEmail(user.getEmail(), subject, mailContent.toString());
    }

    // Phương thức gửi email chung
    private void sendEmail(String recipient, String subject, String content) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(sender);
            mailMessage.setTo(recipient);
            mailMessage.setSubject(subject);
            mailMessage.setText(content);
            javaMailSender.send(mailMessage);
        } catch (Exception e) {
            e.getMessage();
        }
    }

    public void sendPassword(User user, String password) {
        String subject = "Your Password from HNQ Shop";
        String senderName = "HNQ Shop";
        String mailContent = "Hello " + user.getEmail() + ",\n";
        mailContent += "Your password is: " + password + "\n";
        mailContent += senderName;

        sendEmail(user.getEmail(), subject, mailContent);
    }
}
