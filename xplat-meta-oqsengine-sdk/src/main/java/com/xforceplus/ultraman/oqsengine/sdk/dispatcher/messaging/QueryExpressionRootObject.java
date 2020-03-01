package com.xforceplus.ultraman.oqsengine.sdk.dispatcher.messaging;

public class QueryExpressionRootObject {

    private final Message msg;

    private final Object[] args;

    public QueryExpressionRootObject(Message msg, Object[] args) {
        this.msg = msg;
        this.args = args;
    }

    public Message getMsg() {
        return this.msg;
    }

    public Object[] getArgs() {
        return this.args;
    }

}