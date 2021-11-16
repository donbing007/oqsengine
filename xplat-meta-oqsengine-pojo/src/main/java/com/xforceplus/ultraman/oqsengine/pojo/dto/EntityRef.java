package com.xforceplus.ultraman.oqsengine.pojo.dto;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * 表示一个 entity 的指针.
 *
 * @author dongbin
 * @version 0.1 2020/2/17 16:55
 * @since 1.8
 */
public final class EntityRef implements Serializable, Comparable<EntityRef> {

    private long id;
    private int op;
    private int major;
    private String orderValue;
    private String secondOrderValue;
    private String thridOrderValue;

    public long getId() {
        return id;
    }

    public int getOp() {
        return op;
    }

    public int getMajor() {
        return major;
    }

    /**
     * 获取排序值.
     *
     * @param index 排序字段序号,从0开始,最大为2.
     * @return 排序值.
     */
    public Optional<String> getSortValue(int index) {
        switch (index) {
            case 0: {
                return Optional.ofNullable(getOrderValue());
            }
            case 1: {
                return Optional.ofNullable(getSecondOrderValue());
            }
            case 2: {
                return Optional.ofNullable(getThridOrderValue());
            }
            default: {
                return Optional.empty();
            }
        }
    }

    public String getOrderValue() {
        return orderValue;
    }

    public String getSecondOrderValue() {
        return secondOrderValue;
    }

    public String getThridOrderValue() {
        return thridOrderValue;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setOp(int op) {
        this.op = op;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public void setOrderValue(String orderValue) {
        this.orderValue = orderValue;
    }

    public void setSecondOrderValue(String secondOrderValue) {
        this.secondOrderValue = secondOrderValue;
    }

    public void setThridOrderValue(String thridOrderValue) {
        this.thridOrderValue = thridOrderValue;
    }

    /**
     * 设置排序字段.
     *
     * @param index     序号,从0开始,最大接爱为2.
     * @param sortValue 排序的值.
     */
    public void setSortValue(int index, String sortValue) {
        switch (index) {
            case 0: {
                setOrderValue(sortValue);
                break;
            }
            case 1: {
                setSecondOrderValue(sortValue);
                break;
            }
            case 2: {
                setThridOrderValue(sortValue);
                break;
            }
            default: {

            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EntityRef)) {
            return false;
        }
        EntityRef entityRef = (EntityRef) o;
        return getId() == entityRef.getId()
            && getOp() == entityRef.getOp()
            && getMajor() == entityRef.getMajor()
            && Objects.equals(getOrderValue(), entityRef.getOrderValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getOp(), getMajor(), getOrderValue());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("EntityRef{");
        sb.append("id=").append(id);
        sb.append(", op=").append(op);
        sb.append(", major=").append(major);
        sb.append(", orderValue='").append(orderValue).append('\'');
        sb.append(", secondOrderValue='").append(secondOrderValue).append('\'');
        sb.append(", thridOrderValue='").append(thridOrderValue).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(EntityRef o) {
        if (this.id < o.id) {
            return -1;
        } else if (this.id > o.id) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * 构造器.
     */
    public static final class Builder {
        private long id;
        private int op;
        private int major;
        private String orderValue;
        private String secondOrderValue;
        private String thridOrderValue;

        private Builder() {
        }

        public static Builder anEntityRef() {
            return new Builder();
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withOp(int op) {
            this.op = op;
            return this;
        }

        public Builder withMajor(int major) {
            this.major = major;
            return this;
        }

        public Builder withOrderValue(String orderValue) {
            this.orderValue = orderValue;
            return this;
        }

        public Builder withSecondOrderValue(String secondOrderValue) {
            this.secondOrderValue = secondOrderValue;
            return this;
        }

        public Builder withThridOrderValue(String thridOrderValue) {
            this.thridOrderValue = thridOrderValue;
            return this;
        }

        /**
         * 根据顺序设置排序值.
         *
         * @param index      序号,从0开始.最大接爱的为2.
         * @param orderValue 排序的值.
         * @return 构造器.
         */
        public Builder withSortValue(int index, String orderValue) {
            switch (index) {
                case 0: {
                    return withOrderValue(orderValue);
                }
                case 1: {
                    return withSecondOrderValue(orderValue);
                }
                case 2: {
                    return withThridOrderValue(orderValue);
                }
                default: {
                    return this;
                }
            }
        }

        /**
         * 构造.
         */
        public EntityRef build() {
            EntityRef entityRef = new EntityRef();
            entityRef.op = this.op;
            entityRef.thridOrderValue = this.thridOrderValue;
            entityRef.id = this.id;
            entityRef.major = this.major;
            entityRef.orderValue = this.orderValue;
            entityRef.secondOrderValue = this.secondOrderValue;
            return entityRef;
        }
    }
}
