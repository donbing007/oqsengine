package com.xforceplus.ultraman.oqsengine.sdk.transactional;

/**
 *
 */
public class OqsTransaction {

    private String id;

    private boolean isRollBack;

    private boolean isCommit;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isRollBack() {
        return isRollBack;
    }

    public void setRollBack(boolean rollBack) {
        isRollBack = rollBack;
    }

    public boolean isCommit() {
        return isCommit;
    }

    public void setCommit(boolean commit) {
        isCommit = commit;
    }

    @Override
    public String toString() {
        return "OqsTransaction{" +
                "id='" + id + '\'' +
                ", isRollBack=" + isRollBack +
                ", isCommit=" + isCommit +
                '}';
    }
}
