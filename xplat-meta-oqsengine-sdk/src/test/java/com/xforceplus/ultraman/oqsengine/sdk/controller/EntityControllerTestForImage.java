package com.xforceplus.ultraman.oqsengine.sdk.controller;


import com.xforceplus.ultraman.oqsengine.sdk.ContextWareBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@EnableWebMvc
@ActiveProfiles("image")
@EnableAsync
public class EntityControllerTestForImage extends ContextWareBaseTest{

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void  testQueryWithImage() throws Exception{

        MvcResult mvcResult = this.mockMvc.perform(
                post("/bos/1227069312661626882/entities/query?v=0.0.6")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
                        .characterEncoding("utf-8")
                        .content("{\"sort\":[],\"entity\":{\"fields\":[\"name\",\"age\",\"address\",\"city\",\"id\"],\"entities\":[{\"code\":\"personOTOcity\",\"fields\":[\"name\",\"code\",\"un_code\",\"id\"]}]},\"pageNo\":1,\"pageSize\":20,\"startFrom\":null,\"conditions\":{\"fields\":[],\"entities\":[{\"code\":\"personOTOcity\",\"fields\":[{\"code\":\"name\",\"operation\":\"eq\",\"value\":[\"大连\"]}]}]}}")
        ).andDo(print()).andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));


        MvcResult mvcResult2 = this.mockMvc.perform(
                post("/bos/1227069312661626882/entities/query?v=0.0.58")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
                        .characterEncoding("utf-8")
                        .content("{\"sort\":[],\"entity\":{\"fields\":[\"name\",\"age\",\"address\",\"city\",\"id\"],\"entities\":[{\"code\":\"personOTOcity\",\"fields\":[\"name\",\"code\",\"un_code\",\"id\"]}]},\"pageNo\":1,\"pageSize\":20,\"startFrom\":null,\"conditions\":{\"fields\":[],\"entities\":[{\"code\":\"personOTOcity\",\"fields\":[{\"code\":\"name\",\"operation\":\"eq\",\"value\":[\"大连\"]}]}]}}")
        ).andDo(print()).andReturn();

        System.out.println(mvcResult2.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

}
