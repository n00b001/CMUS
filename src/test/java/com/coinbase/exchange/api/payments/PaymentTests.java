package com.coinbase.exchange.api.payments;

import com.coinbase.exchange.api.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class PaymentTests extends BaseTest {

    @Autowired
    PaymentService paymentService;

    @Test
    public void hasAvailablePayments(){
        List<PaymentType> types = paymentService.getPaymentTypes();
        assertTrue(types.size() > 0);
    }
    @Test
    public void hasCoinbaseAccounts(){
        List<CoinbaseAccount> accounts = paymentService.getCoinbaseAccounts();
        assertTrue(accounts.size() > 0);
    }
}
