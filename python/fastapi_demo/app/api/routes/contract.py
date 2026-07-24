from fastapi import APIRouter

from app.schemas.request import ContractRecognizeRequest
from app.schemas.response import BaseResponse
from app.ai._ai_recognize import contract_service as ai_contract_service
from app.services.contract_service import contract_service

router = APIRouter()


@router.post("/recognize", response_model=BaseResponse, summary="合同识别（AI 识别）")
async def recognize_contract(request: ContractRecognizeRequest):
    '''
    接收 Java 后端下发的合同识别请求（minioUrl + filename），调用 AI 进行合同识别。
    请求体示例:
        {"minioUrl": "http://minio:9000/contract/xxx.pdf", "filename": "合同.pdf"}
    返回: BaseResponse，data 为 ContractRecognizeResponse
    '''
    result = ai_contract_service.ai_recognize_contract(request)
    #result = contract_service.recognize_contract(request)
    return BaseResponse(code=200, message="ok", data=result)
