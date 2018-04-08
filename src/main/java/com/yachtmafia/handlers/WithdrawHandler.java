package com.yachtmafia.handlers;

import com.yachtmafia.messages.SwapMessage;
import com.yachtmafia.util.StatusLookup;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.ECKey;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.yachtmafia.util.Const.WITHDRAW_TOPIC_NAME;

public class WithdrawHandler implements MessageHandler {
    private static final String TOPIC_NAME = WITHDRAW_TOPIC_NAME;
//    private final Logger LOG = Logger.getLogger(getClass().getSimpleName());
private static final Logger logger = LogManager.getLogger(WithdrawHandler.class);

//    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final HandlerDAO handlerDAO;
    private ExecutorService pool;
    private ConsumerRecord<String, String> message;

    public WithdrawHandler(HandlerDAO handlerDAO, ExecutorService pool) {
        this.handlerDAO = handlerDAO;
        this.pool = pool;
    }

    private WithdrawHandler(HandlerDAO handlerDAO, ConsumerRecord<String, String> message) {
        this.handlerDAO = handlerDAO;
        this.message = message;
    }

    private void addTransactionStatus(SwapMessage swapMessage, StatusLookup statusLookup) {
        boolean success = handlerDAO.getDbWrapper()
                .addTransactionStatus(swapMessage, statusLookup);
        if (!success){
            logger.error("Failed to update status " + statusLookup);
        }
    }

    @Override
    public Future<Boolean> run(ConsumerRecord<String, String> message) {
        return pool.submit(new WithdrawHandler(handlerDAO, message));
    }

    @Override
    public Boolean call() throws Exception {
        if (TOPIC_NAME.equals(message.topic())) {
            SwapMessage swapMessage = new SwapMessage(message.value());
            logger.info("swapmessage: " + swapMessage);
            addTransactionStatus(swapMessage, StatusLookup.REQUEST_RECEIVED_BY_SERVER);

//            String publicAddress = handlerDAO.getDbWrapper().getPublicAddress(swapMessage.getUsername(),
//                    swapMessage.getFromCoinName());
//            if (publicAddress == null) {
//                logger.error("user: " + swapMessage.getUsername() + " does not have wallet for coin: "
//                        + swapMessage.getFromCoinName());
//
//                addTransactionStatus(swapMessage, StatusLookup.COULD_NOT_FIND_WALLET);
//                return true;
//            }
            List<ECKey> keys = handlerDAO.getDbWrapper().getKeys(swapMessage.getUsername(),
                    swapMessage.getFromCoinName());
            addTransactionStatus(swapMessage, StatusLookup.WALLET_FOUND);

//            String privateKey = handlerDAO.getDbWrapper().getPrivateKey(swapMessage.getUsername(),
//                    swapMessage.getFromCoinName());
//            if (privateKey == null) {
//                logger.error("user: " + swapMessage.getUsername() + " Does not have private key for coin: "
//                        + swapMessage.getFromCoinName());
//                addTransactionStatus(swapMessage, StatusLookup.COULD_NOT_FIND_PRIVATE_KEY);
//                return true;
//            }
            addTransactionStatus(swapMessage, StatusLookup.PRIVATE_KEY_FOUND);

//            String depositAddress = handlerDAO.getExchange().getDepositAddress(swapMessage.getFromCoinName());
            List<String> depositAddresses = handlerDAO.getDbWrapper().getPublicAddress(
                    handlerDAO.getConfig().ADMIN_EMAIL,
                    swapMessage.getFromCoinName());
            if (depositAddresses.isEmpty()){
                logger.error("No admin deposit address found!");
                addTransactionStatus(swapMessage, StatusLookup.FAILED);
                return false;
            }else if(depositAddresses.size() > 1){
                logger.warn("Multiple admin addresses found!", depositAddresses);
                addTransactionStatus(swapMessage, StatusLookup.FAILED);
                return false;
            }

            boolean success = handlerDAO.getWalletWrapper().sendBitcoinTransaction(keys,
                    depositAddresses.get(0), swapMessage.getAmountOfCoin(), handlerDAO.getNetwork(),
                    handlerDAO.getPeerGroup(), handlerDAO.getChain());
            if (!success) {
                logger.error("Error handling wallet to exchange transaction for: " + swapMessage.toString());
                addTransactionStatus(swapMessage, StatusLookup.WALLET_TO_EXCHANGE_TRANSACTION_FAILURE);
                return false;
            }
            addTransactionStatus(swapMessage, StatusLookup.COINS_SENT_TO_EXCHANGE);


//            String purchasedAmount = handlerDAO.getExchange().exchangeCurrency(swapMessage.getFromCoinName(),
//                    swapMessage.getToCoinName(),
//                    swapMessage.getAmountOfCoin());
//            boolean success = handlerDAO.getExchange().withdrawToBank(swapMessage.getToCoinName(), purchasedAmount);
//            if (!success) {
//                logger.error("Error withdrawing from exchange to bank with message; " + swapMessage
//                        + " for amount: " + purchasedAmount);
//                addTransactionStatus(swapMessage, StatusLookup.FAILED_TO_WITHDRAW_FROM_EXCHANGE);
//                return false;
//            }

            String purchasedAmount = handlerDAO.getExchange().getLowestPrice(swapMessage.getFromCoinName()
                    + swapMessage.getToCoinName());
            addTransactionStatus(swapMessage, StatusLookup.MONEY_WITHDRAWN_FROM_EXCHANGE);


            success = handlerDAO.getBank().payUser(swapMessage.getToCoinName(), purchasedAmount,
                    swapMessage.getUsername());
            if (!success) {
                logger.error("error when transfering from bank to user: " + swapMessage.toString()
                        + " for amount: " + purchasedAmount);
                addTransactionStatus(swapMessage, StatusLookup.COULD_NOT_PAY_USER);
                return false;
            }
            addTransactionStatus(swapMessage, StatusLookup.PAID_USER);

            success = handlerDAO.getDbWrapper().addTransaction(swapMessage, purchasedAmount);
            if (!success) {
                logger.error("error when inserting portfoliobalance: " + swapMessage.toString()
                        + " for amount " + purchasedAmount);
                addTransactionStatus(swapMessage, StatusLookup.COULD_NOT_UPDATE_PORTFOLIO_BALANCE);

                return false;
            }
            addTransactionStatus(swapMessage, StatusLookup.SUCCESS);

            return true;
        }
        return false;
    }
}