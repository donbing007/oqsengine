package com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.infuence;

import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.CalculationParticipant;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Infuence;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InfuenceConsumer;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Participant;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Lookup;
import java.util.Collection;
import java.util.Optional;

/**
 * lookup 迭代消费器.
 *
 * @author dongbin
 * @version 0.1 2022/3/2 20:36
 * @since 1.8
 */
public class LookupInfuenceConsumer implements InfuenceConsumer {
    @Override
    public Action accept(Optional<Participant> parentParticipantOp, Participant participant, Infuence infuenceInner) {
        IEntityClass participantClass = participant.getEntityClass();
        IEntityField participantField = participant.getField();

        for (Relationship r : participantClass.relationship()) {
            // 应该包含所有定制的元信息.
            Collection<IEntityClass> relationshipClasss = r.getRightFamilyEntityClasses();

            for (IEntityClass relationshipClass : relationshipClasss) {
                relationshipClass.fields().stream()
                    .filter(f -> f.calculationType() == CalculationType.LOOKUP)
                    .filter(f -> ((Lookup) f.config().getCalculation()).getFieldId() == participantField.id())
                    .forEach(f -> {
                        infuenceInner.impact(
                            participant,
                            CalculationParticipant.Builder.anParticipant()
                                .withEntityClass(relationshipClass)
                                .withField(f)
                                .withAttachment(r.isStrong())
                                .build()
                        );
                    });
            }
        }

        return InfuenceConsumer.Action.CONTINUE;
    }
}
