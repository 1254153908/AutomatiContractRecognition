from pydantic import BaseModel, ConfigDict, Field


class ContractRecognizeRequest(BaseModel):
    '''合同识别请求
    由 Java 后端下发，携带合同文件在 MinIO 中的地址与文件名；
    Python 端仅接收这两个字段，返回设计好的假识别数据体。
    '''
    model_config = ConfigDict(populate_by_name=True)

    minio_url: str = Field(..., alias="minioUrl", description="合同文件在 MinIO 中的访问地址")
    filename: str = Field(..., description="文件名（含扩展名）")
