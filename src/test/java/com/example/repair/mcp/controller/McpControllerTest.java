package com.example.repair.mcp.controller;

import com.example.repair.entity.RepTicket;
import com.example.repair.entity.RepTicketProcessLog;
import com.example.repair.entity.RepTicketStatusLog;
import com.example.repair.mcp.config.McpAuthFilter;
import com.example.repair.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class McpControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();

        McpController controller = new McpController(ticketService, objectMapper);
        ReflectionTestUtils.setField(controller, "serverName", "test-mcp-server");
        ReflectionTestUtils.setField(controller, "serverVersion", "2.0.0");

        McpAuthFilter filter = new McpAuthFilter();
        ReflectionTestUtils.setField(filter, "mcpToken", "test-token");

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addFilters(filter)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(post("/mcp/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"jsonrpc":"2.0","id":"1","method":"initialize","params":{}}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.message").value("Missing or invalid token"));
    }

    @Test
    void shouldAllowInitializeWithBearerToken() throws Exception {
        mockMvc.perform(post("/mcp/messages")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"jsonrpc":"2.0","id":"1","method":"initialize","params":{}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.serverInfo.name").value("test-mcp-server"))
                .andExpect(jsonPath("$.result.serverInfo.version").value("2.0.0"));
    }

    @Test
    void shouldAllowInitializeWithApiKeyHeader() throws Exception {
        mockMvc.perform(post("/mcp/messages")
                        .header("X-API-Key", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"jsonrpc":"2.0","id":"1","method":"initialize","params":{}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.protocolVersion").value("2024-11-05"));
    }

    @Test
    void shouldReturnExpandedTicketDetailIncludingProcessLogs() throws Exception {
        RepTicket ticket = new RepTicket();
        ticket.setId(100L);
        ticket.setTicketNo("WO-20260315-0001");
        ticket.setTitle("打印机无法开机");
        ticket.setDescription("办公室打印机无法启动");
        ticket.setStatus("IN_PROGRESS");
        ticket.setPriority("HIGH");
        ticket.setFaultType("HARDWARE");
        ticket.setRequesterName("张三");
        ticket.setRequesterPhone("13800000000");
        ticket.setLocation("3楼会议室");
        ticket.setCreatedAt(LocalDateTime.of(2026, 3, 15, 10, 0));

        RepTicketStatusLog statusLog = new RepTicketStatusLog();
        statusLog.setCreatedAt(LocalDateTime.of(2026, 3, 15, 10, 30));
        statusLog.setFromStatus("OPEN");
        statusLog.setToStatus("IN_PROGRESS");
        statusLog.setAction("STATUS_CHANGE");

        RepTicketProcessLog processLog = new RepTicketProcessLog();
        processLog.setCreatedAt(LocalDateTime.of(2026, 3, 15, 11, 0));
        processLog.setProcessType("REPAIR");
        processLog.setOperatorName("李维修");
        processLog.setContent("已更换电源模块");

        given(ticketService.getTicketById(100L)).willReturn(ticket);
        given(ticketService.getStatusLogs(100L)).willReturn(List.of(statusLog));
        given(ticketService.getProcessLogs(100L)).willReturn(List.of(processLog));
        given(ticketService.getEvaluation(100L)).willReturn(null);

        mockMvc.perform(post("/mcp/messages")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "jsonrpc":"2.0",
                                  "id":"2",
                                  "method":"tools/call",
                                  "params":{
                                    "name":"get_ticket_detail",
                                    "arguments":{"ticketId":100}
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].text").value(org.hamcrest.Matchers.containsString("=== 处理记录 ===")))
                .andExpect(jsonPath("$.result.content[0].text").value(org.hamcrest.Matchers.containsString("已更换电源模块")));
    }
}
