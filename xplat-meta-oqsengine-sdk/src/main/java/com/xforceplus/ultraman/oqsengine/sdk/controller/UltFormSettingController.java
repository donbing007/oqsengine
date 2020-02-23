package com.xforceplus.ultraman.oqsengine.sdk.controller;

import com.alibaba.fastjson.JSON;
import com.xforceplus.ultraman.oqsengine.pojo.dto.PageBo;
import com.xforceplus.ultraman.oqsengine.pojo.dto.UltForm;
import com.xforceplus.ultraman.oqsengine.pojo.dto.UltPage;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UltFormSettingController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FormBoMapLocalStore formBoMapLocalStore;

    /**
     * 部署动态表单
     * @return
     */
    @PostMapping("/forms/{id}/deployments" )
    public Response deploymentsForm(@PathVariable String id) {
        String accessUri = "http://pfcp.phoenix-t.xforceplus.com";
        String url = String.format("%s/forms/%s/deployments"
                , accessUri
                , id);
        Response<UltForm> result = new Response<UltForm>();
        try {
            result = restTemplate.getForObject(url,Response.class);
            if (result.getResult()!=null){
                //将List转成Entity
                UltForm ultForm = JSON.parseObject(JSON.toJSONString(result.getResult()),UltForm.class);
                //将数据保存到内存中
                formBoMapLocalStore.save(ultForm);
            }
            return result;
        }catch (Exception e){
            result.setCode("400");
            result.setMessage("部署失败");
            return result;
        }
    }

    /**
     * 根据表单id获取详细json配置
     * @return
     */
    @GetMapping("/form-settings/{id}" )
    public Response pageBoSeetings(@PathVariable String id) {
        DataSet ds = null;
        if(!StringUtils.isEmpty(id)) {
            ds = formBoMapLocalStore.query().selectAll()
                    .where("id")
                    .eq(id)
                    .execute();

            List<Row> rows = ds.toRows();
            ResponseList<UltForm> items = rows.stream().map(this::toUltForm).collect(Collectors.toCollection(ResponseList::new));

            Response<UltForm> response = new Response<>();
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

    private UltForm toUltForm(Row row){
        UltForm ultForm = new UltForm();
        ultForm.setId(Long.parseLong(RowUtils.getRowValue(row, "id").map(Object::toString).orElse("")));
        ultForm.setName(RowUtils.getRowValue(row, "name").map(Object::toString).orElse(""));
        ultForm.setCode(RowUtils.getRowValue(row, "code").map(Object::toString).orElse(""));
        ultForm.setSetting(RowUtils.getRowValue(row, "setting").map(Object::toString).orElse(""));
        return ultForm;
    }

}
