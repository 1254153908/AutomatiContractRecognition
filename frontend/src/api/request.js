/**
 * 统一请求封装，处理后端 R<{code, msg, data}> 格式
 * code === 200 时返回 data
 * 否则抛出异常
 */
const BASE = '/contract'

async function parseResponse(res) {
  const text = await res.text()
  if (!text) {
    throw new Error(`HTTP ${res.status}：响应为空（后端可能未返回数据）`)
  }
  try {
    return JSON.parse(text)
  } catch {
    throw new Error(`HTTP ${res.status}：后端返回了非 JSON 数据（${text.substring(0, 100)}）`)
  }
}

async function request(url, options = {}) {
  const res = await fetch(BASE + url, options)
  const result = await parseResponse(res)
  if (result.code !== 200) {
    throw new Error(result.msg || '请求失败')
  }
  return result.data
}

export function get(url, params) {
  let fullUrl = url
  if (params) {
    const qs = new URLSearchParams(params).toString()
    fullUrl += '?' + qs
  }
  return request(fullUrl, { method: 'GET' })
}

export function post(url, data) {
  return request(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
}

export function put(url, data) {
  return request(url, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
}

export function del(url) {
  return request(url, { method: 'DELETE' })
}

/** 文件上传（FormData） */
export async function upload(url, file) {
  const formData = new FormData()
  formData.append('file', file)
  const res = await fetch(BASE + url, { method: 'POST', body: formData })
  const result = await parseResponse(res)
  if (result.code !== 200) {
    throw new Error(result.msg || '上传失败')
  }
  return result
}
