"""
=================================================================
文件: app/middleware/log_middleware.py
作用: HTTP 请求日志中间件，记录每个请求的方法、路径、耗时、状态码
类比: Java 的 Filter (javax.servlet.Filter) 或 Spring Interceptor
=================================================================
"""
import logging
import time

from fastapi import Request

logger = logging.getLogger("app.middleware")


async def log_middleware(request: Request, call_next):
    start = time.time()
    logger.info("--> %s %s", request.method, request.url.path)

    response = await call_next(request)

    elapsed = time.time() - start
    logger.info(
        "<-- %s %s %s %.3fs",
        request.method,
        request.url.path,
        response.status_code,
        elapsed,
    )
    return response
