package com.example.repair.config;

import com.example.repair.DeviceRepairApplication;
import com.example.repair.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = DeviceRepairApplication.class,
        properties = {
                "mcp.auth.token=test-token",
                "spring.main.lazy-initialization=true",
                "spring.datasource.url=jdbc:mysql://127.0.0.1:3306/repair_db?useSSL=false&serverTimezone=Asia/Shanghai&connectTimeout=1000&socketTimeout=1000",
                "spring.datasource.username=test",
                "spring.datasource.password=test",
                "spring.datasource.hikari.initialization-fail-timeout=0"
        }
)
@AutoConfigureMockMvc
class SwaggerConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @Test
    void shouldExposeRestApisInDefaultGroup() throws Exception {
        mockMvc.perform(get("/v3/api-docs/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/tickets']").exists());
    }

    @Test
    void shouldExposeMcpApisInMcpGroup() throws Exception {
        mockMvc.perform(get("/v3/api-docs/mcp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/mcp']").exists())
                .andExpect(jsonPath("$.paths['/mcp/messages']").exists())
                .andExpect(jsonPath("$.paths['/mcp'].post.summary").value("MCP消息处理"));
    }
}
