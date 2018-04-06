package com.yachtmafia.db;

import com.yachtmafia.cryptoKeyPairs.BTC;
import com.yachtmafia.messages.SwapMessage;
import com.yachtmafia.util.StatusLookup;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.ECKey;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class DBWrapperMock implements DBWrapper {
    private BTC btc;

    public DBWrapperMock(NetworkParameters paramers) {
        btc = new BTC(paramers);
    }

    @Override
    public List<ECKey> getKeys(String user, String coin) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getPublicAddress(String user, String coin) {
        List<String> list = new ArrayList<>();
        if ("BTC".equals(coin)){
            list.add(btc.getPublicAddress());
        }
        return list;
    }

    @Override
    public boolean addNewWallet(String user, String coin, String publicAddress, String privateAddress) {
        return true;
    }

    @Override
    public boolean addTransaction(SwapMessage message, String purchasedAmount) {
        return true;
    }

    @Override
    public BigDecimal getFunds(String user, String coin) {
        return BigDecimal.valueOf(1d);
    }

    @Override
    public List<String> getPrivateKey(String user, String coin) {
        List list = new ArrayList<String>();
        if ("BTC".equals(coin)){
            list.add(btc.getPrivateKey());
        }
        return list;
    }

    @Override
    public boolean removeWallet(String user, String coin, String address) {
        return true;
    }

    @Override
    public boolean addTransaction(String id, SwapMessage swapMessage, String topic) {
        return true;
    }

    @Override
    public boolean removeTransaction(String id) {
        return true;
    }

    @Override
    public boolean addTransactionStatus(SwapMessage statusCode, StatusLookup swapMessage) {
        return true;
    }
}
