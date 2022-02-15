package com.xforceplus.ultraman.oqsengine.metadata.integeration.remote;

import static com.xforceplus.ultraman.oqsengine.metadata.Constant.TEST_APP_ID;
import static com.xforceplus.ultraman.oqsengine.metadata.Constant.TEST_ENTITY_CLASS_ID;
import static com.xforceplus.ultraman.oqsengine.metadata.Constant.TEST_ENV;

import com.xforceplus.ultraman.oqsengine.metadata.Constant;
import com.xforceplus.ultraman.oqsengine.metadata.MockerRequestClientHelper;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * test.
 *
 * @author j.xu
 * @version 0.1 2021/05/2021/5/17
 * @since 1.8
 */
@Disabled
public class SyncReadTest extends MockerRequestClientHelper {

    @BeforeEach
    public void before() throws Exception {
        init(false);
    }

    @AfterEach
    public void after() throws Exception {
        destroy();
    }

    @Test
    public void testGetFormula() throws Exception {
        try {
            MetaInitialization.getInstance().getMetaManager().need(TEST_APP_ID, TEST_ENV);
        } catch (Exception e) {

        }
        int count = 0;
        Optional<IEntityClass> entityClassOptional = null;
        while (count < 500) {
            entityClassOptional = MetaInitialization.getInstance().getMetaManager().load(TEST_ENTITY_CLASS_ID, "");
            if (entityClassOptional.isPresent()) {
                break;
            }
            count++;
            Thread.sleep(1_000);
        }
        Assertions.assertTrue(entityClassOptional.isPresent());

        IEntityClass iEntityClass = entityClassOptional.get();

        Assertions.assertEquals(TEST_ENTITY_CLASS_ID, iEntityClass.id());
    }
}

