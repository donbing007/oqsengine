package com.xforceplus.ultraman.oqsengine.sdk.controller;

import com.alibaba.fastjson.JSON;
import com.xforceplus.ultraman.oqsengine.pojo.dto.PageBo;
import com.xforceplus.ultraman.oqsengine.pojo.dto.UltForm;
import com.xforceplus.ultraman.oqsengine.pojo.dto.UltPage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.UltPageBo;
import com.xforceplus.ultraman.oqsengine.sdk.store.RowUtils;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.PageBoMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.DictItem;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Response;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ResponseList;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.UltPageBoItem;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UltPageSettingController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PageBoMapLocalStore pageBoMapLocalStore;

    /**
     * 部署页面
     * @return
     */
    @PostMapping("/pages/{id}/deployments" )
    public Response deploymentsPage(@PathVariable String id) {
        String accessUri = "http://pfcp.phoenix-t.xforceplus.com";
        String url = String.format("%s/pages/%s/deployments"
                , accessUri
                , id);
        Response<UltPage> result = new Response<UltPage>();
        try {
            result = restTemplate.getForObject(url,Response.class);
            if (result.getResult()!=null){
                //将List转成Entity
                UltPage ultPage = JSON.parseObject(JSON.toJSONString(result.getResult()),UltPage.class);
                //将数据保存到内存中
                pageBoMapLocalStore.save(ultPage);
            }
            return result;
        }catch (Exception e){
            result.setCode("400");
            result.setMessage("部署失败");
            return result;
        }
    }

    /**
     * 获取页面bo列表
     * @return
     */
    @GetMapping("/pages/{id}/bo-settings" )
    public Response pageBos(@PathVariable String id) {

        DataSet ds = null;
        if(!StringUtils.isEmpty(id)) {
            ds = pageBoMapLocalStore.query().selectAll()
                    .where("id")
                    .eq(id)
                    .execute();

            List<Row> rows = ds.toRows();
            ResponseList<UltPageBoItem> items = rows.stream().map(this::toUltPageBos).collect(Collectors.toCollection(ResponseList::new));

            Response<ResponseList<UltPageBoItem>> response = new Response<>();
            response.setMessage("查询成功");
            response.setCode("1");
            response.setResult(items);
            return response;

        }else {
            Response<ResponseList<UltPage>> response = new Response<>();

            response.setMessage("未传id");
            response.setCode("1");

            return response;
        }
    }

    /**
     * 根据业务对象id获取详细json配置
     * @return
     */
    @GetMapping("/bo-settings/{id}" )
    public Response pageBoSeetings(@PathVariable String id) {

        DataSet ds = null;
        if(!StringUtils.isEmpty(id)) {
            ds = pageBoMapLocalStore.query().selectAll()
                    .where("settingId")
                    .eq(id)
                    .execute();

            List<Row> rows = ds.toRows();
            ResponseList<UltPageBoItem> items = rows.stream().map(this::toUltPageBoSeeting).collect(Collectors.toCollection(ResponseList::new));

            Response<UltPageBoItem> response = new Response<>();
            response.setMessage("查询成功");
            response.setCode("1");
            if (items.size() == 1){
                response.setResult(items.get(0));
            }
            return response;

        }else {
            Response<ResponseList<UltPage>> response = new Response<>();

            response.setMessage("未传id");
            response.setCode("1");

            return response;
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
