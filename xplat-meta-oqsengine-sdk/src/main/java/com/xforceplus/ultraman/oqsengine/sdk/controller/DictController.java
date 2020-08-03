package com.xforceplus.ultraman.oqsengine.sdk.controller;

import com.xforceplus.ultraman.oqsengine.sdk.service.EntityServiceEx;
import com.xforceplus.ultraman.oqsengine.sdk.store.RowUtils;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.DictMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.DictItem;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Response;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ResponseList;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.UltPageBoItem;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author admin
 */
@RequestMapping
public class DictController {

    @Autowired
    private EntityServiceEx entityServiceEx;

    @GetMapping("/enum/{id}/options")
    @ResponseBody
    public Response getDict(@PathVariable("id") String enumId
            , @RequestParam(required = false) String enumCode
    ) {
        Response<ResponseList<DictItem>> response = new Response<>();
        List<DictItem> dictItems = entityServiceEx.findDictItems(enumId, enumCode);

        if (dictItems.size() > 0) {
            response.setMessage("查询成功");
            response.setCode("200");
            response.setResult((ResponseList<DictItem>) dictItems);
        } else {
            response.setMessage("查询无结果");
            response.setCode("500");
        }
        return response;
    }

    @GetMapping("/enums/options")
    @ResponseBody
    public Response getDicts(
            @RequestParam(required = false, value = "ids") String[] enumIds
    ) {
        Response<Map<String, List<DictItem>>> response = new Response<>();
        if (enumIds != null && enumIds.length > 0) {
            Map<String, List<DictItem>> enums = new HashMap<>();
            for (String enumId : enumIds) {
                List<DictItem> dictItems = entityServiceEx.findDictItems(enumId, null);
                enums.put(enumId, dictItems);
            }
            if (enums.size() > 0) {
                response.setMessage("查询成功");
                response.setCode("1");
                response.setResult(enums);
            } else {
                response.setMessage("查询无结果");
                response.setCode("-1");
            }
        } else {
            response.setMessage("请传入需要查询的字典id");
            response.setCode("-1");
        }

        return response;
    }
}
