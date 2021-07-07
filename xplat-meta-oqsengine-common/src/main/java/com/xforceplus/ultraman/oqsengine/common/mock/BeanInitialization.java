package com.xforceplus.ultraman.oqsengine.common.mock;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
public interface BeanInitialization {

    /**
     * 初始化.
     */
    void init() throws Exception;

    /**
     * 销毁.
     */
    void destroy() throws Exception;
}
