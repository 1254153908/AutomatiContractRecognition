# 📄 Contract Manager — 智能合同管理系统

> **Spring Boot + FastAPI + Vue 3** · 大模型视觉识别 · 设备入账审核 · 全栈合同管理平台

一个集合同录入、AI 智能识别、设备资产入账于一体的全栈管理系统。上传合同 PDF 或图片，后端自动调用多模态大模型（Vision LLM）直接识别文档内容并提取结构化字段，支持多级明细和设备入账审核流程。

---

## ✨ 功能亮点

- **📑 合同 CRUD** — 增删改查合同基本信息（编号、甲乙双方、日期、金额、状态流转）
- **🤖 AI 智能识别** — 上传 PDF，Java → MinIO → Python → 多模态大模型看图识别 → 回填表单
- **📋 合同明细管理** — 每条合同支持多条明细，含产品名称、数量、单价、单位、型号
- **🏷 设备入账审核** — 明细级联录入设备资产信息（单位号、分类号、品牌/规格、厂家等），自动标记为待审核
- **🔄 完整流程** — `待处理 → 已识别 → 已完成` 状态机，全程可追溯

---

## 🧱 技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| **后端** | Java 17 · Spring Boot 3.2.0 · MyBatis-Plus 3.5.5 | REST API，端口 **1005** |
| **数据库** | MySQL 8.0 (utf8mb4) | 端口映射 3307→3306，库 `contract_db` |
| **对象存储** | MinIO | 存储原始 PDF，生成预签名 URL 供 Python 消费 |
| **AI 服务** | Python 3.9+ · FastAPI · PyMuPDF · pypdfium2 · OpenAI SDK | 多模态大模型视觉识别，端口 **8000** |
| **前端** | Vue 3 · Vite 5 | SPA，Vite dev 代理到后端 |

---

## 🚀 快速开始

### 前置条件

- **JDK 17+**、**Maven 3.8+**
- **MySQL 8.0**（或 Docker 启动）
- **Python 3.9+**（配 `venv`）
- **Node.js 18+**（配 `npm/pnpm`）
- **MinIO**（可选，本地目录模式也支持）

### 1. 初始化数据库

```bash
mysql -u root -p < sql/init.sql
```

脚本会自动创建 `contract_db` 库和三张业务表。

### 2. 启动 Java 后端

```bash
mvn clean package -DskipTests
java -jar target/Contract-1.0-SNAPSHOT.jar
# 或 IDE 中直接运行 ContractApplication.java
```

> 应用启动在 `http://localhost:1005`。确保 `application.yml` 中数据库/MinIO 连接配置正确。

### 3. 启动 Python AI 服务

```bash
cd python/fastapi_demo
python -m venv .venv
source .venv/bin/activate   # Windows: .venv\Scripts\activate
pip install -e .
cp .env.example .env        # 填入你的 AI API Key
fastapi dev app/main.py
```

> AI 服务启动在 `http://localhost:8000`，Swagger 文档在 `http://localhost:8000/docs`。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

> 默认启动在 `http://localhost:5173`，`vite.config.js` 已配置 API 代理到后端 `1005` 端口。

---

## 📁 项目结构

```
Contract/
├── pom.xml                         # Maven 构建配置
├── src/main/java/org/huhu/contract/
│   ├── ContractApplication.java    # Spring Boot 入口
│   ├── controller/                 # REST 控制器
│   ├── service/                    # 业务接口 & 实现
│   ├── entity/                     # 数据库实体
│   ├── mapper/                     # MyBatis Mapper 接口
│   ├── Bo/                         # 入参 DTO
│   ├── Vo/                         # 出参 DTO
│   └── common/R.java               # 统一响应 {code, msg, data}
├── src/main/resources/
│   ├── application.yml             # 数据库 / MinIO / Python 地址配置
│   └── mapper/                     # MyBatis XML
├── sql/init.sql                    # 数据库建表脚本
├── frontend/                       # Vue 3 前端
│   ├── package.json
│   ├── vite.config.js
│   └── src/components/             # 页面组件
├── python/fastapi_demo/            # Python AI 识别服务
│   ├── pyproject.toml
│   └── app/
│       ├── main.py                 # FastAPI 入口
│       ├── api/routes/             # 路由
│       ├── schemas/                # Pydantic 模型
│       └── services/               # AI 视觉识别逻辑
├── docker/                         # Docker Compose 编排
└── uploads/                        # 本地文件上传目录 (gitignored)
```

---

## 🔄 核心流程

```
┌──────┐  PDF上传   ┌─────────┐  预签名URL  ┌────────────┐
│ 前端  │ ────────→ │  Java   │ ─────────→ │ Python AI  │
│ Vue3 │ ←──────── │ :1005   │ ←───────── │ :8000      │
└──────┘  结构化JSON └─────────┘  识别结果   └────────────┘
                │
                ▼
         ┌──────────┐
         │  MySQL   │  合同+明细+设备入账
         │ :3307    │
         └──────────┘
                │
                ▼
         ┌──────────┐
         │  MinIO   │  PDF 对象存储
         │ :9000    │
         └──────────┘
```

### 识别流程详解

```
PDF/图片 → PyMuPDF 提取文本 → 智能筛选目标页（关键词/表格/数字占比）
         → pypdfium2 渲染为 JPEG 图片
         → 多模态大模型（Vision LLM）直接看图理解并输出结构化 JSON
         → 回填到前端表单
```

> **不是传统 OCR**：项目没有使用 Tesseract 或 PaddleOCR 等 OCR 引擎，而是通过 PyMuPDF + pypdfium2 做页面渲染预处理后，交由视觉大模型（如 GPT-4o）直接理解文档图像内容，兼顾文字识别和语义理解。

---

## 🔌 API 概览

| 方法 | 端点 | 说明 |
|------|------|------|
| `POST` | `/contract` | 新增合同（含明细 & 设备入账信息） |
| `PUT` | `/contract` | 修改合同 |
| `GET` | `/contract/{id}` | 查询单个合同 |
| `DELETE` | `/contract/{id}` | 删除合同 |
| `GET` | `/contract/list` | 合同列表 |
| `POST` | `/contract/recognize` | 上传 PDF 进行 AI 识别 |
| `POST` | `/contract/items/{itemId}/audits` | 保存明细的设备入账信息 |
| `GET` | `/contract/items/{itemId}/audits` | 查询明细的设备入账信息 |

---

## 📝 项目约定

- **状态码**：`200` 成功 / `404` 资源不存在 / `500` 默认失败
- **统一响应格式**：`{"code": 200, "data": {...}, "msg": "ok"}`
- **金额字段**：BigDecimal / DECIMAL(15,2)，避免浮点精度问题
- **Long 主键**：统一加 `@JsonFormat(shape = STRING)`，防止 JavaScript 精度丢失
- **外键级联**：删除合同时自动删除关联明细及设备入账记录
- **命名规范**：数据库 snake_case、Java 驼峰、Service 接口后缀 `ServiceIntreface`

---

## 📄 License

MIT

---

## 🤝 协作参考

- [Java-Python 图片识别协作方案](./Java-Python图片识别协作方案.md)
- [文件上传与路径说明](./文件上传与路径说明.md)
- [项目约定与 OpenSpec 知识沉淀](./项目约定与OpenSpec知识沉淀.md)
