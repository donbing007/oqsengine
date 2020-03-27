package com.xforceplus.ultraman.oqsengine.sdk.controller;

import com.alibaba.fastjson.JSON;
import com.xforceplus.ultraman.oqsengine.pojo.auth.Authorization;
import com.xforceplus.ultraman.oqsengine.pojo.dto.UltForm;
import com.xforceplus.ultraman.oqsengine.pojo.dto.UltPage;
import com.xforceplus.ultraman.oqsengine.sdk.store.RowUtils;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.FormBoMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Response;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ResponseList;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ult test
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = UltFormSettingControllerTest.class)
@WebAppConfiguration
public class UltFormSettingControllerTest {

    private RestTemplate restTemplate = new RestTemplate();

//    @Mock
//    private FormBoMapLocalStore formBoMapLocalStore;

    /**
     * 动态表单
     *
     * @return
     */
//    @Test
    public void deploymentsPage() {
        String accessUri = "http://pfcp.phoenix-t.xforceplus.com";
//        String accessUri = "http://localhost:8080";
        String url = String.format("%s/forms/%s/deployments"
                , accessUri
                , "1231472494170529793");
        Authorization auth = new Authorization();
        auth.setTenantId(Long.parseLong("1141603295426236416"));
        Response<List<UltForm>> result = new Response<List<UltForm>>();
        try {
            result = restTemplate.postForObject(url, auth, Response.class);
            if (result.getResult() != null) {
                List<UltForm> ultForms = result.getResult();
                for (int i = 0; i < ultForms.size(); i++) {
                    UltForm saveUltForm = JSON.parseObject(JSON.toJSONString(ultForms.get(i)), UltForm.class);
                }
                //将List转成Entity
//                UltForm ultForm = JSON.parseObject(JSON.toJSONString(result.getResult()),UltForm.class);
                //将数据保存到内存中
//                formBoMapLocalStore.save(ultForm);
            }
        } catch (Exception e) {
            result.setCode("204");
            result.setMessage("部署失败");
        }
    }

    /**
     * 根据表单id获取详细json配置
     *
     * @return
     */
    @Test
    public void pageBoSeetings() {
        FormBoMapLocalStore formBoMapLocalStore = FormBoMapLocalStore.create();
        DataSet ds = null;
        if (!StringUtils.isEmpty("1230708278908764162")) {
            ds = formBoMapLocalStore.query().selectAll()
                    .where("id")
                    .eq("1230708278908764162")
                    .execute();

            List<Row> rows = ds.toRows();
            ResponseList<UltForm> items = rows.stream().map(this::toUltForm).collect(Collectors.toCollection(ResponseList::new));

            Response<UltForm> response = new Response<>();
            response.setMessage("查询成功");
            response.setCode("1");
            if (items.size() == 1) {
                response.setResult(items.get(0));
            }

        } else {
            Response<ResponseList<UltPage>> response = new Response<>();

            response.setMessage("未传id");
            response.setCode("1");
        }
    }

    private UltForm toUltForm(Row row) {
        UltForm ultForm = new UltForm();
        ultForm.setId(Long.parseLong(RowUtils.getRowValue(row, "id").map(Object::toString).orElse("")));
        ultForm.setName(RowUtils.getRowValue(row, "name").map(Object::toString).orElse(""));
        ultForm.setCode(RowUtils.getRowValue(row, "code").map(Object::toString).orElse(""));
        ultForm.setSetting(RowUtils.getRowValue(row, "setting").map(Object::toString).orElse(""));
        return ultForm;
    }

}
