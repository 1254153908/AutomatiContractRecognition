"""
=================================================================
文件: app/utils/common.py
作用: 通用工具函数
类比: Java 的工具类 CommonsUtil / StringUtils

典型函数:

    def json_response(data, code=200, message="ok") -> dict:
        '''
        构建统一 JSON 响应
        参数: data(object), code(int), message(str)
        返回: {"code":200, "message":"ok", "data":{...}}
        '''

    def get_current_time_str(fmt: str = "%Y-%m-%d %H:%M:%S") -> str:
        '''
        获取当前时间字符串
        参数: fmt (str) - 时间格式
        返回: str - 如 "2026-07-15 10:30:00"
        '''

    def truncate_text(text: str, max_length: int = 500) -> str:
        '''
        截断文本（用于日志打印避免过长）
        参数: text(str), max_length(int)
        返回: str
        '''

    def safe_get(d: dict, *keys, default=None):
        '''
        安全地从嵌套字典中取值，避免 KeyError
        参数: d(dict), *keys(str), default(any)
        返回: 取到的值或 default
        示例: safe_get(data, "user", "name", default="未知")
        '''
=================================================================
"""
