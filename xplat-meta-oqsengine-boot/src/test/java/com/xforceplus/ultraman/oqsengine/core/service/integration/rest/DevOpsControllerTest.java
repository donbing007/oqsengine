package com.xforceplus.ultraman.oqsengine.core.service.integration.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.xforceplus.ultraman.oqsengine.boot.rest.DevOpsController;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
public class DevOpsControllerTest {

    @Mock
    private SyncExecutor syncExecutor;

    @InjectMocks
    private DevOpsController devOpsController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        this.mockMvc = MockMvcBuilders.standaloneSetup(devOpsController).build();
    }

    @Test
    public void testMetaImport() throws Exception {
        this.mockMvc.perform(put("/oqs/devops/import-meta/{appId}/{version}", "1", 1)
            .accept(MediaType.APPLICATION_JSON)
            .content("test")
        ).andDo(print())
            .andExpect(status().isOk());

    }
}
