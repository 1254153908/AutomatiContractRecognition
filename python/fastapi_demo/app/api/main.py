"""
=================================================================
文件: app/api/main.py
作用: 聚合所有业务路由（对应官方骨架 app/api/main.py 中的 api_router）
=================================================================
"""
from fastapi import APIRouter

from app.api.routes import contract

api_router = APIRouter()
api_router.include_router(contract.router, prefix="/contract", tags=["合同识别"])
