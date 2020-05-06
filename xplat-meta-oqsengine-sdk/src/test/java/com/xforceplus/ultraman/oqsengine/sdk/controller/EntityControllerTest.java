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

import java.nio.charset.Charset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@EnableWebMvc
public class EntityControllerTest extends ContextWareBaseTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void testQuery() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(
                post("/bos/1257921699227975681/entities/query?v=0.0.2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
                        .characterEncoding("utf-8")
                        .content("{\"sort\":[],\"entity\":{\"fields\":[\"name\",\"age\",\"address\",\"id\"],\"entities\":[]},\"pageNo\":1,\"pageSize\":20,\"startFrom\":null,\"conditions\":{\"fields\":[],\"entities\":[]}}")


        ).andDo(print()).andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString(Charset.forName("utf-8")));
    }
}
