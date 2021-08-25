package com.xforceplus.ultraman.oqsengine.core.service.integration.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.xforceplus.ultraman.oqsengine.boot.rest.DevOpsController;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.impl.DevOpsManagementServiceImpl;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.dto.metrics.MetaMetrics;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
    private MetaManager metaManager;

    @Mock
    private DevOpsManagementServiceImpl devOpsManagementService;

    @Mock
    private EntityManagementService entityManagementService;

    @Mock
    private EntitySearchService entitySearchService;


    @InjectMocks
    private DevOpsController devOpsController;

    private MockMvc mockMvc;

    /**
     * 测试初始化.
     */
    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        Mockito.when(metaManager.showMeta("1"))
            .thenReturn(Optional.of(new MetaMetrics(1, "0", "1", new ArrayList<>())));

        this.mockMvc = MockMvcBuilders.standaloneSetup(devOpsController).build();
    }

    @Test
    public void testMetaImport() throws Exception {
        this.mockMvc.perform(put("/oqs/devops/import-meta/{appId}/{env}/{version}", "1", "0", 1)
            .accept(MediaType.APPLICATION_JSON)
            .content("test")
        ).andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    public void testShowMeta() throws Exception {
        this.mockMvc.perform(get("/oqs/devops/show-meta/{appId}", "1")
        ).andDo(print())
            .andExpect(status().isOk());
    }
}
