package com.yachtmafia.walletwrapper;

import com.yachtmafia.WalletAppKitMock;
import com.yachtmafia.cryptoKeyPairs.CryptoKeyPair;
import com.yachtmafia.cryptoKeyPairs.CryptoKeyPairGenerator;
import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.wallet.Wallet;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.util.ArrayList;
import java.util.List;

import static com.yachtmafia.util.Util.getUnitsPerCoin;

/**
 * Created by xfant on 2018-01-29.
 */
public class WalletWrapperTest {
    WalletWrapper walletWrapper;
    WalletAppKit walletAppKit = new WalletAppKitMock();
    Web3j web3j = new Web3jMock(new Web3jServiceMock(true));

    public WalletWrapperTest() throws BlockStoreException {
    }

    @Before
    public void setUp() throws Exception {
        walletWrapper = new WalletWrapper(walletAppKit, web3j);
    }

    @Test
    public void startAsync() throws Exception {
        walletWrapper.startAsync();
    }

    @Test @Ignore
    public void sendTransaction() throws Exception {
        walletAppKit.startAsync();
//        walletAppKit.awaitRunning();
        NetworkParameters params = walletAppKit.params();
        CryptoKeyPair btc = CryptoKeyPairGenerator.parse("BTC", params);

        String publicAddress = btc.getPublicAddress();
        System.out.println(publicAddress);

        Block genesisBlock = params.getGenesisBlock();
        Block nextBlock = genesisBlock.createNextBlock(
                Address.fromBase58(params, publicAddress));
        List<Transaction> transactionsNext = nextBlock.getTransactions();
        for (Transaction transaction : transactionsNext){
            List<TransactionOutput> outputs = transaction.getOutputs();
            for (TransactionOutput output : outputs) {
                System.out.println(output.toString() + "\n");
//                Address addressFromP2SH = output.getAddressFromP2SH(params);
//                System.out.println(addressFromP2SH);
            }
        }

//        Wallet wallet = walletAppKit.wallet();

        Wallet wallet = new Wallet(params);
//        wallet.set
        wallet.importKey(ECKey.fromPrivate(btc.getPrivateKey().getBytes()));


        CryptoKeyPair depositAddress = CryptoKeyPairGenerator.parse("BTC", params);

        String privateKey = btc.getPrivateKey();
        ECKey ecKey = ECKey.fromPrivate(privateKey.getBytes());
        List<ECKey> list = new ArrayList<>();
        list.add(ecKey);

        String despositAddress = depositAddress.getPublicAddress();
        String amountOfCoin = getUnitsPerCoin("BTC").toPlainString();
        boolean success = walletWrapper.sendBitcoinTransaction(list,
                despositAddress, amountOfCoin, params);
        assert success;
    }

}