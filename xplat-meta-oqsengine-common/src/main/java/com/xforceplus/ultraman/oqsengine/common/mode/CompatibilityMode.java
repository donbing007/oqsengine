package com.xforceplus.ultraman.oqsengine.common.mode;

/**
 * 判断是否为兼容模式.
 * 兼容模式用以兼容1.3之前版本的某些行为.
 *
 * @author dongbin
 * @version 0.1 2022/7/18 14:22
 * @since 1.8
 */
public class CompatibilityMode {

    private boolean compatibility;

    public CompatibilityMode(boolean compatibility) {
        this.compatibility = compatibility;
    }

    public boolean isCompatibility() {
        return compatibility;
    }
}
