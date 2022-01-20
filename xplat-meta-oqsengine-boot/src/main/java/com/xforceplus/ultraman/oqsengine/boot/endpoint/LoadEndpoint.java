package com.xforceplus.ultraman.oqsengine.boot.endpoint;

import com.xforceplus.ultraman.oqsengine.common.load.SystemLoadEvaluator;
import javax.annotation.Resource;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

/**
 * 自定义的负载端点.
 *
 * @author dongbin
 * @version 0.1 2022/1/17 18:00
 * @since 1.8
 */
@Component
@Endpoint(id = "load")
public class LoadEndpoint {

    @Resource
    private SystemLoadEvaluator loadEvaluator;

    @ReadOperation
    public double enpointByGet() {
        return loadEvaluator.evaluate();
    }
}
