package com.xforceplus.ultraman.oqsengine.core.conditions;

import com.xforceplus.ultraman.oqsengine.core.conditions.interfaces.ICondition;
import com.xforceplus.ultraman.oqsengine.core.conditions.interfaces.IConditions;
import com.xforceplus.ultraman.oqsengine.core.enums.Link;

import java.util.List;

public class Conditions implements IConditions {

    private List<ICondition> conditions;

    @Override
    public void addCondition(Link link, ICondition condition) {

    }

    @Override
    public void addConditions(Link link, IConditions conditions) {

    }
}
