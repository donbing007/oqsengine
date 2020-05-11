package com.xforceplus.ultraman.oqsengine.storage.transaction;

import com.xforceplus.ultraman.oqsengine.storage.undo.constant.DbType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * MultiLocalTransaction Tester.
 *
 * @author <Authors name>
 * @version 1.0 02/20/2020
 * @since <pre>Feb 20, 2020</pre>
 */
public class MultiLocalTransactionTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testCommit() throws Exception {
        MultiLocalTransaction tx = new MultiLocalTransaction(1);

        List<MockResource> resources = buildResources(10, false);

        for (MockResource resource : resources) {
            tx.join(resource);
        }

        tx.commit();
        for (MockResource resource : resources) {
            Assert.assertTrue(resource.isCommitted());
        }
    }

    @Test
    public void testRollback() throws Exception {
        MultiLocalTransaction tx = new MultiLocalTransaction(1);

        List<MockResource> resources = buildResources(10, false);

        for (MockResource resource : resources) {
            tx.join(resource);
        }

        tx.rollback();
        for (MockResource resource : resources) {
            Assert.assertTrue(resource.isRollback());
        }
    }

    @Test
    public void testCommitEx() throws Exception {
        MultiLocalTransaction tx = new MultiLocalTransaction(1);

        List<MockResource> exResources = buildResources(2, true); // 这里提交会异常.
        List<MockResource> correctResources = buildResources(1, false); // 这里可以提交

        for (MockResource resource : exResources) {
            tx.join(resource);
        }

        for (MockResource resource : correctResources) {
            tx.join(resource);
        }

        try {
            tx.commit();
            Assert.fail("No expected exception was thrown.");
        } catch (SQLException ex) {

        }

        for (MockResource r : exResources) {
            Assert.assertFalse(r.isCommitted());
        }

        for (MockResource r : correctResources) {
            Assert.assertTrue(r.isCommitted());
        }

    }

    @Test
    public void testRollbackEx() throws Exception {
        MultiLocalTransaction tx = new MultiLocalTransaction(1);

        List<MockResource> exResources = buildResources(2, true); // 这里提交会异常.
        List<MockResource> correctResources = buildResources(1, false); // 这里可以提交

        for (MockResource resource : exResources) {
            tx.join(resource);
        }

        for (MockResource resource : correctResources) {
            tx.join(resource);
        }

        try {
            tx.rollback();
            Assert.fail("No expected exception was thrown.");
        } catch (SQLException ex) {

        }

        for (MockResource r : exResources) {
            Assert.assertFalse(r.isRollback());
        }

        for (MockResource r : correctResources) {
            Assert.assertTrue(r.isRollback());
        }
    }

    private List<MockResource> buildResources(int size, boolean ex) {
        List<MockResource> resources = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            resources.add(new MockResource(Integer.toString(i), Integer.toString(i), ex));
        }

        return resources;
    }

    static class MockResource implements TransactionResource<String> {

        private String key;
        private String value;
        private boolean committed;
        private boolean rollback;
        private boolean destroyed;
        private boolean exception;


        public MockResource(String key, String value) {
            this(key, value, false);
        }

        public MockResource(String key, String value, boolean ex) {
            this.key = key;
            this.value = value;
            this.exception = ex;
        }


        @Override
        public String key() {
            return key;
        }

        @Override
        public String value() {
            return null;
        }

        @Override
        public void commit() throws SQLException {
            if (exception) {
                throw new SQLException("Expected SQLException.");
            }
            this.committed = true;
        }

        @Override
        public void rollback() throws SQLException {
            if (exception) {
                throw new SQLException("Expected SQLException.");
            }

            this.rollback = true;
        }

        @Override
        public void destroy() throws SQLException {
            this.destroyed = true;
        }

        public boolean isCommitted() {
            return committed;
        }

        public boolean isRollback() {
            return rollback;
        }

        @Override
        public DbType dbType() {
            return null;
        }

        @Override
        public boolean isDestroyed() throws SQLException {
            return false;
        }

        @Override
        public void undo(boolean commit) throws SQLException {

        }


    }

}
