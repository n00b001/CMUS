package com.yachtmafia.walletwrapper;

import com.google.common.util.concurrent.ListenableFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.web3j.protocol.Web3j;

import java.util.ArrayList;
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
                                          NetworkParameters network) {

        try {
            /**
             * todo: test
             */
            Wallet wallet = new Wallet(bitcoinWalletAppKit.params());
            for (ECKey key : keys) {
                boolean success = wallet.importKey(key);
                if (!success){
                    logger.error("Failed to import key!");
                    return false;
                }
            }

//            ECKey ecKey = ECKey.fromPrivate(privateKey.getBytes());
//            List<ECKey> ecKeyList = new ArrayList<>();
//            ecKeyList.add(ecKey);
//            Wallet wallet = Wallet.fromKeys(network, ecKeyList);

            Address address = Address.fromBase58(network, depositAddress);

//            wallet.

            Long satoshis = Long.valueOf(amountOfCoin);
            final Coin value = Coin.valueOf(satoshis);
            // Make sure this code is run in a single thread at once.
            SendRequest request = SendRequest.to(address, value);
            // The SendRequest object can be customized at this point to modify how the transaction will be created.
            wallet.completeTx(request);
            // Ensure these funds won't be spent again.
            wallet.commitTx(request.tx);
            // A proposed transaction is now sitting in request.tx - send it in the background.
            ListenableFuture<Transaction> future = bitcoinWalletAppKit.peerGroup()
                    .broadcastTransaction(request.tx).future();

            // The future will complete when we've seen the transaction ripple across the network to a sufficient degree.
            // Here, we just wait for it to finish, but we can also attach a listener that'll get run on a background
            // thread when finished. Or we could just assume the network accepts the transaction and carry on.
            Transaction transaction = future.get();
            logger.info("Transactions " + transaction);
            return true;
        } catch (Exception e) {
            logger.error("Caught: ", e);
            return false;
        }
    }

}
