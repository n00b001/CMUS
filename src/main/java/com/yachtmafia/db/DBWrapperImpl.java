package com.yachtmafia.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yachtmafia.config.Config;
import com.yachtmafia.messages.SwapMessage;
import com.yachtmafia.util.StatusLookup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.NetworkParameters;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.yachtmafia.util.Util.PRECISION;


public class DBWrapperImpl implements DBWrapper {
    private static final Logger logger = LogManager.getLogger(DBWrapper.class);
//    private final Logger LOG = Logger.getLogger(getClass().getSimpleName());

    private Config config;

    public DBWrapperImpl(Config config) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Connector not set up!", e);
        }
        this.config = config;
    }


    @Override
    public boolean addNewWallet(String user, String coin, String publicAddress, String privateAddress) {
        List<String> userIdSet = getUserId(user);
        if (userIdSet.isEmpty()){
            logger.error("UserId not found! ");
            return false;
        }else if(userIdSet.size() > 1){
            logger.error("Multiple users found! ");
            return false;
        }
        String userId = userIdSet.get(0);
        if (userId == null) {
            logger.error("User not found: " + user);
            return false;
        }

//        String query =
//                "SELECT " + config.ID + " FROM " +
//                        config.WALLETS_TABLE + " WHERE " + config.CURRENCY_ID
//                        + " = '" + coin + "' AND " + config.USER_ID + " = " + userId
//                        + " AND " + config.PUBLIC_ADDRESS + " is not null";
//        String userWallet = getSingleQueryString(query);
//
//        if (userWallet != null) {
//            query =
//                    "SELECT " + config.ID + " FROM " +
//                            config.PRIVATE_TABLE + " WHERE " + config.WALLET_ID
//                            + " = " + userWallet + " AND " + config.PRIVATE_KEY + " is not null";
//            String userPrivatekey = getSingleQueryString(query);
//            if (userPrivatekey != null) {
//                logger.warn("User already has wallet! " + user);
//                return true;
//            } else {
//                /**
//                 * create private key
//                 */
//                return insertPrivateKey(privateAddress, userWallet);
//            }
//        } else {
            /**
             * create wallet and private key
             */
            String query =
                    "INSERT INTO " +
                            config.WALLETS_TABLE +
                            "    (" + config.CURRENCY_ID + ", " + config.USER_ID + ", " + config.PUBLIC_ADDRESS + ") " +
                            " VALUES " +
                            "    ('" + coin + "', '" + userId + "', '" + publicAddress + "') ";

            String walletId;

            try (Connection con = DriverManager.getConnection(
                    config.connectionString,
                    config.username, config.password);
                 Statement stmt = con.createStatement()
            ) {
                int rs = stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
                try (ResultSet generatedKeys = stmt.getGeneratedKeys();) {
                    if (generatedKeys.next()) {
                        walletId = String.valueOf(generatedKeys.getLong(1));
                    } else {
                        logger.error("Failed to add wallet " + user);
                        return false;
                    }
                }
            } catch (SQLException e) {
                logger.error("Caught ", e);
                return false;
            }

            return insertPrivateKey(privateAddress, walletId);
//        }
    }

    private boolean insertPrivateKey(String privateAddress, String userWallet) {
        String query =
                "INSERT INTO " +
                        config.PRIVATE_TABLE +
                        "    (" + config.WALLET_ID + ", " + config.PRIVATE_KEY + ") " +
                        " VALUES " +
                        "    ('" + userWallet + "', '" + privateAddress + "') ";
        return modifyQuery(query);
    }

    @Override
    public boolean addTransaction(SwapMessage message, String toAmount) {
        String fromAmount = message.getAmountOfCoin();
        if (null == fromAmount || "".equals(fromAmount) || "0".equals(fromAmount)) {
            throw new RuntimeException("Transaction from amount is invalid: " + fromAmount);
        }
        if (null == toAmount || "".equals(toAmount) || "0".equals(toAmount)) {
            throw new RuntimeException("Transaction to amount is invalid: " + toAmount);
        }

        List<String> userIdList = getUserId(message.getUsername());
        if (userIdList.isEmpty()){
            logger.error("UserId not found! ");
            return false;
        }else if(userIdList.size() > 1){
            logger.error("Multiple users found! ");
            return false;
        }
        String userId = userIdList.get(0);
        if (userId == null) {
            throw new RuntimeException("userId not found: " + message.getUsername());
        }

        Long cointToValue = Long.parseLong(toAmount);
        Long cointFromValue = Long.parseLong(fromAmount);

        RoundingMode roundingMode = RoundingMode.HALF_EVEN;
        BigDecimal coinToBigValue = BigDecimal.valueOf(cointToValue);
        BigDecimal coinFromBigValue = BigDecimal.valueOf(cointFromValue);

        BigDecimal exchangeRate = coinToBigValue.divide(coinFromBigValue, PRECISION, roundingMode);

//        String query = "INSERT INTO " +
//                config.TRANSACTION_TABLE +
//                "    (from_currency_id, user_id, from_amount, to_amount, to_currency_id, exchange_rate)" +
//                " VALUES" +
//                "    ('" + currencyIDfrom + "', '" + userId + "', '" + fromAmount + "', '"
//                + toAmount + "', '" + currencyIDto + "', '" + exchangeRate.doubleValue() + "')";

        String query = "SELECT " + config.ID + " FROM " + config.TRANSACTION_TABLE + " WHERE "
                + config.ID + " = '" + message.getID() + "'";
//        String id = getSingleQueryString(query);

        List<String> idList = getSingleQueryString(query);
        if (idList.isEmpty()){
            logger.error("UserId not found! ");
            return false;
        }else if(idList.size() > 1){
            logger.error("Multiple users found! ");
            return false;
        }
        String id = idList.get(0);

        if (id == null) {
            logger.error("No transactions with ID: " + message.getID());
            return false;
        }

        query = "UPDATE " + config.TRANSACTION_TABLE + " SET "
                + config.TO_AMOUNT + " = " + cointToValue + ", " + config.EXCHANGE_RATE + " = "
                + exchangeRate.doubleValue() + " WHERE " + config.ID + " = '" + message.getID() + "'";

        return modifyQuery(query);
//        return success;
    }

    private boolean modifyQuery(String query) {
        try (Connection con = DriverManager.getConnection(
                config.connectionString,
                config.username, config.password);
             Statement stmt = con.createStatement()
        ) {
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            logger.error("Caught: ", e);
            return false;
        }
        return true;
    }

//    private String getCurrencyId(String coinName) {
//        String query =
//                "SELECT " +
//                        "    " + config.ID + " " +
//                        " FROM " +
//                        config.CURRENCIES_TABLE +
//                        " WHERE " +
//                        "    " + config.SYMBOL + " = '" + coinName + "'";
//        String currencyID = getSingleQueryString(query);
//        if (currencyID == null) {
//            throw new RuntimeException("Currency not found: " + coinName);
//        }
//        return currencyID;
//    }


    @Override
    public BigDecimal getFunds(String user, String coin) {
        String query =
                "SELECT " +
                        "   SUM(to_amount) " +
                        " FROM " +
                        "    " + config.TRANSACTION_TABLE + " P INNER JOIN " + config.USERS_TABLE
                        + " U ON P." + config.USER_ID + " = U." + config.ID + " " +
                        "    INNER JOIN " + config.CURRENCIES_TABLE + " C ON C." + config.ID + " = P." + config.TO_CURRENCY_ID + " " +
                        " WHERE " +
                        "    U." + config.EMAIL + " = '" + user + "' AND C." + config.ID + " = '" + coin + "'";
//        String toFunds = getSingleQueryString(query);
        List<String> toFundsList = getSingleQueryString(query);
        if (toFundsList.isEmpty()){
            logger.error("UserId not found! ");
            return null;
        }else if(toFundsList.size() > 1){
            logger.error("Multiple users found! ");
            return null;
        }
        String toFunds = toFundsList.get(0);

        if (toFunds == null) {
            logger.warn("No funds found for user: " + user + " and coin: " + coin);
            return null;
        }

        query =
                "SELECT " +
                        "   SUM(from_amount) " +
                        " FROM " +
                        "    " + config.TRANSACTION_TABLE + " P INNER JOIN " + config.USERS_TABLE
                        + " U ON P." + config.USER_ID + " = U." + config.ID + " " +
                        "    INNER JOIN " + config.CURRENCIES_TABLE + " C ON C." + config.ID + " = P." + config.FROM_CURRENCY_ID + " " +
                        " WHERE " +
                        "    U." + config.EMAIL + " = '" + user + "' AND C." + config.ID + " = '" + coin + "'";
//        String fromFunds = getSingleQueryString(query);
        toFundsList = getSingleQueryString(query);
        if (toFundsList.isEmpty()){
            logger.error("UserId not found! ");
            return null;
        }else if(toFundsList.size() > 1){
            logger.error("Multiple users found! ");
            return null;
        }
        String fromFunds = toFundsList.get(0);

        if (fromFunds == null) {
            logger.warn("No from funds found for user: " + user + " and coin: " + coin);
            return null;
        }

        BigDecimal toFundsDec = BigDecimal.valueOf(Long.parseLong(toFunds));
        BigDecimal fromFundsDec = BigDecimal.valueOf(Long.parseLong(fromFunds));

        return toFundsDec.subtract(fromFundsDec);
    }

    @Override
    public List<String> getPrivateKey(String user, String coin) {
        String query =
                "SELECT " +
                        "   " + config.PRIVATE_KEY + " " +
                        " FROM " +
                        "    " + config.PRIVATE_TABLE + " P" +
                        "    INNER JOIN " + config.WALLETS_TABLE + " W ON P." + config.WALLET_ID + " = W." + config.ID + " " +
                        "    INNER JOIN " + config.USERS_TABLE + " U ON W." + config.USER_ID + " = U." + config.ID + " " +
                        "    INNER JOIN " + config.CURRENCIES_TABLE + " C ON C." + config.ID + " = '" + coin + "'" +
                        " WHERE " +
                        "    U." + config.EMAIL + " = '" + user + "' AND C." + config.ID + " = '" + coin + "'";
        return getSingleQueryString(query);
    }

    @Override
    public boolean removeWallet(String user, String coin, String address) {
        List<String> userIdList = getUserId(user);
        if (userIdList.isEmpty()){
            logger.error("No Users found!");
            return false;
        }else if(userIdList.size() > 1){
            logger.error("Multiple Users found!");
            return false;
        }
        String userId = userIdList.get(0);
        String query = "DELETE from " + config.WALLETS_TABLE + " WHERE " + config.USER_ID + " = " + userId
                + " AND " + config.CURRENCY_ID + " = '" + coin + "' AND " + config.PUBLIC_ADDRESS
                + " = '" + address + "'";
        return modifyQuery(query);
//        return false;
    }

    @Override
    public boolean addTransaction(String id, SwapMessage swapMessage, String topic) throws JsonProcessingException {
        List<String> userIdSet = getUserId(swapMessage.getUsername());
        if (userIdSet.isEmpty()){
            logger.error("UserId not found! ");
            return false;
        }else if(userIdSet.size() > 1){
            logger.error("Multiple users found! ");
            return false;
        }
        String userId = userIdSet.get(0);

        String query =
                "INSERT INTO " +
                        config.TRANSACTION_TABLE +
                        "    (" + config.ID + ", " + config.USER_ID + ", "
                        + config.FROM_AMOUNT + ", " + config.FROM_CURRENCY_ID + ", "
                        + config.TO_CURRENCY_ID + ", " + config.TOPIC
                        + ", " + config.KAFKA_MESSAGE + ") " +
                        " VALUES " +
                        "    ('" + id + "', '" + userId
                        + "', '" + swapMessage.getAmountOfCoin()+ "', '" + swapMessage.getFromCoinName()
                        + "', '" + swapMessage.getToCoinName() + "', '" + topic
                        + "', '" + swapMessage.toJson() + "') ";
        return modifyQuery(query);
    }

    @Override
    public boolean removeTransaction(String id) {
        String query = "DELETE from " + config.TRANSACTION_TABLE + " WHERE "
                + config.ID + " = '" + id + "'";
        return modifyQuery(query);
    }

    @Override
    public boolean addTransactionStatus(SwapMessage swapMessage, StatusLookup statusCode) {
        String query =
                "INSERT INTO " +
                        config.TRANSACTION_PROGRESS_TABLE +
                        "    (" + config.TRANSACTION_ID + ", "
                        + config.STATUS_ID + ") " +
                        " VALUES " +
                        "    ('" + swapMessage.getID() + "', '" + statusCode.getCode() + "') ";
        return modifyQuery(query);
    }

    private List<String> getUserId(String user) {
        String query = "SELECT " + config.ID + " FROM " + config.USERS_TABLE + " WHERE "
                + config.EMAIL + " = '" + user + "'";
        return getSingleQueryString(query);
    }

    @Override
    public List<ECKey> getKeys(String user, String coin) {
        List<ECKey> list = new ArrayList<>();
        List<String> privateKey = getPrivateKey(user, coin);
        for (String s : privateKey) {
            ECKey key = DumpedPrivateKey.fromBase58(NetworkParameters.fromID(config.NETWORK), s).getKey();
//            ECKey ecKey = ECKey.fromPrivate(s.getBytes());
            list.add(key);
        }
        return list;
    }

    @Override
    public List<String> getPublicAddress(String user, String coin) {
        String query =
                "SELECT " +
                        "   " + config.PUBLIC_ADDRESS + " " +
                        " FROM " +
                        "    " + config.WALLETS_TABLE + " W INNER JOIN " + config.USERS_TABLE
                        + " U ON W." + config.USER_ID + " = U." + config.ID + " " +
                        "    INNER JOIN " + config.CURRENCIES_TABLE + " C " +
                        " ON C." + config.ID + " = W." + config.CURRENCY_ID + " " +
                        " WHERE " +
                        "    U." + config.EMAIL + " = '" + user + "' AND C." + config.ID + " = '" + coin + "'";
        return getSingleQueryString(query);
    }

    private List<String> getSingleQueryString(String query) {
        try (Connection con = DriverManager.getConnection(
                config.connectionString,
                config.username, config.password);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)
        ) {
            List<String> hashSet = new ArrayList<>();
            while (rs.next()) {
                hashSet.add(rs.getString(1));
            }
            return hashSet;
        } catch (SQLException e) {
            logger.error("Caught: ", e);
        }
        return Collections.emptyList();
    }

    private List<List<String>> getMultiQueryString(String query, int columns) {
        try (Connection con = DriverManager.getConnection(
                config.connectionString,
                config.username, config.password);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)
        ) {
            List<List<String>> hashSet = new ArrayList<>();
            while (rs.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= columns; i++) {
                    row.add(rs.getString(i));
                }
                hashSet.add(row);
            }
            return hashSet;
        } catch (SQLException e) {
            logger.error("Caught: ", e);
        }
        return Collections.emptyList();
    }
}
