package com.yachtmafia.walletwrapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.web3j.protocol.Web3j;

import java.util.List;

/**
 * Created by xfant on 2018-01-07.
 */
public class WalletWrapper {
//    private final Logger LOG = Logger.getLogger(getClass().getSimpleName());

    private static final Logger logger = LogManager.getLogger(WalletWrapper.class);
//    private File file = new File("wallet/.");
    private final WalletAppKit bitcoinWalletAppKit;// = new WalletAppKit(MainNetParams.get(), file, "");
    private final Web3j ethereumWallet;

    public WalletWrapper(WalletAppKit bitcoinWalletAppKit, Web3j ethereumWallet) {
        this.bitcoinWalletAppKit = bitcoinWalletAppKit;
        this.ethereumWallet = ethereumWallet;
    }

    public void startAsync() {
        bitcoinWalletAppKit.startAsync();
    }

    public WalletAppKit getBitcoinWalletAppKit() {
        return bitcoinWalletAppKit;
    }

    public Web3j getEthereumWallet() {
        return ethereumWallet;
    }

    public boolean sendBitcoinTransaction(List<ECKey> keys,
                                          String depositAddress, String amountOfCoin,
                                          NetworkParameters network, PeerGroup peerGroup, AbstractBlockChain chain) {

        Wallet wallet = Wallet.fromKeys(network, keys);
        wallet.reset();
//        Wallet wallet = new Wallet(network);
//        for (ECKey key : keys) {
//            boolean success = wallet.importKey(key);
//            if (!success){
//                logger.error("Failed to import key!");
//                return false;
//            }
//        }

//        peerGroup = new PeerGroup(network, chain);

        peerGroup.addWallet(wallet);
        chain.addWallet(wallet);

//        if(wallet.getLastBlockSeenHeight() == 0){
//        }

//        peerGroup.stop();
        peerGroup.start();
        peerGroup.downloadBlockChain();

        while(!Thread.currentThread().isInterrupted() &&
                wallet.getLastBlockSeenHeight() == -1){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.warn("Caught: ", e);
                Thread.currentThread().interrupt();
            }
        }
        try {
            /**
             * todo: test
             */

//            ECKey ecKey = ECKey.fromPrivate(privateKey.getBytes());
//            List<ECKey> ecKeyList = new ArrayList<>();
//            ecKeyList.add(ecKey);
//            Wallet wallet = Wallet.fromKeys(network, ecKeyList);

            Address depositAddr = Address.fromBase58(network, depositAddress);

//            wallet.

            Long satoshis = Long.valueOf(amountOfCoin);
//            Long satoshis = Long.valueOf(amountOfCoin);
            final Coin value = Coin.valueOf(satoshis);
            if (wallet.getBalance().isLessThan(value)){
                logger.error("Not enough balance my dude");
                chain.removeWallet(wallet);
                peerGroup.removeWallet(wallet);
                return false;
            }

            SendRequest sendRequest = SendRequest.to(depositAddr, value);
            Wallet.SendResult sendResult = wallet.sendCoins(sendRequest);
            sendResult.broadcastComplete.get();

//            // Make sure this code is run in a single thread at once.
//            SendRequest request = SendRequest.to(depositAddr, value);
//            // The SendRequest object can be customized at this point to modify how the transaction will be created.
//            wallet.completeTx(request);
//            // Ensure these funds won't be spent again.
//            wallet.commitTx(request.tx);
//            // A proposed transaction is now sitting in request.tx - send it in the background.
//            ListenableFuture<Transaction> future = bitcoinWalletAppKit.peerGroup()
//                    .broadcastTransaction(request.tx).future();
//
//            // The future will complete when we've seen the transaction ripple across the network to a sufficient degree.
//            // Here, we just wait for it to finish, but we can also attach a listener that'll get run on a background
//            // thread when finished. Or we could just assume the network accepts the transaction and carry on.
//            Transaction transaction = future.get();
//            logger.info("Transactions " + transaction);
            chain.removeWallet(wallet);
            peerGroup.removeWallet(wallet);
            return true;
        } catch (Exception e) {
            logger.error("Caught: ", e);
            chain.removeWallet(wallet);
            peerGroup.removeWallet(wallet);
            return false;
        }
    }

}
