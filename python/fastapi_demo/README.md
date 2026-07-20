# Contract Recognize Service

基于 FastAPI 的合同识别服务（标准骨架，结构对应官方 `fastapi create` 模板）。

## 目录结构

```
app/
├── main.py            # 应用入口，创建 app 并注册路由/中间件/异常
├── api/
│   ├── main.py        # api_router 聚合各业务路由（前缀 /api/v1）
│   └── routes/
│       └── contract.py# 合同识别接口
├── core/
│   └── config.py      # Settings（pydantic-settings，读取 .env）
├── schemas/           # 请求/响应模型
├── services/          # 业务逻辑
├── ai/                # 大模型识别脚本
├── middleware/        # HTTP 日志中间件
├── exceptions/        # 全局异常处理
└── utils/             # 工具函数
tests/                 # pytest 测试
```

## 本地启动

```bash
# 激活虚拟环境
.venv/Scripts/activate

# 开发模式（自带热重载，推荐）
fastapi dev app/main.py

# 或等价的 uvicorn 写法
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

## 测试

```bash
pytest tests/ -v
```

## 接口

- `GET  /`                         健康检查
- `POST /api/v1/contract/recognize` 合同识别（接收 minioUrl + filename，返回识别数据）
- `POST /ContractRecognize`         兼容 Java 后端的直接调用入口
