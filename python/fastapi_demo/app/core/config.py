"""
=================================================================
文件: app/core/config.py
作用: 全局配置，集中管理环境变量与配置项（对应官方骨架 app/core/config.py）
类比: Java 的 application.yml / @ConfigurationProperties

使用 pydantic-settings 自动读取 .env 文件：
    from app.core.config import settings
    print(settings.API_KEY)
=================================================================
"""
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    # .env 文件自动加载；extra="ignore" 容忍多余变量
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )

    # 项目信息
    PROJECT_NAME: str = "Contract Recognize Service"
    PROJECT_VERSION: str = "0.1.0"
    LOG_LEVEL: str = "INFO"

    # 服务端口
    SERVER_HOST: str = "0.0.0.0"
    SERVER_PORT: int = 8000


    API_KEY: str = "sk-ws-H.EDPRHPH.mpzL.MEUCIFyurL60y8NDfdfrxTtuoCLX7M4DDxvNnU9wuj7CampaAiEA-8fDEYI0Fge0jAiHfrd1bGw50g-UlGvACjeHxPZyIlY"
    BASE_URL: str = "https://dashscope.aliyuncs.com/compatible-mode/v1"
    LLM_MODEL: str = "qwen3-vl-plus"

settings = Settings()
