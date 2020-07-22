package com.xforceplus.ultraman.oqsengine.sdk.controller;

import com.xforceplus.ultraman.oqsengine.sdk.ContextWareBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * 〈功能概述〉<br>  
 * @className: DictControllerTest
 * @package: com.xforceplus.ultraman.oqsengine.sdk.controller
 * @author: wangzheng
 * @date: 2020/7/22 11:00
 */
@EnableWebMvc
public class DictControllerTest extends ContextWareBaseTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void getDict() throws Exception{
        MvcResult mvcResult = this.mockMvc.perform(
                get("/enum/1250259591257968642/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
        ).andDo(print()).andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    public void getDicts() throws Exception{
        String[] s = new String[]{"1254614216480735234","1254614305689387010","1250258068000002049"};

        MvcResult mvcResult = this.mockMvc.perform(
                get("/enums/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .param("ids", s)
        ).andDo(print()).andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }
}
