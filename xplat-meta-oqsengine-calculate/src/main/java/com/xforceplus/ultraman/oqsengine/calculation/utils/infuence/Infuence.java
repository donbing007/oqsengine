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

    /**
     * 影响树构造器.
     *
     * @param entity 实体.
     * @param abstractParticipant 参与者.
     * @param change 变更.
     */
    public Infuence(IEntity entity, AbstractParticipant abstractParticipant, ValueChange change) {
        rootNode = new RootNode(entity, abstractParticipant, change);
        abstractParticipant.setNode(rootNode);
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
     * @param abstractParticipant 新的参与者.
     */
    public void impact(AbstractParticipant abstractParticipant) {
        impact(rootNode.getParticipant(), abstractParticipant);
    }

    /**
     * 增加影响.
     *
     * @param parent      传递影响的参与者.
     * @param abstractParticipant 新的参与者.
     * @return true 成功,false失败.
     */
    public boolean impact(AbstractParticipant parent, AbstractParticipant abstractParticipant) {
        if (rootNode.getParticipant().equals(parent)) {
            insert(rootNode, abstractParticipant);
            return true;
        }

        Optional<ChildNode> childOp = searchChild(parent);
        if (childOp.isPresent()) {
            ChildNode childNode = childOp.get();
            insert(childNode, abstractParticipant);
            return true;
        }

        return false;
    }

    /**
     * 删除参与者影响列表中指定参与者.
     */
    public boolean remove(AbstractParticipant parent, AbstractParticipant abstractParticipant) {
        if (parent.equals(rootNode.getParticipant())) {
            remove(rootNode, abstractParticipant);
            return true;
        }
        Optional<ChildNode> childOp = searchChild(parent);
        if (childOp.isPresent()) {
            ChildNode childNode = childOp.get();
            remove(childNode, abstractParticipant);
            return true;
        }
        return false;
    }

    /**
     * 指定节点删除指定参与者.
     */
    private void remove(Node point, AbstractParticipant abstractParticipant) {
        for (Node n : point.getChildren()) {
            ChildNode c = (ChildNode) n;
            if (c.getParticipant().equals(abstractParticipant)) {
                // 这里找到表示已经存在.
                point.getChildren().remove(c);
                this.size--;
                return;
            }
        }
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
     * 判断是否为空影响树. 空影响表示发起源影响力没有任何作用.
     *
     * @return true 空, false 非空.
     */
    public boolean empty() {
        // 只有一个根结点.
        final int onlyRoot = 1;
        return getSize() == onlyRoot;
    }

    /**
     * 以广度优先的方式遍历整个影响树.
     *
     * @param consumer 对于每一个结点调用的消费实现.
     */
    public void scan(InfuenceConsumer consumer) {
        scan(consumer, rootNode.getParticipant());
    }


    /**
     * 指定参与者所在节点开始遍历.
     */
    public void scan(InfuenceConsumer consumer, AbstractParticipant abstractParticipant) {
        bfsIter((node, level) -> {
            if (RootNode.class.isInstance(node)) {
                RootNode rootNode = (RootNode) node;

                return consumer.accept(Optional.empty(), rootNode.getParticipant(), this);

            } else {

                ChildNode childNode = (ChildNode) node;
                Optional<Node> parentNode = childNode.getParent();

                return consumer.accept(
                        parentNode.isPresent() ? Optional.of(parentNode.get().getParticipant()) : Optional.empty(),
                        childNode.getParticipant(),
                        this
                );
            }
        }, abstractParticipant);
    }

    @Override
    public String toString() {
        if (empty()) {
            return rootNode.toString();
        }

        TreeSize treeSize = getTreeSize();
        /*
        构造一个结点buff二维数组,行数量等于有效结点数量,列数量等于树的高度.
               A
             /   \
            B     C
            这样的树目标是构造出如下的矩阵.

             A ·
             L B
             L C

             L 表示连接线.
         */
        Node[][] buff = new Node[size][treeSize.getHigh() + 1];

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
                        // 设定子元素的坐标.
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
        sb.append('\n');
        for (int v = 0; v < buff.length; v++) {
            for (int h = 0; h < buff[v].length; h++) {
                if (buff[v][h] == null) {
                    sb.append("··");
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
    private void insert(Node point, AbstractParticipant abstractParticipant) {
        for (Node n : point.getChildren()) {
            ChildNode c = (ChildNode) n;
            if (c.getParticipant().equals(abstractParticipant)) {
                // 这里找到表示已经存在.
                return;
            }
        }
        ChildNode childNode = new ChildNode(abstractParticipant);
        point.addChild(childNode);
        childNode.setLevel(point.getLevel() + 1);
        abstractParticipant.setNode(childNode);
        abstractParticipant.setPre(point.abstractParticipant);

        size++;
    }

    public boolean contains(AbstractParticipant abstractParticipant) {
        return searchChild(abstractParticipant).isPresent() || rootNode.getParticipant().equals(abstractParticipant);
    }

    // 搜索子类结点.
    private Optional<ChildNode> searchChild(AbstractParticipant abstractParticipant) {

        AtomicReference<ChildNode> ref = new AtomicReference<>();
        bfsIter((node, level) -> {
            if (RootNode.class.isInstance(node)) {
                return InfuenceConsumer.Action.CONTINUE;
            } else {

                ChildNode childNode = (ChildNode) node;
                if (childNode.getParticipant().equals(abstractParticipant)) {

                    ref.set((ChildNode) node);

                    return InfuenceConsumer.Action.OVER;

                } else {
                    return InfuenceConsumer.Action.CONTINUE;
                }
            }
        }, rootNode.getParticipant());

        return Optional.ofNullable(ref.get());
    }

    // 广度优先方式迭代.
    private void bfsIter(BfsIterNodeConsumer bfsIterNodeConsumer, AbstractParticipant abstractParticipant) {
        Queue<Node> stack = new LinkedList<>();
        int level = 0;
        stack.add(abstractParticipant.getNode());
        stack.add(LevelNode.getInstance());
        Node node;
        InfuenceConsumer.Action action;
        while (!stack.isEmpty()) {
            node = stack.poll();

            if (node == LevelNode.getInstance()) {

                level++;
                if (!stack.isEmpty()) {
                    // 判断是否最后一个层分隔结点.
                    stack.add(LevelNode.getInstance());
                }

            } else {

                action = bfsIterNodeConsumer.consumer(node, level);
                switch (action) {
                    case CONTINUE: {
                        node.getChildren().forEach(n -> stack.add(n));
                        break;
                    }
                    case OVER: {
                        return;
                    }
                    case OVER_SELF: {
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Error action.");
                    }
                }
            }
        }
    }

    @FunctionalInterface
    private interface BfsIterNodeConsumer {

        InfuenceConsumer.Action consumer(Node node, int level);
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

            return InfuenceConsumer.Action.CONTINUE;
        }, rootNode.getParticipant());

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

    /**
     * 树节点.
     */
    protected static class Node {
        private AbstractParticipant abstractParticipant;
        private Node parent;
        private List<Node> children;
        private int level;

        public Node(AbstractParticipant abstractParticipant) {
            this.abstractParticipant = abstractParticipant;
        }

        public Optional<Node> getParent() {
            return Optional.ofNullable(parent);
        }

        public AbstractParticipant getParticipant() {
            return abstractParticipant;
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

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public void addChild(Node child) {
            if (this.children == null) {
                this.children = new LinkedList<>();
            }

            child.setParent(this);
            this.children.add(child);
        }

        public void removeChild(Node child) {
            if (this.children == null) {
                return;
            }
            children.remove(child);
        }
    }

    /**
     * 根节点.
     */
    protected static class RootNode extends Node {
        // 触发影响的实例.
        private IEntity entity;
        // 触发影响的实例字段值.
        private ValueChange change;

        public RootNode(IEntity entity, AbstractParticipant abstractParticipant, ValueChange change) {
            super(abstractParticipant);
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

            AbstractParticipant abstractParticipant = getParticipant();
            sb.append("(")
                .append(abstractParticipant.getEntityClass().code())
                .append(",")
                .append(getChange() == null ? this.getParticipant().getField().name() : getChange().getField().name())
                .append(")");

            return sb.toString();
        }
    }

    /**
     * 孩子节点.
     */
    protected static class ChildNode extends Node implements Comparable<ChildNode> {

        public ChildNode(AbstractParticipant abstractParticipant) {
            super(abstractParticipant);
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


    /**
     * 这是一个表示层结束的结点.
     */
    protected static class LevelNode extends Node {

        public static Node INSTANCE = new LevelNode();

        public static Node getInstance() {
            return INSTANCE;
        }

        public LevelNode() {
            super(null);
        }
    }
}
