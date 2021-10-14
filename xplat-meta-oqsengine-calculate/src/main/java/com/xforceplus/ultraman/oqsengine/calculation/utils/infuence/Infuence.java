package com.xforceplus.ultraman.oqsengine.calculation.utils.infuence;

import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * 一个影响树,表示目标源的改动造成的影响范围.
 *
 * @author dongbin
 * @version 0.1 2021/9/30 15:39
 * @since 1.8
 */
public class Infuence {

    private Root root;

    public Infuence(IEntity entity, IEntityClass entityClass, ValueChange change) {
        root = new Root(entity, entityClass, change);
    }

    public IEntity getSourceEntity() {
        return root.getEntity();
    }

    public ValueChange getValueChange() {
        return root.getChange();
    }

    /**
     * 增加影响.默认以根为传递者.
     *
     * @param entityClass 受影响的实例元信息.
     * @param field       受影响的实例字段信息.
     */
    public void impact(IEntityClass entityClass, IEntityField field) {
        impact(root.getEntityClass(), entityClass, field);
    }

    /**
     * 增加影响.
     *
     * @param parentClass 传递影响给当前的元信息.
     * @param entityClass 当前被影响的元信息.
     * @param field       当前被影响的字段.
     * @return true 成功,false失败.
     */
    public boolean impact(IEntityClass parentClass, IEntityClass entityClass, IEntityField field) {
        if (root.getEntity().entityClassRef().getId() == parentClass.id()) {
            insert(root, entityClass, field);
            return true;
        }

        Optional<Child> childOp = searchChild(parentClass);
        if (childOp.isPresent()) {
            Child child = childOp.get();
            insert(child, entityClass, field);
            return true;
        }

        return false;
    }

    /**
     * 以广度优先的方式遍历整个影响树.
     *
     * @param consumer 对于每一个结点(不包含根结点)调用的消费实现.
     */
    public void scan(InfuenceConsumer consumer) {
        bfsIter(node -> {
            if (Root.class.isInstance(node)) {
                Root rootNode = (Root) node;

                return consumer.accept(
                    Optional.empty(),
                    new Participant(rootNode.getEntityClass(), rootNode.getChange().getField(),
                        rootNode.getAttachment()),
                    this);

            } else {

                Child childNode = (Child) node;

                Node parent = childNode.getParent();
                IEntityClass parentClass;
                if (Root.class.isInstance(parent)) {
                    parentClass = parent.getEntityClass();
                } else {
                    parentClass = parent.getEntityClass();
                }

                for (IEntityField field : childNode.getFields()) {
                    if (!consumer.accept(
                        Optional.ofNullable(parentClass),
                        new Participant(childNode.getEntityClass(), field, childNode.getAttachment()),
                        this)) {
                        return false;
                    }
                }

                return true;
            }
        });
    }

    // 插入影响
    private void insert(Node point, IEntityClass entityClass, IEntityField field) {
        for (Node n : point.getChildren()) {
            Child c = (Child) n;
            if (c.getEntityClass().id() == entityClass.id()) {
                c.addField(field);
                return;
            }
        }

        Child newChild = new Child(entityClass);
        newChild.addField(field);
        point.addChild(newChild);
    }

    // 搜索子类结点.
    private Optional<Child> searchChild(IEntityClass targetEntityClass) {

        AtomicReference<Child> ref = new AtomicReference<>();
        bfsIter(node -> {
            if (Root.class.isInstance(node)) {
                return true;
            }

            if (node.getEntityClass().id() == targetEntityClass.id()) {
                ref.set((Child) node);
                return false;
            } else {
                return true;
            }
        });

        return Optional.ofNullable(ref.get());
    }

    // 广度优先方式迭代.
    private void bfsIter(Function<Node, Boolean> nodeConsumer) {
        Queue<Node> stack = new LinkedList<>();
        stack.add(root);
        Node node;
        while (!stack.isEmpty()) {
            node = stack.poll();

            if (!nodeConsumer.apply(node)) {
                return;
            }

            node.getChildren().forEach(n -> stack.add(n));
        }
    }

    // 树的结点.
    private static class Node {
        private Object attachment;
        private IEntityClass entityClass;
        private Node parent;
        private List<Node> children;

        public Node(IEntityClass entityClass) {
            this.entityClass = entityClass;
        }

        public Node getParent() {
            return parent;
        }

        public IEntityClass getEntityClass() {
            return entityClass;
        }

        public List<Node> getChildren() {
            if (children == null) {
                return Collections.emptyList();
            } else {
                return children;
            }
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public Object getAttachment() {
            return attachment;
        }

        public void setAttachment(Object attachment) {
            this.attachment = attachment;
        }

        public void addChild(Node child) {
            if (this.children == null) {
                this.children = new LinkedList<>();
            }

            child.setParent(this);
            this.children.add(child);
        }
    }

    private static class Root extends Node {
        // 触发影响的实例.
        private IEntity entity;
        // 触发影响的实例字段值.
        private ValueChange change;

        public Root(IEntity entity, IEntityClass entityClass, ValueChange change) {
            super(entityClass);
            this.entity = entity;
            this.change = change;
        }

        public IEntity getEntity() {
            return entity;
        }

        public ValueChange getChange() {
            return change;
        }

        public Optional<Node> getChild(IEntityClass entityClass) {
            return getChildren().stream().filter(node ->
                ((Child) node).getEntityClass().id() == entityClass.id()
            ).findFirst();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Root otherRoot = (Root) o;
            return otherRoot.getEntity().id() == otherRoot.getEntity().id();
        }
    }

    private static class Child extends Node implements Comparable<Child> {
        // 被影响的字段列表.
        private List<IEntityField> fields;

        public Child(IEntityClass entityClass) {
            super(entityClass);
        }

        public void addField(IEntityField field) {
            if (fields == null) {
                this.fields = new LinkedList<>();
            }
            fields.add(field);
        }

        public List<IEntityField> getFields() {
            if (fields == null) {
                return Collections.emptyList();
            }
            return fields;
        }

        @Override
        public int compareTo(Child o) {
            long ownId = getEntityClass().id();
            long otherId = o.getEntityClass().id();

            if (ownId < otherId) {
                return -1;
            } else if (ownId > otherId) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
