from app.schemas.request import ContractRecognizeRequest
from app.schemas.response import ContractRecognizeResponse, ContractItemResponse, EquipmentAuditResponse


class ContractService:


    def recognize_contract(self, request: ContractRecognizeRequest) -> ContractRecognizeResponse:

        items = [
            ContractItemResponse(
                item_name="软件开发服务",
                spec="标准版",
                quantity=1,
                unit="项",
                unit_price=80000.00,
                amount=80000.00,
                equipments=[
                    EquipmentAuditResponse(
                        lydwh="SZ001",
                        lydwm="深圳研发中心",
                        zcbhqj="SB2026001-005",
                        zcflh="02010100",
                        zcmc="软件开发服务平台",
                        ppxh="自研V3.0",
                        gg="企业版/支持500并发",
                        sl=1,
                        dj=80000.00,
                        je=80000.00,
                        jldw="套",
                        cj="某某软件技术有限公司",
                        ggrq="2026-07-01",
                    ),
                ],
            ),
            ContractItemResponse(
                item_name="技术支持服务",
                spec="1年",
                quantity=12,
                unit="月",
                unit_price=5000.00,
                amount=60000.00,
                equipments=[
                    EquipmentAuditResponse(
                        lydwh="SZ001",
                        lydwm="深圳研发中心",
                        zcbhqj="SB2026006-010",
                        zcflh="02010200",
                        zcmc="技术运维服务",
                        ppxh="7x24标准服务",
                        gg="远程+现场支持",
                        sl=1,
                        dj=60000.00,
                        je=60000.00,
                        jldw="项",
                        cj="某某软件技术有限公司",
                        ggrq="2026-07-01",
                    ),
                ],
            ),
        ]

        return ContractRecognizeResponse(
            contract_no="HT2026-000123",
            party_a="腾讯科技（深圳）有限公司",
            party_b="某某软件技术有限公司",
            sign_date="2026-07-01",
            total_amount=140000.00,
            status=1,
            items=items,
        )


contract_service = ContractService()
