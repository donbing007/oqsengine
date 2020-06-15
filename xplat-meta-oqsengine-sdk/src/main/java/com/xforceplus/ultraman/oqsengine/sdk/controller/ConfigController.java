package com.xforceplus.ultraman.oqsengine.sdk.controller;

import com.xforceplus.ultraman.config.storage.ConfigurationStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * config current controller
 */
@RequestMapping
public class ConfigController {

    @Autowired
    private ConfigurationStorage configurationStorage;

    @GetMapping("/history")
    @ResponseBody
    public Map getHistory(){
        return configurationStorage.getCurrentState().getHistoryRecords();
    }

    @GetMapping("/changelist")
    @ResponseBody
    public Map getChangelist(){
        return configurationStorage.getCurrentState().getCurrentChangeList();
    }
}
