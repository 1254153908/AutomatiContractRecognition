import { get, post, put, del, upload } from './request.js'

/** 查询所有合同列表（全量，前端搜索用） */
export function listContracts() {
  return get('/list')
}

/** 分页查询合同列表：返回 IPage { records, total, current, size, pages } */
export function pageContracts(current, size) {
  return get('/page', { current, size })
}

/** 根据ID查询合同 */
export function getContract(id) {
  return get('/' + id)
}

/** 新增合同 */
export function addContract(data) {
  return post('', data)
}

/** 修改合同 */
export function updateContract(data) {
  return put('', data)
}

/** 删除合同 */
export function deleteContract(id) {
  return del('/' + id)
}

/** 上传文件识别 */
export function recognizeFile(file) {
  return upload('/recognize', file)
}

// ==================== 设备入账未审核 ====================

/** 根据明细ID查询设备入账未审核记录 */
export function getAuditsByItemId(itemId) {
  return get('/items/' + itemId + '/audits')
}

/** 保存某条明细的设备入账未审核记录（先删后插，传数组） */
export function saveAudits(itemId, audits) {
  return post('/items/' + itemId + '/audits', audits)
}

/** 根据合同ID查询所有设备入账未审核记录 */
export function getAuditsByContractId(contractId) {
  return get('/equipment-audit/by-contract/' + contractId)
}
