"""
=================================================================
文件: tests/main.py
作用: 应用冒烟测试（对应官方骨架 tests/main.py）
框架: pytest + FastAPI TestClient（Starlette）
=================================================================
"""
from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_root():
    r = client.get("/")
    assert r.status_code == 200
    assert "message" in r.json()


def test_contract_recognize():
    r = client.post(
        "/api/v1/contract/recognize",
        json={"minioUrl": "http://minio:9000/contract/xxx.pdf", "filename": "合同.pdf"},
    )
    assert r.status_code == 200
    data = r.json()
    assert data["code"] == 200
    assert data["data"]["contract_no"]


def test_contract_recognize_compat():
    r = client.post(
        "/ContractRecognize",
        json={"minioUrl": "http://minio:9000/contract/xxx.pdf", "filename": "合同.pdf"},
    )
    assert r.status_code == 200
    assert r.json()["code"] == 200
