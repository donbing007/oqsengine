package com.xforceplus.ultraman.oqsengine.storage.master.unique;


/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 2020/10/27 4:04 PM
 */
public class UniqueIndexValue {
    private String name;
    private String code;
    private String value;

    public UniqueIndexValue(String name, String code, String value) {
        this.name = name;
        this.code = code;
        this.value = value;
    }

    public static UniqueIndexValue.UniqueIndexValueBuilder builder() {
        return new UniqueIndexValue.UniqueIndexValueBuilder();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "UniqueIndexValue{" +
            "name='" + name + '\'' +
            ", code='" + code + '\'' +
            ", value='" + value + '\'' +
            '}';
    }

    /**
     *
     */
    public static class UniqueIndexValueBuilder {
        private String name;
        private String code;
        private String value;

        UniqueIndexValueBuilder() {
        }

        public UniqueIndexValue.UniqueIndexValueBuilder name(final String name) {
            this.name = name;
            return this;
        }

        public UniqueIndexValue.UniqueIndexValueBuilder code(final String code) {
            this.code = code;
            return this;
        }

        public UniqueIndexValue.UniqueIndexValueBuilder value(final String value) {
            this.value = value;
            return this;
        }

        public UniqueIndexValue build() {
            return new UniqueIndexValue(this.name, this.code, this.value);
        }

        @Override
        public String toString() {
            return "UniqueIndexValue.UniqueIndexValueBuilder(name=" + this.name + ", code=" + this.code + ", value=" + this.value + ")";
        }
    }
}
