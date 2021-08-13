package com.xforceplus.ultraman.oqsengine.changelog.config;

import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeValue;
import com.xforceplus.ultraman.oqsengine.changelog.domain.Changelog;
import com.xforceplus.ultraman.oqsengine.changelog.utils.ChangelogHelper;
import com.xforceplus.ultraman.oqsengine.common.id.IdGenerator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeValue.Op.SET;

/**
 * example has multi
 */
public class ChangelogExample {

    public IEntityClass A;

    public IEntityClass B;

    public IEntityClass C;

    public IEntityClass E;

    public IEntityClass F;

    public Map<Long, IEntityClass> idMapping = new HashMap<>();

    public List<Changelog> changelogs = new LinkedList<>();

    public final static Long A_Class = 1L;
    public final static Long A_ObjId = 1000001L;
    public final static Long A_Field1 = 1001L;
    public final static Long A_Field2 = 1002L;

    public final static Long A_B_OTO = 100002L;

    public final static Long B_Class = 2L;
    public final static Long B_ObjId = 2000001L;
    public final static Long B_Field1 = 2001L;
    public final static Long B_Field2 = 2002L;

    private IdGenerator<Long> versionIdGenerator;

    Random random = new Random();

    public ChangelogExample(IdGenerator<Long> versionIdGenerator) {
        this.versionIdGenerator = versionIdGenerator;
        A = EntityClass.Builder.anEntityClass()
                .withId(A_Class)
                .withCode("A")
                .withRelations(
                        Arrays.asList(
                                Relationship
                                        .Builder
                                        .anOqsRelation()
                                        .withRelationType(Relationship.RelationType.ONE_TO_ONE)
                                        .withCode("A_B")
                                        .withRightEntityClassId(B_Class)
                                        .withId(A_B_OTO)
                                        .withIdentity(true)
                                        .withLeftEntityClassId(A_Class)
                                        .withStrong(true)
                                        .withBelongToOwner(true)
                                        .withEntityField(EntityField.Builder.anEntityField()
                                                .withId(A_B_OTO)
                                                .withFieldType(FieldType.LONG)
                                                .build())
                                        .build()
                        )
                )
                .withFields(
                        Arrays.asList(
                                EntityField
                                        .Builder.anEntityField()
                                        .withFieldType(FieldType.STRING)
                                        .withId(A_Field1)
                                        .build(),
                                EntityField
                                        .Builder.anEntityField()
                                        .withFieldType(FieldType.DATETIME)
                                        .withId(A_Field2)
                                        .build()
                        ))
                .build();

        B = EntityClass.Builder.anEntityClass()
                .withId(B_Class)
                .withCode("B")
                .withFields(
                        Arrays.asList(
                                EntityField
                                        .Builder.anEntityField()
                                        .withFieldType(FieldType.STRING)
                                        .withId(B_Field1)
                                        .build()
                        ))
                .build();


        idMapping.put(1L, A);
        idMapping.put(2L, B);

//        build(10, changelogs);
        buildFix(changelogs);
    }


    public void build(int changelogSize, List<Changelog> changelogs){
        Random random = new Random();
        for(int i = 0 ; i < changelogSize ; i ++ ){
            int result = random.nextInt(4);
            if(result == 0){
                changelogs.add(genBChangelog());
            } else if(result == 1){
                changelogs.add(genAChangelog());
            } else if(result == 2){
                changelogs.add(addRelABChangelog());
            } else if(result == 3){
                changelogs.add(addRelABChangelog());
            }
        }
    }

    public void buildFix(List<Changelog> changelogs){
        changelogs.add(genAChangelog());
        changelogs.add(addRelABChangelog());
    }


    public IEntityClass getEntityClassById(Long id) {
        return idMapping.get(id);
    }

    public List<Changelog> getChangelogByIdVersion(long id, long version) {
        if (version > 0) {
            return changelogs.stream().filter(x -> x.getId() == id && x.getVersion() <= version)
                    .sorted((o1, o2) -> Long.compare(o2.getVersion(), o1.getVersion())).collect(Collectors.toList());
        } else {
            return changelogs.stream().filter(x -> x.getId() == id)
                    .sorted((o1, o2) -> Long.compare(o2.getVersion(), o1.getVersion())).collect(Collectors.toList());
        }
    }

    public Changelog addRelABChangelog() {
        Changelog changelog = new Changelog();
        changelog.setId(A_ObjId);
        changelog.setEntityClass(A_Class);
        changelog.setVersion(versionIdGenerator.next());
        changelog.setCreateTime(new DateTimeValue(null, LocalDateTime.now()).valueToLong());

        List<ChangeValue> changeValues = new LinkedList<>();
        ChangeValue changeValue1 = new ChangeValue();
        changeValue1.setRawValue(B_ObjId.toString());
        changeValue1.setOp(SET);
        changeValue1.setFieldId(A_B_OTO);
        changeValues.add(changeValue1);
        changelog.setChangeValues(changeValues);
        changelog.setComment("建立AB关联");

        return changelog;
    }

    public Changelog removeRelABChangelog() {
        Changelog changelog = new Changelog();
        changelog.setId(A_ObjId);
        changelog.setEntityClass(A_Class);
        changelog.setVersion(versionIdGenerator.next());
        changelog.setCreateTime(new DateTimeValue(null, LocalDateTime.now()).valueToLong());

        List<ChangeValue> changeValues = new LinkedList<>();
        ChangeValue changeValue1 = new ChangeValue();
        changeValue1.setRawValue(null);
        changeValue1.setOp(SET);
        changeValue1.setFieldId(A_B_OTO);
        changelog.setChangeValues(changeValues);

        changeValues.add(changeValue1);

        changelog.setComment("删除AB关联");

        return changelog;
    }

    public Changelog genAChangelog() {
        Changelog changelog = new Changelog();
        changelog.setId(A_ObjId);
        changelog.setCreateTime(new DateTimeValue(null, LocalDateTime.now()).valueToLong());
        changelog.setVersion(versionIdGenerator.next());
        changelog.setEntityClass(A_Class);

        List<ChangeValue> changeValues = new LinkedList<>();

        ChangeValue changeValue1 = new ChangeValue();
        changeValue1.setRawValue(Integer.toString(random.nextInt(1000)));
        changeValue1.setOp(SET);
        changeValue1.setFieldId(A_Field1);

        ChangeValue changeValue2 = new ChangeValue();
        changeValue2.setRawValue(ChangelogHelper.serialize(new DateTimeValue(null, LocalDateTime.now())));
        changeValue2.setOp(SET);
        changeValue2.setFieldId(A_Field2);

        changeValues.add(changeValue1);
        changeValues.add(changeValue2);
        changelog.setChangeValues(changeValues);
        changelog.setComment("随机修改A");

        //System.out.println("Gen Changelog A:" + changelog);

        return changelog;
    }

    public Changelog genBChangelog() {
        Changelog changelog = new Changelog();
        changelog.setId(B_ObjId);
        changelog.setCreateTime(new DateTimeValue(null, LocalDateTime.now()).valueToLong());
        changelog.setVersion(versionIdGenerator.next());
        changelog.setEntityClass(B_Class);

        List<ChangeValue> changeValues = new LinkedList<>();

        ChangeValue changeValue1 = new ChangeValue();
        changeValue1.setRawValue(Integer.toString(random.nextInt(1000)));
        changeValue1.setOp(SET);
        changeValue1.setFieldId(B_Field1);

        changeValues.add(changeValue1);
        changelog.setChangeValues(changeValues);
        changelog.setComment("随机修改B");

        //System.out.println("Gen Changelog B:" + changelog);

        return changelog;
    }
}
