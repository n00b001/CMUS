package com.yachtmafia.config;

/**
 * Created by xfant on 2018-01-04.
 */
public class Config {
    //    public String database_name = "heroku_8fe142771c0a565";
//    public String port = "3306";
//    public String hostname = "eu-cdbr-west-01.cleardb.com";
//    public String connectionString = "jdbc:mysql://" + hostname + ":" + port + "/" + database_name;
//    public String username = "b9fa8d64d98abc";
//    public String password = "e86b2e53";
    public String database_name = "cryptobox-dev";
    public String port = "3306";
    public String hostname = "35.197.215.225";
    public String connectionString = "jdbc:mysql://" + hostname + ":" + port + "/" + database_name;
    public String username = "root";
    public String password = "Watersports2017";

    public String CURRENCIES_TABLE = "currencies";
    public String USERS_TABLE = "users";
    public String WALLETS_TABLE = "wallets";
    public String PRIVATE_TABLE = "privatekeys";
    public String TRANSACTION_TABLE = "transactions";
    public String JOBS_TABLE = "jobs";

    public String PUBLIC_ADDRESS = "public_address";
    public String CURRENCY_ID = "currency_id";
    public String PRIVATE_KEY = "private_key";
    public String USER_ID = "user_id";
    public String WALLET_ID = "wallet_id";
    public String ID = "id";
//    public String SYMBOL = "symbol";
    public String EMAIL = "email";
    public String FROM_CURRENCY_ID = "from_currency_id";
    public String TO_CURRENCY_ID = "to_currency_id";
    public String TO_AMOUNT = "to_amount";
    public String FROM_AMOUNT = "from_amount";
    public String EXCHANGE_RATE = "exchange_rate";
    public String TOPIC = "topic";
    public String KAFKA_MESSAGE = "kafka_message";

    public String CLIENT_ID_PAYPAL = "AbTewYvrX2Ts8bDNai80TeybnI8G9qKoPsUQoZN8Qs0fMvZZJsgCRBeRyIduGsuYLZ-sbgj47ZNeNFeV";
    public String CLIENT_SECRET_PAYPAL = "EBT3S1kHYZVH6Um_VlIfRRTEKTc86WzUniKvac770EL6J1T2ig77X2VrLWpfx4tBBBNwkQoCGXFhpjcK";

    public String COINBASE_KEY = "k3d1JeBuZoqPXDNM";
    public String COINBASE_SECRET = "yoSgwEVgXVRRnfeW8OZe8QybYZXDg9RJ";

    public String KAFKA_ADDRESS = "35.197.252.186:9092";
}
