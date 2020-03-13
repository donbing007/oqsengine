package com.xforceplus.ultraman.oqsengine.sdk.controller;

import com.alibaba.fastjson.JSON;
import com.xforceplus.ultraman.oqsengine.pojo.auth.Authorization;
import com.xforceplus.ultraman.oqsengine.pojo.dto.PageBo;
import com.xforceplus.ultraman.oqsengine.pojo.dto.UltForm;
import com.xforceplus.ultraman.oqsengine.pojo.dto.UltPage;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.store.RowUtils;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.FormBoMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Response;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ResponseList;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.UltPageBoItem;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping
public class UltFormSettingController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FormBoMapLocalStore formBoMapLocalStore;

    @Autowired
    private AuthSearcherConfig config;

    /**
     * 部署动态表单
     * @return
     */
    @PostMapping("/form-settings/{id}/deployments" )
    @ResponseBody
    public Response deploymentsForm(@PathVariable String id) {
        Response<List<UltForm>> result = initSeetings(id);
        return result;
//        String accessUri = "http://pfcp.phoenix-t.xforceplus.com";
//        String url = String.format("%s/forms/%s/deployments"
//                , accessUri
//                , id);
//        Authorization auth = new Authorization();
//        auth.setAppId(Long.parseLong(config.getAppId()));
////        auth.setTenantId(Long.parseLong(config.getTenant()));
//        auth.setEnv(config.getEnv());
//        Response<List<UltForm>> result = new Response<List<UltForm>>();
//        try {
////            result = restTemplate.getForObject(url,Response.class);
//            result = restTemplate.postForObject(url, auth,Response.class);
//            if (result.getResult()!=null){
//                List<UltForm> ultForms = result.getResult();
//                for (int i = 0;i<ultForms.size();i++) {
//                    UltForm saveUltForm = JSON.parseObject(JSON.toJSONString(ultForms.get(i)),UltForm.class);
//                    formBoMapLocalStore.save(saveUltForm);
//                }
//            }
//            return result;
//        }catch (Exception e){
//            result.setCode("400");
//            result.setMessage("部署失败");
//            return result;
//        }
    }

    /**
     * 根据表单id获取详细json配置
     * @return
     */
    @GetMapping("/form-settings/{id}" )
    @ResponseBody
    public Response pageBoSeetings(HttpServletRequest request,@PathVariable String id) {
        DataSet ds = null;
        String tenantId = request.getParameter("tenantId");
        Response<UltForm> response = new Response<>();
        if(!StringUtils.isEmpty(id)) {
            ResponseList<UltForm> items = getSeetings(id,tenantId);
            if (items.size() == 1){
                response.setMessage("查询成功");
                response.setCode("200");
                response.setResult(items.get(0));
                return response;
            }else {
                Response<List<UltForm>> result = initSeetings(id);
                if (result.getResult().size() > 0){
                    items = getSeetings(id,tenantId);
                }
                if (items.size() == 1){
                    response.setMessage("查询成功");
                    response.setCode("200");
                    response.setResult(items.get(0));
                    return response;
                }else {
                    response.setMessage("菜单未部署");
                    response.setCode("500");
                    return response;
                }
            }

//            Response<UltForm> response = new Response<>();
//            List<Row> trows = new ArrayList<>();
//            if (!StringUtils.isEmpty(tenantId)) {
//                ds = formBoMapLocalStore.query().selectAll()
//                        .where("refFormId")
//                        .eq(id)
//                        .and("tenantId")
//                        .eq(tenantId)
//                        .execute();
//                trows = ds.toRows();
//            }
//            if (ds!=null && trows!=null && trows.size() > 0){
//                ResponseList<UltForm> items = trows.stream().
//                        map(this::toUltForm).collect(Collectors.toCollection(ResponseList::new));
//                response.setMessage("查询成功");
//                response.setCode("1");
//                if (items.size() == 1) {
//                    response.setResult(items.get(0));
//                }
//            }else {
//                ds = formBoMapLocalStore.query().selectAll()
//                        .where("id")
//                        .eq(id)
//                        .execute();
//
//                List<Row> rows = ds.toRows();
//                ResponseList<UltForm> items = rows.stream().
//                        map(this::toUltForm).collect(Collectors.toCollection(ResponseList::new));
//
//                response.setMessage("查询成功");
//                response.setCode("1");
//                if (items.size() == 1) {
//                    response.setResult(items.get(0));
//                }else {
//                    Response<List<UltForm>> result = initSeetings(id);
//                    if (result.getResult().size() > 0) {
//
//                    }
//                }
//            }
//            return response;
//
        }else {
            response.setMessage("未传id");
            response.setCode("1");
            return response;
        }
    }

    private Response initSeetings(String id){
        String accessUri = "http://pfcp.phoenix-t.xforceplus.com";
        String url = String.format("%s/forms/%s/deployments"
                , accessUri
                , id);
        Authorization auth = new Authorization();
        auth.setAppId(Long.parseLong(config.getAppId()));
//        auth.setTenantId(Long.parseLong(config.getTenant()));
        auth.setEnv(config.getEnv());
        Response<List<UltForm>> result = new Response<List<UltForm>>();
        try {
//            result = restTemplate.getForObject(url,Response.class);
            result = restTemplate.postForObject(url, auth,Response.class);
            if (result.getResult()!=null){
                List<UltForm> ultForms = result.getResult();
                for (int i = 0;i<ultForms.size();i++) {
                    UltForm saveUltForm = JSON.parseObject(JSON.toJSONString(ultForms.get(i)),UltForm.class);
                    formBoMapLocalStore.save(saveUltForm);
                }
            }
            return result;
        }catch (Exception e){
            result.setCode("400");
            result.setMessage("获取失败");
            return result;
        }
    }

    private ResponseList getSeetings(String id,String tenantId){
        DataSet ds = null;
        if(!StringUtils.isEmpty(id)) {
            List<Row> trows = new ArrayList<>();
            if (!StringUtils.isEmpty(tenantId)) {
                ds = formBoMapLocalStore.query().selectAll()
                        .where("refFormId")
                        .eq(id)
                        .and("tenantId")
                        .eq(tenantId)
                        .execute();
                trows = ds.toRows();
            }
            if (ds!=null && trows!=null && trows.size() > 0){
                ResponseList<UltForm> items = trows.stream().
                        map(this::toUltForm).collect(Collectors.toCollection(ResponseList::new));
                return items;
            }else {
                ds = formBoMapLocalStore.query().selectAll()
                        .where("id")
                        .eq(id)
                        .execute();
                List<Row> rows = ds.toRows();
                ResponseList<UltForm> items = rows.stream().
                        map(this::toUltForm).collect(Collectors.toCollection(ResponseList::new));
                return items;
            }
        }else {
            return null;
        }
    }

    private UltForm toUltForm(Row row){
        UltForm ultForm = new UltForm();
        ultForm.setId(Long.parseLong(RowUtils.getRowValue(row, "id").map(Object::toString).orElse("")));
        ultForm.setName(RowUtils.getRowValue(row, "name").map(Object::toString).orElse(""));
        ultForm.setCode(RowUtils.getRowValue(row, "code").map(Object::toString).orElse(""));
        ultForm.setRefFormId(Long.parseLong(RowUtils.getRowValue(row, "refFormId").map(Object::toString).orElse("")));
        if (!"".equals(RowUtils.getRowValue(row, "tenantId").map(Object::toString).orElse(""))){
            ultForm.setTenantId(Long.parseLong(RowUtils.getRowValue(row, "tenantId").map(Object::toString).orElse("")));
        }
        ultForm.setTenantName(RowUtils.getRowValue(row, "tenantName").map(Object::toString).orElse(""));
        ultForm.setSetting(RowUtils.getRowValue(row, "setting").map(Object::toString).orElse(""));
        return ultForm;
    }

}
