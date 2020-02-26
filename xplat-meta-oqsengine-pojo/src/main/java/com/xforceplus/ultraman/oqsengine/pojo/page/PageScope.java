package com.xforceplus.ultraman.oqsengine.pojo.page;

import java.io.Serializable;

/**
 * 记录分页的起始行和结束行序号。
 * @author donbing
 * @since 1.0
 * @version 1.00, 08/3/15
 */
public class PageScope implements Serializable {
    
    private static final long serialVersionUID = 6857657318970467129L;
    protected long startLine;
    protected long endLine;

    public PageScope(long startLine, long endLine) {
        this.startLine = startLine;
        this.endLine = endLine;
    }

    public long getStartLine() {
        return startLine == 0 ? 0 : startLine - 1;
    }

    public long getEndLine() {
        return endLine == 0 ? 0 : endLine - 1;
    }
    
    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("Dividing page information is ");
        buff.append(startLine);
        buff.append(" to ");
        buff.append(endLine);
        return buff.toString();
    }
}
