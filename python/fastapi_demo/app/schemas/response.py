from pydantic import BaseModel, Field
from typing import Optional, List


class BaseResponse(BaseModel):
    '''统一响应格式'''
    code: int = 200
    message: str = "ok"
    data: Optional[object] = None


class EquipmentAuditResponse(BaseModel):
    lydwh: Optional[str] = Field(default=None, description="使用单位号（辅关键字）")
    lydwm: Optional[str] = Field(default=None, description="使用单位名")
    zcbhqj: Optional[str] = Field(default=None, description="设备编号区间")
    zcflh: Optional[str] = Field(default=None, description="分类号（关键字）")
    zcmc: Optional[str] = Field(default=None, description="设备名称")
    ppxh: Optional[str] = Field(default=None, description="品牌型号")
    gg: Optional[str] = Field(default=None, description="规格")
    sl: Optional[int] = Field(default=None, description="数量")
    dj: Optional[float] = Field(default=None, description="单价")
    je: Optional[float] = Field(default=None, description="金额（辅关键字）")
    jldw: Optional[str] = Field(default=None, description="计量单位")
    cj: Optional[str] = Field(default=None, description="厂家")
    ggrq: Optional[str] = Field(default=None, description="购置日期（格式 YYYY-MM-DD，辅关键字）")


class ContractItemResponse(BaseModel):
    '''合同明细（对应 config.yaml 的 contract_items 表）'''
    item_name: str = Field(..., description="标的名称")
    spec: Optional[str] = Field(default=None, description="规格/型号")
    quantity: float = Field(..., description="数量")
    unit: Optional[str] = Field(default=None, description="单位")
    unit_price: float = Field(..., description="单价（DECIMAL(15,2)）")
    amount: float = Field(..., description="金额（DECIMAL(15,2)）")
    equipments: Optional[List[EquipmentAuditResponse]] = Field(default=None, description="该明细对应的设备入账信息列表")


class ContractRecognizeResponse(BaseModel):

    contract_no: str = Field(..., description="合同编号（唯一键 uk_contract_no）")
    party_a: str = Field(..., description="甲方")
    party_b: str = Field(..., description="乙方")
    sign_date: str = Field(..., description="签约日（LocalDate，格式 YYYY-MM-DD）")
    total_amount: float = Field(..., description="合同总金额（BigDecimal，DECIMAL(15,2)）")
    status: int = Field(..., description="状态：0待处理 1已识别 2已完成")
    items: List[ContractItemResponse] = Field(default=[], description="合同明细列表")
