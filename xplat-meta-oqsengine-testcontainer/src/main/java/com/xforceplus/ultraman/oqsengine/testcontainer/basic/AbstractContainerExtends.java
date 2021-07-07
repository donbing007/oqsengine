package com.xforceplus.ultraman.oqsengine.testcontainer.basic;

import com.xforceplus.ultraman.test.tools.core.container.basic.CannalContainer;
import com.xforceplus.ultraman.test.tools.core.container.basic.ManticoreSearchContainer;
import com.xforceplus.ultraman.test.tools.core.container.basic.ManticoreWriteContainer;
import com.xforceplus.ultraman.test.tools.core.container.basic.ManticoreWriteSubContainer;
import com.xforceplus.ultraman.test.tools.core.container.basic.MysqlContainer;
import com.xforceplus.ultraman.test.tools.core.container.basic.RedisContainer;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
@ExtendWith({RedisContainer.class, MysqlContainer.class, CannalContainer.class, ManticoreWriteContainer.class, ManticoreWriteSubContainer.class,
    ManticoreSearchContainer.class})
public abstract class AbstractContainerExtends {
}
