package com.yachtmafia.handlers;

import com.yachtmafia.bank.Bank;
import com.yachtmafia.config.Config;
import com.yachtmafia.db.DBWrapper;
import com.yachtmafia.exchange.Exchange;
import com.yachtmafia.walletwrapper.WalletWrapper;
import org.bitcoinj.core.AbstractBlockChain;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;

/**
 * Created by xfant on 2017-12-31.
 */
public class HandlerDAO {
    private final DBWrapper dbWrapper;
    private final Bank bank;
    private final Exchange exchange;
    private final WalletWrapper walletWrapper;
    private final Config config;
    private final NetworkParameters network;
    private final PeerGroup peerGroup;
    private final AbstractBlockChain chain;

    public HandlerDAO(DBWrapper dbWrapper, Bank bank, Exchange exchange, WalletWrapper walletWrapper, Config config,
                      NetworkParameters network, PeerGroup peerGroup, AbstractBlockChain chain) {
        this.dbWrapper = dbWrapper;
        this.bank = bank;
        this.exchange = exchange;
        this.walletWrapper = walletWrapper;
        this.config = config;
        this.network = network;
        this.peerGroup = peerGroup;
        this.chain = chain;
    }

    DBWrapper getDbWrapper() {
        return dbWrapper;
    }

    Bank getBank() {
        return bank;
    }

    Exchange getExchange() {
        return exchange;
    }

    WalletWrapper getWalletWrapper() {
        return walletWrapper;
    }

    public Config getConfig() {
        return config;
    }

    public NetworkParameters getNetwork() {
        return network;
    }

    public PeerGroup getPeerGroup() {
        return peerGroup;
    }

    public AbstractBlockChain getChain() {
        return chain;
    }
}
