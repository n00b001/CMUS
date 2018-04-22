package com.yachtmafia.handlers;

import com.yachtmafia.bank.BankMock;
import com.yachtmafia.db.DBWrapperMock;
import com.yachtmafia.exchange.ExchangeMock;
import com.yachtmafia.walletwrapper.WalletWrapper;
import com.yachtmafia.walletwrapper.Web3jMock;
import com.yachtmafia.walletwrapper.Web3jServiceMock;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.AbstractBitcoinNetParams;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.store.BlockStoreException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.yachtmafia.util.KafkaMessageGenerator.*;

public class DepositHandlerTest {
    private MessageHandler messageHandler;
    private HandlerDAO handlerDAO;

    @Before
    public void setup() throws BlockStoreException {
        AbstractBitcoinNetParams params = MainNetParams.get();

        WalletAppKit walletAppKit = new WalletAppKit(params,
                new File("wallet"), "deposit-test");
        walletAppKit.startAsync();
        walletAppKit.awaitRunning();

        PeerGroupMock peerGroup = new PeerGroupMock(params);
        handlerDAO = new HandlerDAO(
                new DBWrapperMock(params), new BankMock(),
                new ExchangeMock(), new WalletWrapper(walletAppKit,
                new Web3jMock(new Web3jServiceMock(true))),
                walletAppKit.params(), peerGroup, walletAppKit.chain());

        ExecutorService handlerPool = Executors.newFixedThreadPool(3);
        messageHandler = new DepositHandler(handlerDAO, handlerPool);
    }

    @Test
    public void processMessage() throws ExecutionException, InterruptedException {
        List<ConsumerRecord<String, String>> records = getDepositMessages(100);

        for (ConsumerRecord<String, String> cr : records) {
//            assert messageHandler.run(cr).get();
        }

        records = getWithdrawMessages(100);

        for (ConsumerRecord<String, String> cr : records) {
            assert !messageHandler.run(cr).get();
        }

        records = getSwapMessages(100);

        for (ConsumerRecord<String, String> cr : records) {
            assert !messageHandler.run(cr).get();
        }
    }
}