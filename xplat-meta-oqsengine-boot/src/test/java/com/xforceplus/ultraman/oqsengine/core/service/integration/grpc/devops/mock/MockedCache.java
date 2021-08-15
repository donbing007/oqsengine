package com.xforceplus.ultraman.oqsengine.core.service.integration.grpc.devops.mock;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.metadata.cache.CacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;

/**
 * Created by justin.xu on 08/2021.
 *
 * @since 1.8
 */
public class MockedCache {

    private static CacheExecutor cacheExecutor;

    static {
        try {
            cacheExecutor = MetaInitialization.getInstance().getCacheExecutor();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void entityClassStorageSave(String expectedAppId,
        int expectedVersion) throws JsonProcessingException {

        List<EntityClassStorage> entityClassStorageList = new ArrayList<>();
        List<ExpectedEntityStorage> expectedEntityStorageList = new ArrayList<>();

        initEntityStorage(entityClassStorageList, expectedEntityStorageList);


        //  set storage
        if (!cacheExecutor.save(expectedAppId, expectedVersion, entityClassStorageList, new ArrayList<>())) {
            throw new RuntimeException("save error.");
        }
    }


    private static void initEntityStorage(List<EntityClassStorage> entityClassStorageList,
                                   List<ExpectedEntityStorage> expectedEntityStorageList) {
        /*
         * set self
         */
        ExpectedEntityStorage self =
            new ExpectedEntityStorage(5L, 10L, Arrays.asList(10L, 20L), Arrays.asList(10L));
        entityClassStorageList.add(GeneralEntityClassStorageBuilder.prepareEntity(self));
        expectedEntityStorageList.add(self);

        /*
         * set father
         */
        ExpectedEntityStorage father =
            new ExpectedEntityStorage(10L, 20L, Collections.singletonList(20L),
                Arrays.asList(20L));
        entityClassStorageList.add(GeneralEntityClassStorageBuilder.prepareEntity(father));
        expectedEntityStorageList.add(father);

        /*
         * set ancestor
         */
        ExpectedEntityStorage ancestor =
            new ExpectedEntityStorage(20L, null, null, null);
        entityClassStorageList.add(GeneralEntityClassStorageBuilder.prepareEntity(ancestor));
        expectedEntityStorageList.add(ancestor);

        /*
         * set son
         */
        ExpectedEntityStorage son =
            new ExpectedEntityStorage(4L, 5L, Arrays.asList(5L, 10L, 20L),
                Arrays.asList(5L, 20L));
        entityClassStorageList.add(GeneralEntityClassStorageBuilder.prepareEntity(son));
        expectedEntityStorageList.add(son);

        /*
         * set brother
         */
        ExpectedEntityStorage brother =
            new ExpectedEntityStorage(6L, 10L, Arrays.asList(10L, 20L),
                Arrays.asList(4L, 20L));
        entityClassStorageList.add(GeneralEntityClassStorageBuilder.prepareEntity(brother));
        expectedEntityStorageList.add(brother);
    }
}
