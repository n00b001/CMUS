package com.yachtmafia.handlers;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;

/**
 * Created by xfant on 2018-04-07.
 */
public class PeerGroupMock extends PeerGroup {
    public PeerGroupMock(NetworkParameters params) {
        super(params);
    }
}
