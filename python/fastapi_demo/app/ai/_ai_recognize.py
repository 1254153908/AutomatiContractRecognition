import os
import json
import base64
import logging
from io import BytesIO
from openai import OpenAI
from dotenv import load_dotenv
import pypdfium2 as pdfium
import fitz  # PyMuPDF, 用于提取 PDF 文本和筛选目标页面

from app.schemas.request import ContractRecognizeRequest
from app.schemas.response import ContractRecognizeResponse, ContractItemResponse, EquipmentAuditResponse
from app.core.config import settings

load_dotenv(override=False)
logger = logging.getLogger(__name__)


class AiContractService:
    def __init__(self):
        self.client = OpenAI(
            api_key=settings.API_KEY,
            base_url=settings.BASE_URL,
        )

        self.system_prompt = """
You are a helpful assistant specialized in contract document recognition and parsing.
Your task is to find the main pages of contracts and extract key information from contract files and return the results in JSON format.
The response must follow this structure:
{
    "contract_no": "合同编号",
    "party_a": "甲方名称",
    "party_b": "乙方名称",
    "sign_date": "签署日期 (格式: YYYY-MM-DD)",
    "total_amount": 总金额 (浮点数),
    "status": 状态码 (整数),
    "items": [
        {
            "item_name": "项目名称",
            "spec": "规格/描述",
            "quantity": 数量 (整数或浮点数),
            "unit": "单位",
            "unit_price": 单价 (浮点数),
            "amount": 金额 (浮点数),
            "equipments": [
                {
                    "lydwh": "使用单位号",
                    "lydwm": "使用单位名",
                    "zcbhqj": "设备编号区间",
                    "zcflh": "分类号",
                    "zcmc": "设备名称",
                    "ppxh": "品牌型号",
                    "gg": "规格",
                    "sl": 数量 (整数),
                    "dj": 单价 (浮点数),
                    "je": 金额 (浮点数),
                    "jldw": "计量单位",
                    "cj": "厂家",
                    "ggrq": "购置日期 (格式: YYYY-MM-DD)"
                }
            ]
        }
    ]
}
Instructions:
1. Carefully read and analyze the contract document.
2. Ensure dates are in YYYY-MM-DD format.
3. Calculate total_amount as the sum of all item amounts.
4. For each contract item, try to extract the corresponding equipment information (equipments array) if visible in the document, including usage department, category number, device name, brand/model, specs, quantity, unit price, amount, measurement unit, manufacturer, and purchase date.
5. If a field is not visible in the document, set it to null (not empty string).
6. Return only valid JSON, no additional text or explanations.
"""

    def _download_file(self, url: str) -> bytes:
        """从 MinIO 或其他 URL 下载文件内容"""
        import requests
        response = requests.get(url)
        response.raise_for_status()
        return response.content

    def _find_main_pages(self, pdf_bytes: bytes) -> list:
        """
        使用 PyMuPDF 智能筛选 PDF 中包含关键信息的页面。
        规则：
          A. 包含关键词（合同、合计、明细、设备、规格等）
          B. 有表格且文本量适中（50-3000 词）
          C. 文本量大且数字占比 > 10%
        如果所有页都提取不到文本（可能是扫描件），则返回全部页面。
        返回：页码列表（从 0 开始，用于后续 pypdfium2 渲染）
        """
        doc = fitz.open(stream=pdf_bytes, filetype="pdf")
        total_pages = len(doc)
        target_pages = []
        total_text_length = 0  # 统计全文可提取文本总长度

        # 合同相关关键词
        keywords = [
            "合同", "甲方", "乙方", "总计", "合计", "小计", "明细",
            "设备", "规格", "型号", "数量", "单价", "金额", "厂家",
            "计量单位", "购置日期", "品牌", "品名", "合同编号",
            "Schedule", "Total", "Balance", "Quantity", "Unit Price", "Amount",
        ]

        for page_num in range(total_pages):
            page = doc[page_num]
            text = page.get_text()
            words = page.get_text("words")
            word_count = len(words)
            total_text_length += len(text.strip())

            # 检测表格
            tables = page.find_tables()
            has_table = len(tables.tables) > 0 if tables else False

            is_target = False

            # 规则A：包含关键词
            if any(kw in text for kw in keywords):
                is_target = True

            # 规则B：有表格且文本量适中（排除纯图片页或极短页）
            if has_table and 50 < word_count < 3000:
                is_target = True

            # 规则C：文本量大且数字占比高（如设备清单页）
            digit_count = sum(c.isdigit() for c in text)
            if word_count > 100 and (digit_count / max(word_count, 1)) > 0.1:
                is_target = True

            if is_target:
                target_pages.append(page_num)

        doc.close()

        # 如果没筛到任何页，说明可能是扫描件或全文无文本，降级为全部页面
        if not target_pages:
            logger.warning(
                f"[WARN] 未筛选到目标页 (总页数: {total_pages}, 全文文本: {total_text_length} 字符), "
                f"可能是扫描件PDF, 将使用全部页面"
            )
            target_pages = list(range(total_pages))

        return target_pages

    def _pdf_to_image_base64_list(self, pdf_bytes: bytes, page_indices: list = None) -> list:
        """使用 pypdfium2 将 PDF 指定页面转为 JPEG Base64 列表（无需 poppler）"""
        pdf = pdfium.PdfDocument(pdf_bytes)
        if page_indices is None:
            total_pages = min(len(pdf), 50)
            page_indices = list(range(total_pages))
        images = []
        for i in page_indices:
            if i >= len(pdf):
                continue
            page = pdf[i]
            bitmap = page.render(scale=2)  # 2x 放大保证清晰度
            pil_image = bitmap.to_pil()
            buffered = BytesIO()
            pil_image.save(buffered, format="JPEG")
            data_url = f"data:image/jpeg;base64,{base64.b64encode(buffered.getvalue()).decode()}"
            images.append(data_url)
        return images

    def ai_recognize_contract(self, request: ContractRecognizeRequest) -> ContractRecognizeResponse:
        logger.info(f"[STEP 1] 开始处理合同识别请求, URL: {request.minio_url}, 文件名: {request.filename}")

        # 1. 获取文件内容
        file_content = self._download_file(request.minio_url)
        logger.info(f"[STEP 2] 文件下载完成, 大小: {len(file_content)} bytes")

        # 2. 预处理：筛选目标页面 + 转图片
        image_list = []
        if request.minio_url.lower().endswith('.pdf'):
            logger.info("[STEP 3] 检测到 PDF 文件, 正在智能筛选目标页面...")
            target_pages = self._find_main_pages(file_content)
            logger.info(f"[STEP 3] 筛选完成, 共 {len(target_pages)} 个目标页: {[p+1 for p in target_pages]}")
            logger.info("[STEP 4] 正在将目标页转为图片...")
            image_list = self._pdf_to_image_base64_list(file_content, page_indices=target_pages)
            logger.info(f"[STEP 4] PDF 转图片完成, 共 {len(image_list)} 张")
        else:
            logger.info("[STEP 3] 检测到图片文件, 正在转 Base64...")
            b64 = base64.b64encode(file_content).decode()
            ext = "png" if request.minio_url.lower().endswith('.png') else "jpeg"
            image_list = [f"data:image/{ext};base64,{b64}"]
            logger.info("[STEP 3] 图片转 Base64 完成")

        # 3. 调用视觉大模型
        logger.info(f"[STEP 5] 正在调用大模型: {settings.LLM_MODEL}, BASE_URL: {settings.BASE_URL}, 图片数量: {len(image_list)}")
        try:
            # 构建多图片的 content 列表
            user_content = []
            for img_url in image_list:
                user_content.append({
                    "type": "image_url",
                    "image_url": {"url": img_url}
                })
            user_content.append({
                "type": "text",
                "text": "请识别合同文件中的关键信息，并以 JSON 格式返回。"
            })

            completion = self.client.chat.completions.create(
                model=settings.LLM_MODEL,
                messages=[
                    {
                        "role": "system",
                        "content": self.system_prompt,
                    },
                    {
                        "role": "user",
                        "content": user_content,
                    },
                ],
                response_format={"type": "json_object"}
            )

            json_string = completion.choices[0].message.content
            logger.info(f"[STEP 6] 大模型返回内容: {json_string[:200]}...")
            data = json.loads(json_string)
            logger.info(f"[STEP 6] JSON 解析成功, 字段: {list(data.keys())}")

            # 4. 映射到 Pydantic Schema
            items = []
            for item_data in data.get("items", []):
                # 解析每个明细下的设备入账信息
                equipments = []
                for equip_data in item_data.get("equipments", []) or []:
                    equipments.append(EquipmentAuditResponse(**equip_data))
                item = ContractItemResponse(
                    item_name=item_data.get("item_name"),
                    spec=item_data.get("spec"),
                    quantity=float(item_data.get("quantity", 0)),
                    unit=item_data.get("unit"),
                    unit_price=float(item_data.get("unit_price", 0)),
                    amount=float(item_data.get("amount", 0)),
                    equipments=equipments if equipments else None
                )
                items.append(item)
            logger.info(f"[STEP 7] 数据映射完成, items 数量: {len(items)}")

            return ContractRecognizeResponse(
                contract_no=data.get("contract_no"),
                party_a=data.get("party_a"),
                party_b=data.get("party_b"),
                sign_date=data.get("sign_date"),
                total_amount=float(data.get("total_amount", 0)),
                status=int(data.get("status", 0)),
                items=items
            )

        except Exception as e:
            logger.error(f"[ERROR] 合同识别失败: {str(e)}", exc_info=True)
            raise Exception(f"合同识别失败: {str(e)}")


contract_service = AiContractService()


