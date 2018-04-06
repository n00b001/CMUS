package com.yachtmafia.util;

import com.yachtmafia.config.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Properties;


/**
 * Created by xfant on 2018-01-20.
 */
public class Util {
//    private static final Logger LOG = Logger.getLogger(Util.class.getSimpleName());

//    private final static Logger LOG = LoggerFactory.getLogger(Util.class);
private static final Logger logger = LogManager.getLogger(Util.class);
    public static final int PRECISION = 20;

    private Util() {
    }

    public static BigDecimal getCoinDoubleValue(String amount, String currencySymbol) {
        return getCoinDoubleValue(amount, currencySymbol, PRECISION);
    }

    public static BigDecimal getCoinDoubleValue(String amount, String currencySymbol, int precision) {
        BigDecimal unitsPerCoin = getUnitsPerCoin(currencySymbol);

        BigDecimal amountBigInt = BigDecimal.valueOf(Long.parseLong(amount));
        RoundingMode roundingMode = RoundingMode.FLOOR;
        return amountBigInt.divide(unitsPerCoin, PRECISION, roundingMode);
    }

    public static BigDecimal getUnitsPerCoin(String currency) {
        switch (currency) {
            case "GBP":
                return BigDecimal.valueOf(100L);
            case "USD":
                return BigDecimal.valueOf(100L);
            case "EUR":
                return BigDecimal.valueOf(100L);
            case "JPY":
                return BigDecimal.valueOf(1000L);
            case "CHF":
                return BigDecimal.valueOf(100L);
            case "CAD":
                return BigDecimal.valueOf(100L);
            case "BTC":
                return BigDecimal.valueOf(100000000L);
            case "ETH":
                return BigDecimal.valueOf(1000000000000000000L);
            default:
                logger.error("UNKNOWN CURRENCY: " + currency);
                throw new RuntimeException("UNKNOWN CURRENCY: " + currency);
        }
    }

    public static boolean sendEmail(String message, String subject,
                                    Config config, String[] recipients) {

        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                config.ADMIN_EMAIL,
                                config.EMAIL_PASSWORD);
                    }
                });

        try {

            for (String recipient : recipients) {
                Message mailMessage = new MimeMessage(session);
                mailMessage.setFrom(new InternetAddress(config.ADMIN_EMAIL));
                mailMessage.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(recipient));
                mailMessage.setSubject(subject);
                mailMessage.setText(message);

                Transport.send(mailMessage);

                logger.info("Sent email to: " + recipient + "!");
            }
            return true;

        } catch (MessagingException e) {
            logger.error("Email error: ", e);
        }
        return false;
    }
}
