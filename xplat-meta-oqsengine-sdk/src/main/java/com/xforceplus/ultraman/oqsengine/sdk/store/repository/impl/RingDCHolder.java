package com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl;

import java.util.LinkedList;
import java.util.List;

public class RingDCHolder {

    List<RingDC> ringDCList = new LinkedList<>();

    private RingDC root;

    private RingDC current;

    public RingDCHolder(RingDC root){
        this.root = root;
        this.current = this.root;
        this.current.setNext(root);
        ringDCList.add(root);
    }

    /**
     * make this a circle
     * @param node
     */
    public void addNode(RingDC node){
        this.current.setNext(node);
        node.setNext(root);
        this.current = node;
        ringDCList.add(node);
    }

    public RingDC getRoot() {
        return root;
    }
}
