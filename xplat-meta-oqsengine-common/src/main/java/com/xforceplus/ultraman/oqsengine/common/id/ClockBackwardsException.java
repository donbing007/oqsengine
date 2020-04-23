package com.xforceplus.ultraman.oqsengine.common.id;

/**
 * clock backwards exception
 */
public class ClockBackwardsException extends RuntimeException{

    private static final String message = "Last referenceTime %s is after reference time %s";

    public ClockBackwardsException(Long lastTime, Long currentTime){
        super(String.format(message, lastTime, currentTime));
    }
}
