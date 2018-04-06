package com.yachtmafia.db;

import com.yachtmafia.config.Config;
import com.yachtmafia.cryptoKeyPairs.CryptoKeyPair;
import com.yachtmafia.cryptoKeyPairs.CryptoKeyPairGenerator;
import com.yachtmafia.messages.SwapMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.bitcoinj.params.UnitTestParams;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.yachtmafia.util.KafkaMessageGenerator.getDepositMessages;
import static org.junit.Assert.assertEquals;

/**
 * Created by xfant on 2018-01-14.
 */
public class DBWrapperImplTest {
    DBWrapper dbWrapper;
    @Before
    public void setUp() throws Exception {
        Config config = new Config();
        dbWrapper = new DBWrapperImpl(config);
    }

    @Test
    public void addNewWallet() throws Exception {
        String user = "MarkRobins@gmail.com";
        String coin = "BTC";
        CryptoKeyPair keyPair = CryptoKeyPairGenerator.parse(coin, UnitTestParams.get());
        boolean success = dbWrapper.addNewWallet(user, coin, keyPair.getPublicAddress(), keyPair.getPrivateKey());
        assert success;
        assert dbWrapper.removeWallet(user, coin, keyPair.getPublicAddress());
    }

    @Test
    public void addPortfolioBalance() throws Exception {
        String purchasedAmount = String.valueOf(100000000);// 1btc;
        String id = "0";
        ConsumerRecord<String, String> consumerRecord = getDepositMessages(id, 1).get(0);
        String recordString = consumerRecord.value();
        SwapMessage message = new SwapMessage(recordString);
        String topic = "DEPOSIT";
        assert dbWrapper.addTransaction(id, message, topic);
        boolean success = dbWrapper.addTransaction(message, purchasedAmount);
        assert dbWrapper.removeTransaction(id);
        assert success;
    }

    @Test
    public void getFunds() throws Exception {
        String user = "MarkRobins@gmail.com";
        String coin = "BTC";
        BigDecimal funds = dbWrapper.getFunds(user, coin);
//        assert funds != null;
    }

    @Test
    public void getPrivateKey() throws Exception {
        String user = "MarkRobins@gmail.com";
        String coin = "BTC";
        String publicAddress = "publicUnitTest";
        String privateAddress = "privateUnitTest";
        assert dbWrapper.addNewWallet(user, coin, publicAddress, privateAddress);
        List<String> privateKey = dbWrapper.getPrivateKey(user, coin);
        assertEquals(privateAddress, privateKey.get(0));
        assert dbWrapper.removeWallet(user, coin, publicAddress);
    }

    @Test
    public void getPublicAddress() throws Exception {
        String user = "MarkRobins@gmail.com";
        String coin = "BTC";
        String publicAddressExpected = "unitTestPublic";
        boolean success = dbWrapper.addNewWallet(user, coin, publicAddressExpected, "unitTestPrivate");
        assert success;
        List<String> publicAddress = dbWrapper.getPublicAddress(user, coin);
        assertEquals(publicAddressExpected, publicAddress.get(0));
        success = dbWrapper.removeWallet(user, coin, publicAddressExpected);
        assert success;
    }

}