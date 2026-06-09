/**
 * 旅游业务规则 — 纯函数工具库
 * 从 Service 层提取，前端可复用并可独立进行单元测试
 */

/**
 * 计算订金比例
 * 规则: ≥60天→10%, 30~59天→20%, <30天→100%
 * @param {Date|string} departureDate - 出发日期
 * @param {Date|string} [today] - 参考日期，默认今天
 * @returns {{ ratio: number, label: string }}
 */
export function calcDepositRatio(departureDate, today = new Date()) {
  const dep = new Date(departureDate)
  const now = new Date(today)
  // 去掉时分秒，只比较日期部分
  const depDay = new Date(dep.getFullYear(), dep.getMonth(), dep.getDate())
  const nowDay = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const diffMs = depDay - nowDay
  const days = Math.floor(diffMs / (1000 * 60 * 60 * 24))

  if (days >= 60) return { ratio: 0.10, label: '10%' }
  if (days >= 30) return { ratio: 0.20, label: '20%' }
  return { ratio: 1.00, label: '全额' }
}

/**
 * 计算订金金额
 * @param {number} adultPrice - 成人单价
 * @param {number} childPrice - 儿童单价
 * @param {number} adultCount - 成人人数
 * @param {number} childCount - 儿童人数
 * @param {Date|string} departureDate - 出发日期
 * @returns {{ total: number, deposit: number, ratio: number, ratioLabel: string }}
 */
export function calcDepositAmount(adultPrice, childPrice, adultCount, childCount, departureDate) {
  const total = adultPrice * adultCount + childPrice * childCount
  const { ratio, label } = calcDepositRatio(departureDate)
  return {
    total,
    deposit: total * ratio,
    ratio,
    ratioLabel: label
  }
}

/**
 * 计算取消手续费比例
 * 规则: >30天→0%, 10~30天→20%, 1~9天→50%, ≤0天→100%
 * @param {Date|string} departureDate
 * @param {Date|string} [today]
 * @returns {{ rate: number, label: string }}
 */
export function calcCancellationFeeRate(departureDate, today = new Date()) {
  const dep = new Date(departureDate)
  const now = new Date(today)
  const depDay = new Date(dep.getFullYear(), dep.getMonth(), dep.getDate())
  const nowDay = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const diffMs = depDay - nowDay
  const days = Math.floor(diffMs / (1000 * 60 * 60 * 24))

  if (days > 30) return { rate: 0.00, label: '0%' }
  if (days >= 10) return { rate: 0.20, label: '20%' }
  if (days >= 1) return { rate: 0.50, label: '50%' }
  return { rate: 1.00, label: '100%' }
}

/**
 * 计算取消退款
 * @param {number} paidAmount - 已付金额
 * @param {Date|string} departureDate
 * @returns {{ fee: number, refund: number, rate: number, rateLabel: string }}
 */
export function calcCancelRefund(paidAmount, departureDate) {
  const { rate, label } = calcCancellationFeeRate(departureDate)
  const fee = Math.round(paidAmount * rate * 100) / 100
  const refund = Math.round((paidAmount - fee) * 100) / 100
  return { fee, refund, rate, rateLabel: label }
}

/**
 * 计算支付截止日期
 * 规则: max(出发日期-30天, 交款单日期+10天)
 * @param {Date|string} departureDate
 * @param {Date|string} slipDate
 * @returns {Date}
 */
export function calcPaymentDeadline(departureDate, slipDate) {
  const dep = new Date(departureDate)
  const slip = new Date(slipDate)
  const depMinus30 = new Date(dep.getTime() - 30 * 24 * 60 * 60 * 1000)
  const slipPlus10 = new Date(slip.getTime() + 10 * 24 * 60 * 60 * 1000)
  return depMinus30 > slipPlus10 ? depMinus30 : slipPlus10
}

/**
 * 格式化金额为元（保留两位小数）
 * @param {number} amount
 * @returns {string}
 */
export function formatMoney(amount) {
  return amount.toFixed(2)
}
