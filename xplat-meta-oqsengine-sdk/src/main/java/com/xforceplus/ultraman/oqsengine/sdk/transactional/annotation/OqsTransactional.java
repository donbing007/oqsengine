package com.xforceplus.ultraman.oqsengine.sdk.transactional.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.xforceplus.ultraman.oqsengine.sdk.transactional.annotation.TransactionDefinition.TIMEOUT_DEFAULT;

/**
 *
 */
@Target({ ElementType.METHOD })
@Retention( RetentionPolicy.RUNTIME)
public @interface OqsTransactional {

    Propagation propagation() default Propagation.REQUIRED;


    int timeout() default TIMEOUT_DEFAULT;

    Class<? extends Throwable>[] rollbackFor() default {};

    Class<? extends Throwable>[] noRollbackFor() default {};

}
