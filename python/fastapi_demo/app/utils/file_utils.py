"""
=================================================================
文件: app/utils/file_utils.py
作用: 文件处理相关工具函数
类比: Java 的工具类 FileUtils / IOUtils

典型函数:

    def read_file(file_path: str) -> str:
        '''
        读取文件内容
        参数: file_path (str) - 文件路径
        返回: str - 文件文本内容
        '''

    def write_file(file_path: str, content: str) -> None:
        '''
        写入文件
        参数: file_path (str), content (str)
        返回: None
        '''

    def parse_contract_file(file_path: str) -> str:
        '''
        解析合同文件（支持 .txt .docx .pdf）
        参数: file_path (str) - 合同文件路径
        返回: str - 提取的纯文本内容
        '''

    def save_upload_file(upload_file, target_dir: str) -> str:
        '''
        保存上传的文件到指定目录
        参数:
            upload_file: FastAPI UploadFile 对象
            target_dir (str): 目标目录
        返回: str - 保存后的文件路径
        '''

    def allowed_file(filename: str, allowed_extensions: set) -> bool:
        '''
        检查文件扩展名是否合法
        参数: filename(str), allowed_extensions({"txt","docx","pdf"})
        返回: bool
        '''
=================================================================
"""
