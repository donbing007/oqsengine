package com.xforceplus.ultraman.oqsengine.sdk.controller;

import com.xforceplus.ultraman.oqsengine.sdk.store.RowUtils;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.DictMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.DictItem;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Response;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ResponseList;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class DictController {

    @Autowired
    private DictMapLocalStore store;

    @GetMapping("/enum/{id}/options")
    public Response<ResponseList<DictItem>> getDict(@PathVariable("id") String enumId
            , @RequestParam(required = false) String enumCode
    ){

        DataSet ds = null;
        if(StringUtils.isEmpty(enumCode)) {
            ds = store.query().selectAll()
                    .where("dictId")
                    .eq(enumId).execute();
        }else{
            ds = store.query().selectAll()
                    .where("dictId")
                    .eq(enumId)
                    .and("code").eq(enumCode)
                    .execute();
        }

        List<Row> rows = ds.toRows();


        ResponseList<DictItem> items = rows.stream().map(this::toDictItem).collect(Collectors.toCollection(ResponseList::new));

        Response<ResponseList<DictItem>> response = new Response<>();

        response.setMessage("查询成功");
        response.setCode("1");
        response.setResult(items);

        return response;
    }

    private  DictItem toDictItem(Row row){
        DictItem dictItem = new DictItem();
        dictItem.setText(RowUtils.getRowValue(row, "name").map(Object::toString).orElse(""));
        dictItem.setValue(RowUtils.getRowValue(row, "code").map(Object::toString).orElse(""));
        return dictItem;
    }
}
