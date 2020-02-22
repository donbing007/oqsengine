package com.xforceplus.ultraman.oqsengine.sdk.controller;

import com.alibaba.fastjson.JSON;
import com.xforceplus.ultraman.oqsengine.pojo.dto.UltPage;
import com.xforceplus.ultraman.oqsengine.sdk.store.RowUtils;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.PageBoMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Response;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ResponseList;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.UltPageBoItem;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = UltPageSettingControllerTest.class)
@WebAppConfiguration
public class UltPageSettingControllerTest {

    private RestTemplate restTemplate = new RestTemplate();

//    @Mock
//    private PageBoMapLocalStore pageBoMapLocalStore;

    /**
     * 部署页面
     * @return
     */
    @Test
    public void deploymentsPage() {
        String accessUri = "http://localhost:8080";
        String url = String.format("%s/pages/%s/deployments"
                , accessUri
                , "1229961613309448194");
        Response<UltPage> result = new Response<UltPage>();

        try {
            result = restTemplate.getForObject(url,Response.class);
            if (result.getResult()!=null){
                //将List转成Entity
                UltPage ultPage = JSON.parseObject(JSON.toJSONString(result.getResult()),UltPage.class);
                //将数据保存到内存中
                PageBoMapLocalStore pageBoMapLocalStore = PageBoMapLocalStore.create();
                pageBoMapLocalStore.save(ultPage);

                DataSet ds = pageBoMapLocalStore.query().selectAll()
                        .where("id")
                        .eq("1229961613309448194")
                        .execute();
                List<Row> rows = ds.toRows();
                ResponseList<UltPageBoItem> items = rows.stream().map(this::toUltPageBos).collect(Collectors.toCollection(ResponseList::new));

            }
        }catch (Exception e){
            e.printStackTrace();
            result.setCode("400");
            result.setMessage("部署失败");
        }
    }

    /**
     * 获取页面bo列表
     * @return
     */
    @Test
    public void pageBos() {
        PageBoMapLocalStore pageBoMapLocalStore = PageBoMapLocalStore.create();
        DataSet ds = null;
        System.out.println("1229961613309448194");
        if(!StringUtils.isEmpty("1229961613309448194")) {
            ds = pageBoMapLocalStore.query().selectAll()
                    .where("id")
                    .eq("1229961613309448194")
                    .execute();

            List<Row> rows = ds.toRows();
            ResponseList<UltPageBoItem> items = rows.stream().map(this::toUltPageBos).collect(Collectors.toCollection(ResponseList::new));

            Response<ResponseList<UltPageBoItem>> response = new Response<>();
            response.setMessage("查询成功");
            response.setCode("1");
            response.setResult(items);

        }else {
            Response<ResponseList<UltPage>> response = new Response<>();

            response.setMessage("未传id");
            response.setCode("1");

        }
    }

    /**
     * 根据业务对象id获取详细json配置
     * @return
     */
    @Test
    public void pageBoSeetings() {
        PageBoMapLocalStore pageBoMapLocalStore = PageBoMapLocalStore.create();
        DataSet ds = null;
        if(!StringUtils.isEmpty("1229961613309448194")) {
            ds = pageBoMapLocalStore.query().selectAll()
                    .where("settingId")
                    .eq("1229961613309448194")
                    .execute();

            List<Row> rows = ds.toRows();
            ResponseList<UltPageBoItem> items = rows.stream().map(this::toUltPageBoSeeting).collect(Collectors.toCollection(ResponseList::new));

            Response<UltPageBoItem> response = new Response<>();
            response.setMessage("查询成功");
            response.setCode("1");
            if (items.size() == 1){
                response.setResult(items.get(0));
            }

        }else {
            Response<ResponseList<UltPage>> response = new Response<>();

            response.setMessage("未传id");
            response.setCode("1");

        }

    }

    private UltPageBoItem toUltPageBos(Row row){
        UltPageBoItem ultPageBoItem = new UltPageBoItem();
        ultPageBoItem.setId(Long.parseLong(RowUtils.getRowValue(row, "settingId").map(Object::toString).orElse("")));
        ultPageBoItem.setPageId(Long.parseLong(RowUtils.getRowValue(row, "id").map(Object::toString).orElse("")));
        ultPageBoItem.setBoCode(RowUtils.getRowValue(row, "boCode").map(Object::toString).orElse(""));
        ultPageBoItem.setBoName(RowUtils.getRowValue(row, "boName").map(Object::toString).orElse(""));
        return ultPageBoItem;
    }

    private UltPageBoItem toUltPageBoSeeting(Row row){
        UltPageBoItem ultPageBoItem = new UltPageBoItem();
        ultPageBoItem.setId(Long.parseLong(RowUtils.getRowValue(row, "settingId").map(Object::toString).orElse("")));
        ultPageBoItem.setPageId(Long.parseLong(RowUtils.getRowValue(row, "id").map(Object::toString).orElse("")));
        ultPageBoItem.setBoCode(RowUtils.getRowValue(row, "boCode").map(Object::toString).orElse(""));
        ultPageBoItem.setBoName(RowUtils.getRowValue(row, "boName").map(Object::toString).orElse(""));
        ultPageBoItem.setSetting(RowUtils.getRowValue(row, "setting").map(Object::toString).orElse(""));
        return ultPageBoItem;
    }

}
