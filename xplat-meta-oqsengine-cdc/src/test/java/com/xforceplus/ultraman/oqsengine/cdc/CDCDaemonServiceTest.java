package com.xforceplus.ultraman.oqsengine.cdc;

import com.xforceplus.ultraman.oqsengine.cdc.connect.SingleCDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.TestCallbackService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.impl.SphinxConsumerService;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.utils.SQLJsonIEntityValueBuilder;
import com.xforceplus.ultraman.oqsengine.storage.utils.IEntityValueBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.cdc.constant.CDCConstant.ZERO;

/**
 * desc :
 * name : CDCDaemonServiceTest
 *
 * @author : xujia
 * date : 2020/11/5
 * @since : 1.8
 */
public class CDCDaemonServiceTest extends AbstractContainer {

    private CDCDaemonService cdcDaemonService;

    @Before
    public void before() throws Exception {

        initMaster();

        initDaemonService();
    }

    private void initDaemonService() throws SQLException, InterruptedException {
        SingleCDCConnector singleCDCConnector = new SingleCDCConnector();
        singleCDCConnector.init("localhost",
                environment.getServicePort("canal-server_1", 11111),
                "nly-v1", "root", "xplat");

        cdcDaemonService = new CDCDaemonService();
        ReflectionTestUtils.setField(cdcDaemonService, "nodeIdGenerator", new StaticNodeIdGenerator(ZERO));
        ReflectionTestUtils.setField(cdcDaemonService, "consumerService", initConsumerService());
        ReflectionTestUtils.setField(cdcDaemonService, "cdcMetricsCallback", new TestCallbackService());
        ReflectionTestUtils.setField(cdcDaemonService, "cdcConnector", singleCDCConnector);

        cdcDaemonService.startDaemon();
    }

    @Test
    public void binlogSyncTest() {
        List<IEntity> syncList = new ArrayList<>();


    }

    protected ConsumerService initConsumerService() throws SQLException, InterruptedException {
        initIndex();

        ExecutorService consumerPool = new ThreadPoolExecutor(10, 10,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(2048),
                ExecutorHelper.buildNameThreadFactory("consumerThreads", true),
                new ThreadPoolExecutor.AbortPolicy());

        IEntityValueBuilder<String> entityValueBuilder = new SQLJsonIEntityValueBuilder();

        ConsumerService consumerService = new SphinxConsumerService();
        ReflectionTestUtils.setField(consumerService, "sphinxQLIndexStorage", indexStorage);
        ReflectionTestUtils.setField(consumerService, "consumerPool", consumerPool);
        ReflectionTestUtils.setField(consumerService, "entityValueBuilder", entityValueBuilder);

        return consumerService;
    }
}
