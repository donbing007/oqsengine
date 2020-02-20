package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.interfaces.ICondition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.interfaces.IConditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.enums.Link;

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
