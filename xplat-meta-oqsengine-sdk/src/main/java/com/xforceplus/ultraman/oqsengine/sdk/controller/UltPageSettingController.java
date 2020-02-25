package com.xforceplus.ultraman.oqsengine.sdk.controller;

import com.alibaba.fastjson.JSON;
import com.xforceplus.ultraman.oqsengine.pojo.auth.Authorization;
import com.xforceplus.ultraman.oqsengine.pojo.dto.PageBo;
import com.xforceplus.ultraman.oqsengine.pojo.dto.UltForm;
import com.xforceplus.ultraman.oqsengine.pojo.dto.UltPage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.UltPageBo;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UltPageSettingController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PageBoMapLocalStore pageBoMapLocalStore;

    @Autowired
    private AuthSearcherConfig config;

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
        Authorization auth = new Authorization();
        auth.setAppId(Long.parseLong(config.getAppId()));
//        auth.setTenantId(Long.parseLong(config.getTenant()));
        auth.setEnv(config.getEnv());
        Response<List<UltPage>> result = new Response<List<UltPage>>();
        try {
            result = restTemplate.postForObject(url, auth,Response.class);
            if (result.getResult()!=null){
                List<UltPage> ultPages = result.getResult();
                for (int i = 0;i<ultPages.size();i++) {
                    UltPage saveUltPage = JSON.parseObject(JSON.toJSONString(ultPages.get(i)),UltPage.class);
                    pageBoMapLocalStore.save(saveUltPage);
                }

                //将List转成Entity
//                UltPage ultPage = JSON.parseObject(JSON.toJSONString(result.getResult()),UltPage.class);
                //将数据保存到内存中
//                pageBoMapLocalStore.save(ultPage);
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
    public Response pageBos(HttpServletRequest request, @PathVariable String id) {

        DataSet ds = null;
        String tenantId = request.getParameter("tenantId");
        if(!StringUtils.isEmpty(id)) {
            Response<ResponseList<UltPageBoItem>> response = new Response<>();
            List<Row> trows = new ArrayList<>();
            if (!StringUtils.isEmpty(tenantId)){
                ds = pageBoMapLocalStore.query().selectAll()
                        .where("refPageId")
                        .eq(id)
                        .and("tenantId")
                        .eq(tenantId)
                        .execute();
                trows = ds.toRows();
            }
            if (ds!=null && trows!=null && trows.size() > 0){
                ResponseList<UltPageBoItem> items = trows.stream().map(this::toUltPageBos).collect(Collectors.toCollection(ResponseList::new));
                response.setMessage("查询成功");
                response.setCode("1");
                response.setResult(items);
            }else {
                ds = pageBoMapLocalStore.query().selectAll()
                        .where("id")
                        .eq(id)
                        .execute();
                List<Row> rows = ds.toRows();
                ResponseList<UltPageBoItem> items = rows.stream().map(this::toUltPageBos).collect(Collectors.toCollection(ResponseList::new));
                response.setMessage("查询成功");
                response.setCode("1");
                response.setResult(items);
            }

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
            Response<UltPageBoItem> response = new Response<>();
            ds = pageBoMapLocalStore.query().selectAll()
                    .where("settingId")
                    .eq(id)
                    .execute();

            List<Row> rows = ds.toRows();
            ResponseList<UltPageBoItem> items = rows.stream().map(this::toUltPageBoSeeting).collect(Collectors.toCollection(ResponseList::new));
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
        if (!"".equals(RowUtils.getRowValue(row, "tenantId").map(Object::toString).orElse(""))){
            ultPageBoItem.setTenantId(Long.parseLong(RowUtils.getRowValue(row, "tenantId").map(Object::toString).orElse("")));
        }
        ultPageBoItem.setTenantName(RowUtils.getRowValue(row, "tenantName").map(Object::toString).orElse(""));
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
