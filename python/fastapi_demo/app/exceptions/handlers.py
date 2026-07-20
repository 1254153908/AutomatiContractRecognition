"""
=================================================================
文件: app/exceptions/handlers.py
作用: 全局异常处理器，捕获未处理异常并返回统一格式的错误响应
类比: Java 的 @ControllerAdvice + @ExceptionHandler
=================================================================
"""
import logging

from fastapi import Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse

logger = logging.getLogger("app.exceptions")


class AppException(Exception):
    '''自定义业务异常'''

    def __init__(self, message: str, code: int = 400):
        self.message = message
        self.code = code


async def app_exception_handler(request: Request, exc: AppException):
    '''业务异常处理 → 返回 {"code":..., "message":"...", "data":null}'''
    return JSONResponse(
        status_code=exc.code,
        content={"code": exc.code, "message": exc.message, "data": None},
    )


async def validation_exception_handler(request: Request, exc: RequestValidationError):
    '''参数校验失败 → 返回 422'''
    return JSONResponse(
        status_code=422,
        content={"code": 422, "message": "参数校验失败", "data": exc.errors()},
    )


async def general_exception_handler(request: Request, exc: Exception):
    '''通用异常兜底 → 返回 500'''
    logger.exception("Unhandled exception: %s", exc)
    return JSONResponse(
        status_code=500,
        content={"code": 500, "message": f"服务器内部错误: {exc}", "data": None},
    )
