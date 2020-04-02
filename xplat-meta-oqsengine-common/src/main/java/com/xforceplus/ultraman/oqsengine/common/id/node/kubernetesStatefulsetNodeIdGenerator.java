package com.xforceplus.ultraman.oqsengine.common.id.node;

/**
 * 依赖 Statefulset 生成的 pod hostname 最后的序号.
 *
 * @author dongbin
 * @version 0.1 2020/4/1 18:00
 * @since 1.8
 */
public class kubernetesStatefulsetNodeIdGenerator implements NodeIdGenerator {

    @Override
    public Integer next() {
        String hostName = System.getProperty("HOSTNAME");

        if (hostName == null) {
            throw new IllegalStateException("The HOSTNAME environment variable could not be found.");
        }

        String[] temp = hostName.split("-");

        return Integer.parseInt(temp[temp.length - 1]);
    }
}
