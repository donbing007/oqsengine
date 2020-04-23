package com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl;

import org.apache.metamodel.UpdateableDataContext;

/**
 * ring data-context data struct
 */
public class RingDC {

    /**
     * dc ref
     */
    private UpdateableDataContext dc;

    private RingDC next;

    public RingDC(UpdateableDataContext dc) {
        this.dc = dc;
    }

    public void setNext(RingDC ringDC) {
        this.next = ringDC;
    }

    public RingDC next() {
        return next;
    }

    public UpdateableDataContext getDc() {
        return dc;
    }
}
