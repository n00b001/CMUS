package com.yachtmafia.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.annotations.VisibleForTesting;
import com.yachtmafia.messages.SwapMessage;
import com.yachtmafia.util.StatusLookup;
import org.bitcoinj.core.ECKey;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface DBWrapper {

    List<ECKey> getKeys(String user, String coin);

    List<String> getPublicAddress(String user, String coin) throws SQLException;
    boolean addNewWallet(String user, String coin, String publicAddress, String privateAddress);
    boolean addTransaction(SwapMessage message, String purchasedAmount);
    BigDecimal getFunds(String user, String coin);
    List<String> getPrivateKey(String user, String coin);

    @VisibleForTesting
    boolean removeWallet(String user, String coin, String address);

    @VisibleForTesting
    boolean addTransaction(String id, SwapMessage swapMessage, String topic) throws JsonProcessingException;

    @VisibleForTesting
    boolean removeTransaction(String id);

    boolean addTransactionStatus(SwapMessage statusCode, StatusLookup swapMessage);
}
