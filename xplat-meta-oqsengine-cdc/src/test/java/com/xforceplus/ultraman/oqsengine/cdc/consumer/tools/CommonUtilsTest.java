package com.xforceplus.ultraman.oqsengine.cdc.consumer.tools;

import static com.xforceplus.ultraman.oqsengine.cdc.testhelp.meta.EntityClassBuilder.ENTITY_CLASS_2;
import static com.xforceplus.ultraman.oqsengine.cdc.testhelp.meta.EntityClassBuilder.ENTITY_CLASS_STATIC;

import com.xforceplus.ultraman.oqsengine.cdc.context.ParserContext;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.AbstractCdcHelper;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.master.mock.MasterDBInitialization;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class CommonUtilsTest extends AbstractCdcHelper {

    private ParserContext parserContext;
    @BeforeEach
    public void before() throws Exception {
        super.init(false, null);

        parserContext = new ParserContext(-1, true, new CDCMetrics(), MetaInitialization.getInstance().getMetaManager(),
            MasterDBInitialization.getInstance().getMasterStorage());
    }

    @AfterEach
    public void after() throws Exception {
        super.clear(false);
    }

    @Test
    public void getEntityClassTest() throws SQLException {
        //  动态
        //  self
        IEntityClass entityClass =
            CommonUtils.getEntityClass(ENTITY_CLASS_2.ref(), parserContext);

        Assertions.assertNotNull(entityClass);
        check(ENTITY_CLASS_2, entityClass);

        //  father
        IEntityClass expectedFather = ENTITY_CLASS_2.father().get();
        IEntityClass actualFather = entityClass.father().get();
        check(expectedFather, actualFather);

        //  grandpa
        expectedFather = expectedFather.father().get();
        actualFather = actualFather.father().get();
        check(expectedFather, actualFather);

        //  静态.
        IEntityClass staticEntityClass =
            CommonUtils.getEntityClass(ENTITY_CLASS_STATIC.ref(), parserContext);
        check(ENTITY_CLASS_STATIC, staticEntityClass);
    }

    private void check(IEntityClass expected, IEntityClass actual) {
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected.id(), actual.id());
        Assertions.assertEquals(expected.ref().getCode(), actual.ref().getCode());
        Assertions.assertEquals(expected.type(), actual.type());
    }
}
