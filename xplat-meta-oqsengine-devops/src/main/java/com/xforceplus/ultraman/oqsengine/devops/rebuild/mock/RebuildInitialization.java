package com.xforceplus.ultraman.oqsengine.devops.rebuild.mock;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.SnowflakeLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.mock.BeanInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.common.mock.ReflectionUtils;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.DevOpsRebuildIndexExecutor;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.storage.SQLTaskStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.mock.IndexInitialization;
import com.xforceplus.ultraman.oqsengine.storage.master.mock.MasterDBInitialization;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public class RebuildInitialization implements BeanInitialization {

    private static volatile RebuildInitialization instance = null;

    private DevOpsRebuildIndexExecutor taskExecutor;
    private DataSource devOpsDataSource;
    private LongIdGenerator idGenerator;
    private static final String DEVOPS_TABLE_NAME = "devopstasks";

    private ExecutorService asyncThreadPool;

    private RebuildInitialization() {
    }

    /**
     * 获取单例.
     */
    public static RebuildInitialization getInstance() throws Exception {
        if (null == instance) {
            synchronized (RebuildInitialization.class) {
                if (null == instance) {
                    instance = new RebuildInitialization();
                    instance.init();
                    InitializationHelper.add(instance);
                }
            }
        }
        return instance;
    }

    @Override
    public void init() throws Exception {
        idGenerator = new SnowflakeLongIdGenerator(new StaticNodeIdGenerator(0));
        devOpsDataSource = buildDevOpsDataSource();

        SQLTaskStorage sqlTaskStorage = new SQLTaskStorage();
        Collection<Field> fields = ReflectionUtils.printAllMembers(sqlTaskStorage);
        ReflectionUtils.reflectionFieldValue(fields, "devOpsDataSource", sqlTaskStorage, devOpsDataSource);
        sqlTaskStorage.setTable(DEVOPS_TABLE_NAME);

        taskExecutor = new DevOpsRebuildIndexExecutor(10, 2048);
        Collection<Field> taskFields = ReflectionUtils.printAllMembers(taskExecutor);
        ReflectionUtils.reflectionFieldValue(taskFields, "indexStorage", taskExecutor,
            IndexInitialization.getInstance().getIndexStorage());
        ReflectionUtils.reflectionFieldValue(taskFields, "sqlTaskStorage", taskExecutor, sqlTaskStorage);
        ReflectionUtils.reflectionFieldValue(taskFields, "masterStorage", taskExecutor,
            MasterDBInitialization.getInstance().getMasterStorage());
        ReflectionUtils.reflectionFieldValue(taskFields, "idGenerator", taskExecutor, idGenerator);

        asyncThreadPool = new ThreadPoolExecutor(10, 10,
            0L,TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(1024 * 1000),
            ExecutorHelper.buildNameThreadFactory("task-threads", false));

        ReflectionUtils.reflectionFieldValue(taskFields, "asyncThreadPool", taskExecutor, asyncThreadPool);
    }


    @Override
    public void clear() throws Exception {
        try (Connection conn = devOpsDataSource.getConnection()) {
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("truncate table " + DEVOPS_TABLE_NAME);
            }
        } catch (Exception e) {
            //  ignore
        }
    }

    @Override
    public void destroy() throws Exception {
        taskExecutor.destroy();
        devOpsDataSource = null;
        idGenerator = null;
        if (null != asyncThreadPool) {
            ExecutorHelper.shutdownAndAwaitTermination(asyncThreadPool, 3600);
        }
        instance = null;
    }

    private DataSource buildDevOpsDataSource() throws IllegalAccessException {
        return CommonInitialization.getInstance().getDataSourcePackage(false).getDevOps();
    }

    private DataSource buildMasterDataSource() throws IllegalAccessException {
        return CommonInitialization.getInstance().getDataSourcePackage(false).getMaster().get(0);
    }

    public DevOpsRebuildIndexExecutor getTaskExecutor() {
        return taskExecutor;
    }

    public DataSource getDevOpsDataSource() {
        return devOpsDataSource;
    }

    public LongIdGenerator getIdGenerator() {
        return idGenerator;
    }
}
