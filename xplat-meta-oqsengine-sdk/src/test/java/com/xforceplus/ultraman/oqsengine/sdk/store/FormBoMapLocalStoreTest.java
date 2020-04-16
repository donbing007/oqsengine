package com.xforceplus.ultraman.oqsengine.sdk.store;

import com.xforceplus.ultraman.oqsengine.pojo.dto.UltForm;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.FormBoMapLocalStore;
import org.junit.Test;

public class FormBoMapLocalStoreTest {

    @Test
    public void saveTest(){
        FormBoMapLocalStore store = FormBoMapLocalStore.create();
        UltForm ultForm = new UltForm();
        ultForm.setCode("abc");
        ultForm.setId(1L);
        ultForm.setName("abc");
        ultForm.setSetting("abc");
        ultForm.setRefFormId(1L);
        ultForm.setTenantId(0L);
        ultForm.setTenantName("hhh");
        ultForm.setAppId(0L);
        ultForm.setVersion("");

        store.save(ultForm);
        store.query().selectAll().execute().toRows().forEach(System.out::println);
    }
}
