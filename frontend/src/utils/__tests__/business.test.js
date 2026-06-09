/**
 * 旅游业务规则 — 单元测试
 * 测试所有核心计算函数：订金比例、取消费率、支付期限
 */
import { describe, it, expect, vi } from 'vitest'
import {
  calcDepositRatio,
  calcDepositAmount,
  calcCancellationFeeRate,
  calcCancelRefund,
  calcPaymentDeadline,
  formatMoney
} from '../business'

// ======================== 订金比例 ========================

describe('calcDepositRatio — 订金比例计算', () => {
  // 辅助函数：生成固定偏移天数的日期
  function daysFromNow(n) {
    const d = new Date()
    d.setDate(d.getDate() + n)
    return d
  }

  it('≥60天 → 10%', () => {
    expect(calcDepositRatio(daysFromNow(60)).ratio).toBe(0.10)
  })

  it('59天 → 20%（边界：低于60）', () => {
    expect(calcDepositRatio(daysFromNow(59)).ratio).toBe(0.20)
  })

  it('45天 → 20%', () => {
    expect(calcDepositRatio(daysFromNow(45)).ratio).toBe(0.20)
  })

  it('30天 → 20%（边界：刚好≥30）', () => {
    expect(calcDepositRatio(daysFromNow(30)).ratio).toBe(0.20)
  })

  it('29天 → 100%（边界：低于30）', () => {
    expect(calcDepositRatio(daysFromNow(29)).ratio).toBe(1.00)
  })

  it('10天 → 100%', () => {
    expect(calcDepositRatio(daysFromNow(10)).ratio).toBe(1.00)
  })

  it('1天 → 100%', () => {
    expect(calcDepositRatio(daysFromNow(1)).ratio).toBe(1.00)
  })

  it('0天（当天）→ 100%', () => {
    expect(calcDepositRatio(daysFromNow(0)).ratio).toBe(1.00)
  })
})

// ======================== 订金金额 ========================

describe('calcDepositAmount — 订金金额计算', () => {
  it('成人2+小孩1, ≥60天 → 订金=总价×10%', () => {
    const result = calcDepositAmount(5000, 3000, 2, 1, '2026-09-01')
    expect(result.total).toBe(13000)
    expect(result.deposit).toBe(1300)
    expect(result.ratio).toBe(0.10)
  })

  it('仅成人1人, ≥60天', () => {
    const result = calcDepositAmount(5000, 3000, 1, 0, '2026-09-01')
    expect(result.total).toBe(5000)
    expect(result.deposit).toBe(500)
  })

  it('仅小孩2人, ≥60天', () => {
    const result = calcDepositAmount(5000, 3000, 0, 2, '2026-09-01')
    expect(result.total).toBe(6000)
    expect(result.deposit).toBe(600)
  })
})

// ======================== 取消费率 ========================

describe('calcCancellationFeeRate — 取消费率计算', () => {
  function daysFromNow(n) {
    const d = new Date()
    d.setDate(d.getDate() + n)
    return d
  }

  it('>30天 → 0%（无手续费）', () => {
    expect(calcCancellationFeeRate(daysFromNow(31)).rate).toBe(0)
  })

  it('31天 → 0%（边界：刚好>30）', () => {
    expect(calcCancellationFeeRate(daysFromNow(31)).rate).toBe(0)
  })

  it('30天 → 20%（边界：10~30）', () => {
    expect(calcCancellationFeeRate(daysFromNow(30)).rate).toBe(0.20)
  })

  it('20天 → 20%', () => {
    expect(calcCancellationFeeRate(daysFromNow(20)).rate).toBe(0.20)
  })

  it('10天 → 20%（边界）', () => {
    expect(calcCancellationFeeRate(daysFromNow(10)).rate).toBe(0.20)
  })

  it('9天 → 50%（边界：1~9）', () => {
    expect(calcCancellationFeeRate(daysFromNow(9)).rate).toBe(0.50)
  })

  it('5天 → 50%', () => {
    expect(calcCancellationFeeRate(daysFromNow(5)).rate).toBe(0.50)
  })

  it('1天 → 50%（边界）', () => {
    expect(calcCancellationFeeRate(daysFromNow(1)).rate).toBe(0.50)
  })

  it('0天（当天）→ 100%', () => {
    expect(calcCancellationFeeRate(daysFromNow(0)).rate).toBe(1.00)
  })

  it('-1天（已出发）→ 100%', () => {
    expect(calcCancellationFeeRate(daysFromNow(-1)).rate).toBe(1.00)
  })
})

// ======================== 取消退款 ========================

describe('calcCancelRefund — 取消退款计算', () => {
  it('已付2600, >30天 → 手续费0, 全额退款', () => {
    const d = new Date()
    d.setDate(d.getDate() + 31)
    const result = calcCancelRefund(2600, d)
    expect(result.fee).toBe(0)
    expect(result.refund).toBe(2600)
  })

  it('已付2600, 10~30天 → 手续费520, 退款2080', () => {
    const d = new Date()
    d.setDate(d.getDate() + 20)
    const result = calcCancelRefund(2600, d)
    expect(result.fee).toBe(520)
    expect(result.refund).toBe(2080)
  })

  it('已付10000, 1~9天 → 手续费5000, 退款5000', () => {
    const d = new Date()
    d.setDate(d.getDate() + 5)
    const result = calcCancelRefund(10000, d)
    expect(result.fee).toBe(5000)
    expect(result.refund).toBe(5000)
  })

  it('已付5000, 当天 → 手续费5000, 退款0（不退）', () => {
    const d = new Date()
    const result = calcCancelRefund(5000, d)
    expect(result.fee).toBe(5000)
    expect(result.refund).toBe(0)
  })
})

// ======================== 支付截止日期 ========================

describe('calcPaymentDeadline — 支付截止日期', () => {
  it('出发日期-30天 > 交款单+10天 → 取出发-30天', () => {
    const departure = new Date('2026-08-01')
    const slip = new Date('2026-06-01')
    const deadline = calcPaymentDeadline(departure, slip)
    // 出发-30天 = 2026-07-02, 交款+10天 = 2026-06-11, max=2026-07-02
    expect(deadline.toISOString().split('T')[0]).toBe('2026-07-02')
  })

  it('交款单+10天 > 出发-30天 → 取交款+10天', () => {
    const departure = new Date('2026-07-05')
    const slip = new Date('2026-07-01')
    const deadline = calcPaymentDeadline(departure, slip)
    // 出发-30天 = 2026-06-05, 交款+10天 = 2026-07-11, max=2026-07-11
    expect(deadline.toISOString().split('T')[0]).toBe('2026-07-11')
  })

  it('同一天 → 返回同一天', () => {
    const departure = new Date('2026-06-20')
    const slip = new Date('2026-05-10')
    // 出发-30天 = 2026-05-21, 交款+10天 = 2026-05-20, max=2026-05-21
    const deadline = calcPaymentDeadline(departure, slip)
    expect(deadline.toISOString().split('T')[0]).toBe('2026-05-21')
  })
})

// ======================== 格式化 ========================

describe('formatMoney — 金额格式化', () => {
  it('整数 → 保留两位', () => {
    expect(formatMoney(1000)).toBe('1000.00')
  })

  it('小数 → 保留两位', () => {
    expect(formatMoney(1234.567)).toBe('1234.57')
  })

  it('0 → 0.00', () => {
    expect(formatMoney(0)).toBe('0.00')
  })
})
