package com.xforceplus.ultraman.oqsengine.sdk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.sdk.ContextWareBaseTest;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Response;
import com.xforceplus.xplat.galaxy.framework.dispatcher.messaging.GeneralResponse;
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
import java.nio.charset.StandardCharsets;

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
                post("/bos/1257947147311431681/entities/query?v=0.0.3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
                        .characterEncoding("utf-8")
                        .content("{\"sort\":[],\"entity\":{\"fields\":[\"name\",\"age\",\"address\",\"id\"],\"entities\":[]},\"pageNo\":1,\"pageSize\":20,\"startFrom\":null,\"conditions\":{\"fields\":[],\"entities\":[]}}")


        ).andDo(print()).andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }


    @Test
    public void testQueryWired() throws Exception {

        Thread.sleep(10000);

        MvcResult mvcResult = this.mockMvc.perform(
                post("/bos/1260142973288783874/entities/query?v=0.0.6")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
                        .characterEncoding("utf-8")
                        .content("{\"sort\":[],\"entity\":{\"fields\":[\"name\",\"age\",\"address\",\"id\"],\"entities\":[]},\"pageNo\":1,\"pageSize\":20,\"startFrom\":null,\"conditions\":{\"fields\":[],\"entities\":[]}}")


        ).andDo(print()).andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }




    @Test
    public void testQueryLeftJoin() throws Exception  {

        Thread.sleep(10000);

        /**
         * {"sort":[],"entity":{"fields":["name","age","address","city","id"],"entities":[{"code":"personOTOcity","fields":["name","code","un_code","id"]}]},"pageNo":1,"pageSize":20,"startFrom":null,"conditions":{"fields":[],"entities":[]}}
         */




//        1258244393640857601
//        {"name":"大连","code":"DL","un_code":"201020"}

        //新建一个城市
        MvcResult mvcResult2 = this.mockMvc.perform(
                post("/bos/1258244393640857601/entities?v=0.0.6")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
                        .characterEncoding("utf-8")
                        .content(" {\"name\":\"大连\",\"code\":\"DL\",\"un_code\":\"201020\"}")
        ).andDo(print()).andReturn();

        String json = mvcResult2.getResponse().getContentAsString(StandardCharsets.UTF_8);


        ObjectMapper mapper = new ObjectMapper();
        Response response = mapper.readValue(json, Response.class);


        //新建一个人
        MvcResult mvcResult1 = this.mockMvc.perform(
                post("/bos/1257947147311431681/entities?v=0.0.6")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
                        .characterEncoding("utf-8")
                        .content("{\"name\":\"阿提拉\",\"age\":20,\"personOTOcity.id\":\""+ response.getResult()  +"\",\"city\":\"3\"}")
        ).andDo(print()).andReturn();


        MvcResult mvcResult = this.mockMvc.perform(
                post("/bos/1257947147311431681/entities/query?v=0.0.6")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
                        .characterEncoding("utf-8")
                        .content("{\"sort\":[],\"entity\":{\"fields\":[\"name\",\"age\",\"address\",\"city\",\"id\"],\"entities\":[{\"code\":\"personOTOcity\",\"fields\":[\"name\",\"code\",\"un_code\",\"id\"]}]},\"pageNo\":1,\"pageSize\":20,\"startFrom\":null,\"conditions\":{\"fields\":[],\"entities\":[]}}")


        ).andDo(print()).andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    public void  testQueryWithCity() throws Exception{

        Thread.sleep(5000);

        MvcResult mvcResult = this.mockMvc.perform(
                post("/bos/1257947147311431681/entities/query?v=0.0.6")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
                        .characterEncoding("utf-8")
                        .content("{\"sort\":[],\"entity\":{\"fields\":[\"name\",\"age\",\"address\",\"city\",\"id\"],\"entities\":[{\"code\":\"personOTOcity\",\"fields\":[\"name\",\"code\",\"un_code\",\"id\"]}]},\"pageNo\":1,\"pageSize\":20,\"startFrom\":null,\"conditions\":{\"fields\":[],\"entities\":[{\"code\":\"personOTOcity\",\"fields\":[{\"code\":\"name\",\"operation\":\"eq\",\"value\":[\"大连\"]}]}]}}")
        ).andDo(print()).andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    public void  testWiredQuery() throws Exception{

        Thread.sleep(20000);

        MvcResult mvcResult = this.mockMvc.perform(
                post("/bos/1260142809153085442/entities/query?v=0.0.2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
                        .characterEncoding("utf-8")
                        .content("{\"pageNo\":1,\"pageSize\":100,\"entity\":{\"fields\":[\"name\",\"id\"],\"entities\":[]},\"conditions\":{\"fields\":[],\"entities\":[]}}")
        ).andDo(print()).andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }
}
