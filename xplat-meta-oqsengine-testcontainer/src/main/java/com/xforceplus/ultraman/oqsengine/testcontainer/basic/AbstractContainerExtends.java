package com.xforceplus.ultraman.oqsengine.testcontainer.basic;

import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.CanalContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.ManticoreContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.MysqlContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
@ExtendWith({RedisContainer.class, MysqlContainer.class, ManticoreContainer.class, CanalContainer.class})
public abstract class AbstractContainerExtends {
}
