# device-repair-service

设备报修工单服务后端，内置一个基于 HTTP 的 MCP 服务，供 OpenClaw 等 AI Agent 调用。

## 系统整体架构

系统由两类对外能力组成：

1. **业务 REST API**：面向前端、管理后台或第三方系统，完成工单创建、查询、分配、状态更新、撤销、评价等业务操作。
2. **MCP API**：面向 AI Agent，通过标准 MCP/JSON-RPC 协议调用工单能力，让智能体可以把报修系统当作“工具集”使用。

### 分层说明

```text
┌──────────────────────────────────────────────────────────────┐
│                        调用方 / Client                       │
│  前端页面 / 管理后台 / 第三方系统 / OpenClaw / AI Agent      │
└───────────────────────┬───────────────────────┬──────────────┘
                        │                       │
                REST / JSON                MCP / JSON-RPC
                        │                       │
            /api/v1/tickets/**             /mcp, /mcp/messages
                        │                       │
        TicketController                 McpController
                        └───────────────┬───────┘
                                        │
                                  TicketService
                                        │
      ┌─────────────────────────────────┼─────────────────────────────────┐
      │                                 │                                 │
RepTicketMapper              RepTicketStatusLogMapper       其他 Mapper
      │                                 │                                 │
      └─────────────────────────────────┴─────────────────────────────────┘
                                        │
                                      MySQL
```

### 关键组件职责

- **`TicketController`**：提供 `/api/v1/tickets` 下的标准 HTTP 业务接口。
- **`McpController`**：提供 `/mcp` 和 `/mcp/messages` 两个 MCP 入口，支持 `initialize`、`tools/list`、`tools/call`。
- **`McpAuthFilter`**：只对 `/mcp` 路径做鉴权，支持 `Authorization: Bearer <token>` 或 `X-API-Key: <token>`。
- **`TicketService` / `TicketServiceImpl`**：封装工单生命周期、状态校验、分配、撤销、评价等核心业务逻辑。
- **MyBatis Plus Mapper**：负责和数据库表交互。
- **MySQL**：持久化工单主数据、状态变更、处理记录、评价、撤销记录。

## 表结构说明

数据库脚本位于 `src/main/resources/schema.sql`，核心表如下：

### 1. `rep_tickets`：报修工单主表

用于存储工单主体信息，是整个系统的核心主表。

**主要字段**

- `id`：主键
- `ticket_no`：工单编号，如 `WO-20260316-0001`
- `title` / `description`：工单标题、故障描述
- `fault_type`：故障类型
- `priority`：优先级
- `status`：当前状态
- `requester_name` / `requester_phone` / `requester_dept`：报修人信息
- `location`：故障位置
- `assigned_to` / `assigned_to_id`：当前处理人
- `estimated_completion_time` / `actual_start_time` / `actual_completion_time`：关键时间节点
- `cost`：维修费用
- `image_urls` / `attachment_urls`：图片与附件
- `is_sla_breached`：是否超时
- `closed_by` / `closed_time` / `close_comment`：关闭信息
- `is_evaluated`：是否已评价
- `created_at` / `updated_at` / `deleted_at`：通用审计字段

**作用理解**

- 一条记录就代表一张报修工单。
- 其他业务表都通过 `ticket_id` 关联到这张主表。

### 2. `rep_ticket_status_logs`：工单状态流转历史表

记录工单状态的每一次变化，适合做流程追踪和审计。

**主要字段**

- `ticket_id`：关联 `rep_tickets.id`
- `from_status` / `to_status`：变更前后状态
- `operator_id` / `operator_name`：操作人
- `action`：动作类型，如创建、分配、状态更新、撤销
- `comment`：操作说明
- `created_at`：记录时间

**作用理解**

- 一张工单会对应多条状态流转记录。
- 用于回答“工单什么时候被谁从什么状态改成了什么状态”。

### 3. `rep_ticket_process_logs`：工单处理过程记录表

记录维修过程中的处理动作和过程说明。

**主要字段**

- `ticket_id`：关联 `rep_tickets.id`
- `process_type`：处理类型，如维修、备注、转单、暂停、恢复
- `operator_id` / `operator_name`：操作人
- `content`：处理内容
- `images`：过程图片
- `duration_minutes`：处理耗时
- `created_at`：记录时间

**作用理解**

- 与状态流转表不同，这张表记录的是“怎么处理的”，不是“状态怎么变的”。
- 一张工单可以有多条处理过程记录。

### 4. `rep_ticket_evaluations`：工单评价表

记录报修人对工单处理结果的满意度评价。

**主要字段**

- `ticket_id`：关联 `rep_tickets.id`，且唯一
- `evaluator_name`：评价人
- `overall_score`：总体评分
- `response_speed_score` / `service_attitude_score` / `technical_level_score`：分项评分
- `comment`：评价内容
- `reply_content` / `replied_at`：服务方回复

**作用理解**

- 一张工单最多一条评价记录。
- 适合做服务质量统计与回访分析。

### 5. `rep_ticket_cancels`：工单撤销记录表

记录工单被撤销的原因和时间。

**主要字段**

- `ticket_id`：关联 `rep_tickets.id`
- `canceler_name`：撤销人
- `cancel_reason`：撤销原因
- `cancel_time`：撤销时间

**作用理解**

- 用于追踪工单为什么被取消。
- 便于后续统计误报、重复报修等场景。

### 表关系速览

```text
rep_tickets (1)
 ├── rep_ticket_status_logs (N)
 ├── rep_ticket_process_logs (N)
 ├── rep_ticket_cancels (N)
 └── rep_ticket_evaluations (0..1)
```

## 接口文档

启动服务后可直接通过 Swagger 查看接口：

- Swagger UI：`http://127.0.0.1:8080/swagger-ui.html`
- 业务 REST API 文档：`http://127.0.0.1:8080/v3/api-docs/default`
- MCP API 文档：`http://127.0.0.1:8080/v3/api-docs/mcp`

其中：

- `default` 分组展示 `/api/**` 的业务接口；
- `mcp` 分组展示 `/mcp`、`/mcp/messages` 的 MCP 接口。

## 启动

1. 准备好数据库配置。
2. 设置 MCP 认证令牌：

```bash
export MCP_AUTH_TOKEN=your-mcp-token
```

3. 启动服务：

```bash
mvn spring-boot:run
```

默认端口为 `8080`。

## MCP 接口

- 推荐地址：`POST /mcp`
- 兼容地址：`POST /mcp/messages`
- 鉴权方式二选一：
  - `Authorization: Bearer <MCP_AUTH_TOKEN>`
  - `X-API-Key: <MCP_AUTH_TOKEN>`

### MCP 能力一览

当前 MCP 已暴露以下工具能力：

- `create_repair_ticket`：创建报修工单
- `list_repair_tickets`：按状态/优先级/报修人查询工单
- `get_ticket_detail`：查询工单详情、状态日志、处理记录、评价
- `update_ticket_status`：更新工单状态
- `assign_ticket`：分配工单给维修人员
- `cancel_ticket`：撤销工单
- `evaluate_ticket`：对工单进行评价

### 初始化请求示例

```bash
curl -X POST http://127.0.0.1:8080/mcp \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-mcp-token" \
  -d '{
    "jsonrpc": "2.0",
    "id": "init-1",
    "method": "initialize",
    "params": {}
  }'
```

### 查询工具列表示例

```bash
curl -X POST http://127.0.0.1:8080/mcp \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-mcp-token" \
  -d '{
    "jsonrpc": "2.0",
    "id": "tools-1",
    "method": "tools/list",
    "params": {}
  }'
```

## 如何让 OpenClaw 使用这个 MCP

OpenClaw 需要把这个服务注册成一个远程 HTTP MCP 服务器。不同版本的 OpenClaw 配置字段可能略有差异，但核心信息是一样的：

- URL：`http://你的服务地址:8080/mcp`
- Header：
  - `Authorization: Bearer <MCP_AUTH_TOKEN>`
  - 或 `X-API-Key: <MCP_AUTH_TOKEN>`

可以参考下面这份常见的 MCP 配置写法，把字段名映射到你所使用的 OpenClaw 版本：

```json
{
  "mcpServers": {
    "device-repair-service": {
      "type": "http",
      "url": "http://127.0.0.1:8080/mcp",
      "headers": {
        "Authorization": "Bearer your-mcp-token"
      }
    }
  }
}
```

如果你的 OpenClaw 版本区分 `transport`，可设置为等价的 HTTP 传输，并继续使用同一个 URL 与请求头。

### 接入检查清单

1. OpenClaw 能访问到服务地址。
2. `MCP_AUTH_TOKEN` 已在服务端配置。
3. OpenClaw 发送了正确的 `Authorization` 或 `X-API-Key` 请求头。
4. OpenClaw 指向的是 `/mcp`（推荐）或 `/mcp/messages`。
5. 先用 `initialize` 和 `tools/list` 验证连通性，再正式调用工具。
