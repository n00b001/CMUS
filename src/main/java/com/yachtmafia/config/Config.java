package com.yachtmafia.config;

//import com.coinbase.exchange.api.exchange.Signature;
//import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Enumeration;

/**
 * Created by xfant on 2018-01-04.
 */
public class Config {
    public static String DATABASE_NAME = "heroku_924181d6d880656";
    public static String DATABASE_PORT = "3306";
    public static String DATABASE_HOSTNAME = "eu-cdbr-west-02.cleardb.net";
    public static String DATABASE_CONNECTIONSTRING = "jdbc:mysql://" + DATABASE_HOSTNAME + ":" + DATABASE_PORT + "/" + DATABASE_NAME;
    public static String DATABASE_USERNAME = "baeba0d22e4ce6";
    public static String DATABASE_PASSWORD = "46492c38";

    public static int AMOUNT_OF_HANDLER_THREADS = 30;

    public static String NETWORK = "org.bitcoin.test";

    public static String CURRENCIES_TABLE = "currencies";
    public static String USERS_TABLE = "users";
    public static String WALLETS_TABLE = "wallets";
    public static String PRIVATE_TABLE = "privatekeys";
    public static String TRANSACTION_TABLE = "transactions";
    public static String TRANSACTION_PROGRESS_TABLE = "transaction_progress";
    public static String PUBLIC_ADDRESS = "public_address";
    public static String CURRENCY_ID = "currency_id";
    public static String PRIVATE_KEY = "private_key";
    public static String USER_ID = "user_id";
    public static String WALLET_ID = "wallet_id";
    public static String ID = "id";
    public static String TRANSACTION_ID = "transaction_id";
    public static String STATUS_ID = "status_id";
    public static String USER_EMAIL = "email";
    public static String FROM_CURRENCY_ID = "from_currency_id";
    public static String TO_CURRENCY_ID = "to_currency_id";
    public static String TO_AMOUNT = "to_amount";
    public static String FROM_AMOUNT = "from_amount";
    public static String EXCHANGE_RATE = "exchange_rate";
    public static String TOPIC = "topic";
    public static String KAFKA_MESSAGE = "kafka_message";

    public static String[] EMAIL_RECIPTS = new String[]{
            "purchases@yachtmafia.mailclark.ai",
            "yachtmafia01@gmail.com"};

    public static String ADMIN_EMAIL = "team@cryptosave.co.uk";
    public static String ADMIN_EMAIL_LOGIN = "yachtmafia01@gmail.com";
    public static String EMAIL_PASSWORD = "Watersports2017";

    public static String CLIENT_ID_PAYPAL = "AbTewYvrX2Ts8bDNai80TeybnI8G9qKoPsUQoZN8Qs0fMvZZJsgCRBeRyIduGsuYLZ-sbgj47ZNeNFeV";
    public static String CLIENT_SECRET_PAYPAL = "EBT3S1kHYZVH6Um_VlIfRRTEKTc86WzUniKvac770EL6J1T2ig77X2VrLWpfx4tBBBNwkQoCGXFhpjcK";

    public static String KAFKA_ADDRESS = "35.197.203.197:9092";

    public static final String LUNO_KEY = "";
    public static final String LUNO_SECRET = "";

    static {

        File configFile = new File("config.properties");
        if (!configFile.exists()) {
            System.out.println("Couldn't find config file!");
        }else {
            try (InputStream resourceAsStream =
                         new FileInputStream(configFile)) {
                Properties prop = new Properties();
                prop.load(resourceAsStream);

                Enumeration<?> e = prop.propertyNames();
                while (e.hasMoreElements()) {
                    String key = (String) e.nextElement();
                    String value = prop.getProperty(key);
                    try {
                        Object valueOfVar = getValueOf(key);

                        if (valueOfVar == null) {
                            System.out.println("Variable not found for config item: " + key);
                        } else {
//                            String stringValue = String.valueOf(valueOfVar);
                            Object newValue = setIfExists(valueOfVar, value);
                            Field field = Config.class.getField(key);
                            field.set(null, newValue);
                        }
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }

                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static Object getValueOf(String lookingForValue)
            throws Exception {
        Field field = Config.class.getField(lookingForValue);
        Class clazzType = field.getType();
        if (clazzType.toString().equals("double")) {
            return field.getDouble(Config.class);
        } else if (clazzType.toString().equals("int")) {
            return field.getInt(Config.class);
        } else if (clazzType.toString().equals("char")) {
            return field.getChar(Config.class);
        }
        return field.get(Config.class);
    }

    private static Object setIfExists(Object field, Object configItem) {
        if (configItem != null) {
            return configItem;
        }
        return field;
    }
}
