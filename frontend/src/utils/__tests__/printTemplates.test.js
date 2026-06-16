/**
 * 打印模板 — 单元测试
 */
import { describe, it, expect } from 'vitest'
import { generateTravelApplicationForm } from '../printTemplates'

const SAMPLE_DATA = {
  applicationId: 1001,
  groupCode: 'TG-001',
  departureDate: '2026-07-15',
  contactName: '张三',
  contactPhone: '13800138000',
  adultCount: 2,
  childCount: 1,
  depositAmount: 500.00,
  totalAmount: 5000.00,
  paidAmount: 500.00,
  status: 'DEPOSIT_PAID',
  applyTime: '2026-06-16T10:30:00',
  routeCode: 'RT-001',
  deadline: '2026-07-01',
  routeName: '武汉—黄山三日游',
  routeDescription: '经典黄山线路，含缆车和住宿',
  adultPrice: 2000.00,
  childPrice: 1000.00,
  discountDesc: '早鸟优惠，成人立减200',
  adultSubtotal: 4000.00,
  childSubtotal: 1000.00,
  balanceDue: 4500.00
}

describe('generateTravelApplicationForm', () => {
  it('返回完整 HTML 文档（含 DOCTYPE）', () => {
    const html = generateTravelApplicationForm(SAMPLE_DATA)
    expect(html).toMatch(/^<!DOCTYPE html>/)
    expect(html).toContain('</html>')
  })

  it('包含标题"旅游申请书"', () => {
    const html = generateTravelApplicationForm(SAMPLE_DATA)
    expect(html).toContain('旅游申请书')
  })

  it('包含申请编号', () => {
    const html = generateTravelApplicationForm(SAMPLE_DATA)
    expect(html).toContain('1001')
  })

  it('包含路线名称', () => {
    const html = generateTravelApplicationForm(SAMPLE_DATA)
    expect(html).toContain('武汉—黄山三日游')
  })

  it('包含责任人姓名', () => {
    const html = generateTravelApplicationForm(SAMPLE_DATA)
    expect(html).toContain('张三')
  })

  it('包含费用金额', () => {
    const html = generateTravelApplicationForm(SAMPLE_DATA)
    expect(html).toContain('5000.00')
    expect(html).toContain('4500.00')
  })

  it('包含优惠说明', () => {
    const html = generateTravelApplicationForm(SAMPLE_DATA)
    expect(html).toContain('早鸟优惠，成人立减200')
  })

  it('包含 A4 页边距设置', () => {
    const html = generateTravelApplicationForm(SAMPLE_DATA)
    expect(html).toContain('@page')
    expect(html).toContain('A4')
  })

  it('空数据处理不报错', () => {
    const html = generateTravelApplicationForm({})
    expect(html).toContain('旅游申请书')
    expect(typeof html).toBe('string')
  })

  it('无优惠说明时不显示优惠行', () => {
    const noDiscount = { ...SAMPLE_DATA, discountDesc: '' }
    const html = generateTravelApplicationForm(noDiscount)
    expect(html).not.toContain('优惠说明')
  })

  it('包含签名栏', () => {
    const html = generateTravelApplicationForm(SAMPLE_DATA)
    expect(html).toContain('经办人签字')
    expect(html).toContain('顾客签字')
  })
})
