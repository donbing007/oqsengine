package com.xforceplus.ultraman.oqsengine.pojo.cdc.dto;

/**
 * desc :
 * name : RawEntityValue
 *
 * @author : xujia
 * date : 2020/11/10
 * @since : 1.8
 */
public class RawEntityValue {
    private String attr;
    private String meta;

    public RawEntityValue() {
    }

    public RawEntityValue(String attr, String meta) {
        this.attr = attr;
        this.meta = meta;
    }

    public String getAttr() {
        return attr;
    }

    public String getMeta() {
        return meta;
    }

    public void setAttr(String attr) {
        this.attr = attr;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }
}
