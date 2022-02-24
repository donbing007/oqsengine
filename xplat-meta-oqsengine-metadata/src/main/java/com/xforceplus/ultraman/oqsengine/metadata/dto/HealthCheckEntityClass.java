package com.xforceplus.ultraman.oqsengine.metadata.dto;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * 健康检查使用的固定元信息.
 *
 * @author xujia 2021/3/26
 * @since 1.8
 */
public class HealthCheckEntityClass implements IEntityClass {

    private static final long HEALTH_CHECK_ENTITY_ID = 0;

    private static final String HEALTH_CHECK_ENTITY_CODE = "healthCheck";

    private static HealthCheckEntityClass instance = new HealthCheckEntityClass();

    private HealthCheckEntityClass() {

    }

    public static HealthCheckEntityClass getInstance() {
        return instance;
    }


    @Override
    public long id() {
        return HEALTH_CHECK_ENTITY_ID;
    }

    @Override
    public String code() {
        return HEALTH_CHECK_ENTITY_CODE;
    }

    @Override
    public String appCode() {
        return "";
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public int version() {
        return 0;
    }

    @Override
    public int level() {
        return 0;
    }

    @Override
    public Collection<Relationship> relationship() {
        return Collections.emptyList();
    }

    @Override
    public Collection<IEntityClass> relationsEntityClasss() {
        return Collections.emptyList();
    }

    @Override
    public Optional<IEntityClass> father() {
        return Optional.empty();
    }

    @Override
    public Collection<IEntityClass> family() {
        return Collections.singletonList(this);
    }

    @Override
    public Collection<IEntityField> fields() {
        return Collections.emptyList();
    }

    @Override
    public Optional<IEntityField> field(String name) {
        return Optional.empty();
    }

    @Override
    public Optional<IEntityField> field(long id) {
        return Optional.empty();
    }
}
