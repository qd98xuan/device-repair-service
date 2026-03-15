# device-repair-service

设备报修工单服务后端，内置一个基于 HTTP 的 MCP 服务，供 OpenClaw 等 AI Agent 调用。

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
