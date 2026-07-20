"""
=================================================================
文件: app/main.py
作用: FastAPI 应用入口，创建 app 实例并注册路由/中间件/异常处理
类比: Java 的 @SpringBootApplication 主类 + main 方法

启动命令（官方骨架推荐）:
    fastapi dev app/main.py
    # 等价于:
    uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
=================================================================
"""
import logging

from fastapi import FastAPI
from fastapi.exceptions import RequestValidationError

from app.api.main import api_router
from app.core.config import settings
from app.exceptions.handlers import (
    AppException,
    app_exception_handler,
    validation_exception_handler,
    general_exception_handler,
)
from app.middleware.log_middleware import log_middleware

logging.basicConfig(level=settings.LOG_LEVEL)
logger = logging.getLogger("app")

app = FastAPI(title=settings.PROJECT_NAME, version=settings.PROJECT_VERSION)

# 中间件：请求日志
app.middleware("http")(log_middleware)

# 全局异常处理
app.add_exception_handler(AppException, app_exception_handler)
app.add_exception_handler(RequestValidationError, validation_exception_handler)
app.add_exception_handler(Exception, general_exception_handler)

# 路由（统一前缀 /api/v1）
app.include_router(api_router, prefix="/api/v1")


app.include_router(api_router, prefix="")


@app.get("/")
async def root():
    return {"message": f"{settings.PROJECT_NAME} is running"}
