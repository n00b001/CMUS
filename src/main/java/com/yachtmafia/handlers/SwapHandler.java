package com.yachtmafia.handlers;

import com.yachtmafia.messages.SwapMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.yachtmafia.util.Const.SWAP_TOPIC_NAME;

public class SwapHandler implements MessageHandler {
    private static final String TOPIC_NAME = SWAP_TOPIC_NAME;
//    private final Logger LOG = Logger.getLogger(getClass().getSimpleName());
private static final Logger logger = LogManager.getLogger(SwapHandler.class);

    private final HandlerDAO handlerDAO;
    private ConsumerRecord<String, String> message;
    private ExecutorService pool;

    public SwapHandler(HandlerDAO handlerDAO, ExecutorService pool) {
        this.handlerDAO = handlerDAO;
        this.pool = pool;
    }

    private SwapHandler(HandlerDAO handlerDAO, ConsumerRecord<String, String> message) {
        this.handlerDAO = handlerDAO;
        this.message = message;
    }

    @Override
    public Future<Boolean> run(ConsumerRecord<String, String> message) {
        return pool.submit(new SwapHandler(handlerDAO, message));
    }

    @Override
    public Boolean call() throws Exception {
        try {
            if (TOPIC_NAME.equals(message.topic())) {
                SwapMessage swapMessage = new SwapMessage(message.value());
                logger.info("Swapmessage: " + swapMessage);
                throw new NotImplementedException();
            }
        }catch (Exception e){
            logger.error("Caught: ", e);
        }
        return false;
    }
}
