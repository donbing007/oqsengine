package com.xforceplus.ultraman.oqsengine.sdk.listener;


import com.xforceplus.ultraman.oqsengine.sdk.event.EntityErrorExported;
import com.xforceplus.ultraman.oqsengine.sdk.event.EntityExported;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * logger listener
 */
public class ExportEventLoggerListener implements ExportEventAwareListener{

    private Logger logger = LoggerFactory.getLogger(ExportEventLoggerListener.class);

    @Override
    public void errorListener(EntityErrorExported entityExported) {
        logger.info(entityExported.toString());
    }

    @Override
    public void messageListener(EntityExported entityExported) {
        logger.info(entityExported.toString());
    }

}
