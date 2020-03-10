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
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping
public class DictController {

    @Autowired
    private DictMapLocalStore store;

    @GetMapping("/enum/{id}/options")
    @ResponseBody
    public Response<ResponseList<DictItem>> getDict(@PathVariable("id") String enumId
            , @RequestParam(required = false) String enumCode
    ){
        DataSet ds = null;
        List<Row> rows = new ArrayList<Row>();
        if(StringUtils.isEmpty(enumCode)) {
            ds = store.query().selectAll()
                    .where("publishDictId")
                    .eq(enumId).execute();
            rows = ds.toRows();

            if (!(rows!=null && rows.size() > 0)){
                ds = store.query().selectAll()
                        .where("dictId")
                        .eq(enumId).execute();
                rows = ds.toRows();
            }
        }else{
            ds = store.query().selectAll()
                    .where("publishDictId")
                    .eq(enumId)
                    .and("code").eq(enumCode)
                    .execute();
            rows = ds.toRows();

            if (!(rows!=null && rows.size() > 0)) {
                ds = store.query().selectAll()
                        .where("dictId")
                        .eq(enumId)
                        .and("code").eq(enumCode)
                        .execute();
                rows = ds.toRows();
            }
        }

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
