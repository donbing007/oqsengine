package com.xforceplus.ultraman.oqsengine.calculation.utils.infuence;

import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 一个影响树,表示目标源的改动造成的影响范围.
 *
 * @author dongbin
 * @version 0.1 2021/9/30 15:39
 * @since 1.8
 */
public class Infuence {

    private RootNode rootNode;
    private int size;

    public Infuence(IEntity entity, Participant participant, ValueChange change) {
        rootNode = new RootNode(entity, participant, change);
        size++;
    }

    public IEntity getSourceEntity() {
        return rootNode.getEntity();
    }

    public ValueChange getValueChange() {
        return rootNode.getChange();
    }

    /**
     * 增加影响.默认以根为传递者.
     *
     * @param participant 新的参与者.
     */
    public void impact(Participant participant) {
        impact(rootNode.getParticipant(), participant);
    }

    /**
     * 增加影响.
     *
     * @param parent      传递影响的参与者.
     * @param participant 新的参与者.
     * @return true 成功,false失败.
     */
    public boolean impact(Participant parent, Participant participant) {
        if (rootNode.getParticipant().equals(parent)) {
            insert(rootNode, participant);
            return true;
        }

        Optional<ChildNode> childOp = searchChild(parent);
        if (childOp.isPresent()) {
            ChildNode childNode = childOp.get();
            insert(childNode, participant);
            return true;
        }

        return false;
    }

    /**
     * 获取影响的大小. 这个大小包含了最初的触发者,所以最小数量一定为1.
     *
     * @return 影响大小.
     */
    public int getSize() {
        return size;
    }

    /**
     * 以广度优先的方式遍历整个影响树.
     *
     * @param consumer 对于每一个结点(不包含根结点)调用的消费实现.
     */
    public void scan(InfuenceConsumer consumer) {
        bfsIter((node, level) -> {
            if (RootNode.class.isInstance(node)) {
                RootNode rootNode = (RootNode) node;

                return consumer.accept(Optional.empty(), rootNode.getParticipant(), this);

            } else {

                ChildNode childNode = (ChildNode) node;
                Optional<Node> parentNode = childNode.getParent();

                if (!consumer.accept(
                    parentNode.isPresent() ? Optional.of(parentNode.get().getParticipant()) : Optional.empty(),
                    childNode.getParticipant(),
                    this
                )) {
                    return false;
                }

                return true;
            }
        });
    }

    @Override
    public String toString() {
        TreeSize treeSize = getTreeSize();
        Node[][] buff = new Node[size][treeSize.getHigh()];

        // 横轴
        int horizontal = 0;
        // 坚轴
        int vertical = 0;
        buff[vertical][horizontal] = rootNode;
        Deque<Node> stack = new LinkedList<>();
        rootNode.getChildren().forEach(c -> stack.push(c));

        while (!stack.isEmpty()) {
            Node node = stack.pop();

            // 找到父结点坐标.
            boolean found = false;
            for (int v = 0; v < buff.length; v++) {
                if (found) {
                    break;
                }
                for (int h = 0; h < buff[v].length; h++) {
                    if (node.getParent().get() == buff[v][h]) {
                        vertical = v;
                        horizontal = h;
                        found = true;
                        break;
                    }
                }
            }

            if (found) {

                vertical++;
                horizontal++;

                //从当前结点坐的 x 轴偏移一格开始找可用的 y 坐标.
                for (int v = vertical; v < buff.length; v++) {
                    if (buff[v][horizontal] != null) {
                        continue;
                    } else {
                        vertical = v;
                        break;
                    }
                }

                buff[vertical][horizontal - 1] = LevelNode.getInstance();
                buff[vertical][horizontal] = node;

            } else {
                throw new IllegalArgumentException();
            }

            node.getChildren().forEach(c -> stack.push(c));
        }

        final StringBuilder sb = new StringBuilder();
        for (int v = 0; v < buff.length; v++) {
            for (int h = 0; h < buff[h].length; h++) {
                if (buff[v][h] == null) {
                    sb.append("  ");
                } else if (buff[v][h] == LevelNode.getInstance()) {
                    sb.append(" |- ");
                } else {
                    sb.append(buff[v][h].toString());
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    // 插入影响
    private void insert(Node point, Participant participant) {
        for (Node n : point.getChildren()) {
            ChildNode c = (ChildNode) n;
            if (c.getParticipant().equals(participant)) {
                // 这里找到表示已经存在.
                return;
            }
        }

        point.addChild(new ChildNode(participant));
        size++;
    }

    // 搜索子类结点.
    private Optional<ChildNode> searchChild(Participant participant) {

        AtomicReference<ChildNode> ref = new AtomicReference<>();
        bfsIter((node, level) -> {
            if (RootNode.class.isInstance(node)) {
                return true;
            } else {

                ChildNode childNode = (ChildNode) node;
                if (childNode.getParticipant().equals(participant)) {

                    ref.set((ChildNode) node);

                    return false;

                } else {
                    return true;
                }
            }
        });

        return Optional.ofNullable(ref.get());
    }

    // 广度优先方式迭代.
    private void bfsIter(BfsIterNodeConsumer bfsIterNodeConsumer) {
        Queue<Node> stack = new LinkedList<>();
        int level = 0;
        stack.add(rootNode);
        stack.add(LevelNode.getInstance());
        Node node;
        while (!stack.isEmpty()) {
            node = stack.poll();

            if (node == LevelNode.getInstance()) {

                level++;
                if (!stack.isEmpty()) {
                    // 判断是否最后一个层分隔结点.
                    stack.add(LevelNode.getInstance());
                }

            } else {

                if (!bfsIterNodeConsumer.consumer(node, level)) {
                    return;
                }

                node.getChildren().forEach(n -> stack.add(n));
            }
        }
    }

    @FunctionalInterface
    private interface BfsIterNodeConsumer {

        boolean consumer(Node node, int level);
    }

    // 序号
    private TreeSize getTreeSize() {
        AtomicInteger maxWide = new AtomicInteger(0);
        AtomicInteger maxHigh = new AtomicInteger(0);

        AtomicInteger currentLevel = new AtomicInteger(0);
        AtomicInteger currentWide = new AtomicInteger(1);
        bfsIter((node, level) -> {
            if (level == currentLevel.get()) {
                currentWide.incrementAndGet();
            } else {

                maxHigh.incrementAndGet();

                if (currentWide.get() > maxWide.get()) {
                    maxWide.set(currentWide.get());
                }
            }

            return true;
        });

        return new TreeSize(maxWide.get(), maxHigh.get());
    }

    private static class TreeSize {
        private int wide;
        private int high;

        public TreeSize(int wide, int high) {
            this.wide = wide;
            this.high = high;
        }

        public int getWide() {
            return wide;
        }

        public int getHigh() {
            return high;
        }
    }


    // 树的结点.
    private static class Node {
        private Participant participant;
        private Node parent;
        private List<Node> children;

        public Node(Participant participant) {
            this.participant = participant;
        }

        public Optional<Node> getParent() {
            return Optional.ofNullable(parent);
        }

        public Participant getParticipant() {
            return participant;
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

        public void addChild(Node child) {
            if (this.children == null) {
                this.children = new LinkedList<>();
            }

            child.setParent(this);
            this.children.add(child);
        }
    }

    private static class RootNode extends Node {
        // 触发影响的实例.
        private IEntity entity;
        // 触发影响的实例字段值.
        private ValueChange change;

        public RootNode(IEntity entity, Participant participant, ValueChange change) {
            super(participant);
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
                ((ChildNode) node).getParticipant().getEntityClass().id() == entityClass.id()
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
            RootNode otherRootNode = (RootNode) o;
            return otherRootNode.getEntity().id() == otherRootNode.getEntity().id();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();

            sb.append("(")
                .append(getParticipant().getEntityClass().code())
                .append(",")
                .append(getChange().getField().name())
                .append(")");

            return sb.toString();
        }
    }

    private static class ChildNode extends Node implements Comparable<ChildNode> {

        public ChildNode(Participant participant) {
            super(participant);
        }

        @Override
        public int compareTo(ChildNode o) {
            long ownId = getParticipant().getEntityClass().id();
            long otherId = o.getParticipant().getEntityClass().id();

            if (ownId < otherId) {
                return -1;
            } else if (ownId > otherId) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();

            sb.append("(")
                .append(getParticipant().getEntityClass().code())
                .append(",")
                .append(getParticipant().getField().name())
                .append(")");

            return sb.toString();
        }
    }

    // 这是一个表示层结束的结点.
    private static class LevelNode extends Node {

        public static Node INSTANCE = new LevelNode();

        public static Node getInstance() {
            return INSTANCE;
        }

        public LevelNode() {
            super(null);
        }
    }
}
