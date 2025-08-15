package com.codvortex.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public void sendOtpEmail(String recipientEmail, String otp, String username, String fullName) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        helper.setFrom(senderEmail);
        helper.setTo(recipientEmail);
        helper.setSubject("Password Reset OTP");

        helper.setText("<div style=\"font-family: Helvetica,Arial,sans-serif;min-width:1000px;overflow:auto;line-height:2\">\n" +
                "  <div style=\"margin:50px auto;width:70%;padding:20px 0\">\n" +
                "    <div style=\"border-bottom:1px solid #eee\">\n" +
                "      <a href=\"\" style=\"font-size:1.4em;color: #C059FC;text-decoration:none;font-weight:600\">COD VORTEX</a>\n" +
                "    </div>\n" +
                "    <p style=\"font-size:1.1em\">Changement de mot de pass, (" +
                ((username != null) ? username : fullName) +
                ")</p>\n" +
                "    <p>Vous pouver utiliser ce code OTP pour changer votre mot de pass. Le code OTP est valable pour 10 minutes.</p>\n" +
                "    <h2 style=\"background: #C059FC;margin: 0 auto;width: max-content;padding: 0 10px;color: #fff;border-radius: 4px;\">" +
                otp +
                "    </h2>\n" +
                "    <p style=\"font-size:0.9em;\">Merci,<br />COD VORTEX</p>\n" +
                "    <hr style=\"border:none;border-top:1px solid #eee\" />\n" +
                "    <div style=\"float:right;padding:8px 0;color:#aaa;font-size:0.8em;line-height:1;font-weight:300\">\n" +
                "      <p>COD VORTEX Inc</p>\n" +
                "      <p>Maroc</p>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</div>", true);
        System.out.println("cscsdcsdcsdcsddddddddddddddddd");
        mailSender.send(message);
    }

}