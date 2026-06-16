/**
 * 打印模板工具 — 旅游申请书及相关单据
 */

/**
 * 格式化金额
 */
function fmt(n) {
  if (n == null) return '0.00'
  return Number(n).toFixed(2)
}

/**
 * 格式化日期时间
 */
function fmtDate(d) {
  if (!d) return ''
  return d.split('T')[0] || d
}

/**
 * 状态映射
 */
const STATUS_MAP = {
  DRAFT: '草稿',
  DEPOSIT_PAID: '已付订金',
  PARTICIPANTS_ENTERED: '已录入参团人',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}

/**
 * 生成旅游申请书 HTML（独立文档，可直接打印）
 * @param {Object} d - PrintFormData
 * @returns {string} 完整 HTML 字符串
 */
export function generateTravelApplicationForm(d) {
  return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>旅游申请书</title>
<style>
  @page {
    size: A4;
    margin: 12mm 15mm;
  }
  * { margin: 0; padding: 0; box-sizing: border-box; }
  body {
    font-family: "SimSun", "宋体", "Noto Serif CJK SC", serif;
    font-size: 14px;
    color: #000;
    line-height: 1.8;
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
  }
  .container { max-width: 190mm; margin: 0 auto; }

  /* 标题 */
  .title {
    text-align: center;
    font-size: 24px;
    font-weight: bold;
    font-family: "SimHei", "黑体", "Noto Sans CJK SC", sans-serif;
    letter-spacing: 4px;
    margin-bottom: 8px;
  }
  .subtitle {
    text-align: center;
    font-size: 12px;
    color: #666;
    margin-bottom: 24px;
  }

  /* 信息表格 */
  table.info-table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 16px;
  }
  table.info-table td, table.info-table th {
    border: 1px solid #333;
    padding: 6px 10px;
    font-size: 13px;
  }
  table.info-table .label {
    background: #f5f5f5;
    font-weight: bold;
    width: 110px;
    text-align: right;
    padding-right: 8px;
    white-space: nowrap;
  }
  table.info-table .value {
    text-align: left;
  }

  /* 章节标题 */
  .section-title {
    font-size: 16px;
    font-weight: bold;
    font-family: "SimHei", "黑体", "Noto Sans CJK SC", sans-serif;
    border-bottom: 2px solid #333;
    padding-bottom: 4px;
    margin: 24px 0 12px 0;
  }

  /* 费用明细表格 */
  table.fee-table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 16px;
  }
  table.fee-table th, table.fee-table td {
    border: 1px solid #333;
    padding: 6px 8px;
    text-align: center;
    font-size: 13px;
  }
  table.fee-table th {
    background: #e8e8e8;
    font-weight: bold;
  }
  table.fee-table .right { text-align: right; padding-right: 10px; }
  table.fee-table .total-row td {
    font-weight: bold;
    background: #f9f9f9;
  }

  /* 脚注 */
  .footer {
    margin-top: 32px;
    font-size: 12px;
    color: #666;
  }
  .footer .signature {
    margin-top: 40px;
    display: flex;
    justify-content: space-between;
  }
  .footer .sig-item {
    display: inline-block;
    min-width: 160px;
    border-top: 1px solid #333;
    padding-top: 4px;
    text-align: center;
  }

  /* 不打印按钮 */
  @media print {
    .no-print { display: none !important; }
  }

  .no-print {
    text-align: center;
    margin: 20px 0;
  }
  .no-print button {
    padding: 8px 24px;
    font-size: 14px;
    background: #409eff;
    color: #fff;
    border: none;
    border-radius: 4px;
    cursor: pointer;
  }
  .no-print button:hover { background: #337ecc; }
</style>
</head>
<body>
<div class="container">

  <!-- 标题 -->
  <div class="title">旅 游 申 请 书</div>
  <div class="subtitle">申请编号：${d.applicationId || ''}　　打印日期：${fmtDate(new Date().toISOString())}</div>

  <!-- 一、申请信息 -->
  <div class="section-title">一、申请信息</div>
  <table class="info-table">
    <tr>
      <td class="label">申请编号</td><td class="value">${d.applicationId || ''}</td>
      <td class="label">申请时间</td><td class="value">${fmtDate(d.applyTime)}</td>
    </tr>
    <tr>
      <td class="label">当前状态</td><td class="value">${STATUS_MAP[d.status] || d.status || ''}</td>
      <td class="label">经手员工</td><td class="value"></td>
    </tr>
  </table>

  <!-- 二、旅游信息 -->
  <div class="section-title">二、旅游信息</div>
  <table class="info-table">
    <tr>
      <td class="label">路线名称</td><td class="value">${d.routeName || ''}</td>
      <td class="label">路线代码</td><td class="value">${d.routeCode || ''}</td>
    </tr>
    <tr>
      <td class="label">旅游团代码</td><td class="value">${d.groupCode || ''}</td>
      <td class="label">出发日期</td><td class="value">${fmtDate(d.departureDate)}</td>
    </tr>
    <tr>
      <td class="label">报名截止</td><td class="value">${fmtDate(d.deadline)}</td>
      <td class="label">路线描述</td><td class="value">${d.routeDescription || ''}</td>
    </tr>
  </table>

  <!-- 三、旅客信息 -->
  <div class="section-title">三、旅客信息（责任人）</div>
  <table class="info-table">
    <tr>
      <td class="label">责任人姓名</td><td class="value">${d.contactName || ''}</td>
      <td class="label">责任人电话</td><td class="value">${d.contactPhone || ''}</td>
    </tr>
    <tr>
      <td class="label">大人人数</td><td class="value">${d.adultCount || 0} 人</td>
      <td class="label">小孩人数</td><td class="value">${d.childCount || 0} 人</td>
    </tr>
    <tr>
      <td class="label">总人数</td><td class="value">${(d.adultCount || 0) + (d.childCount || 0)} 人</td>
      <td class="label"></td><td class="value"></td>
    </tr>
  </table>

  <!-- 四、费用明细 -->
  <div class="section-title">四、费用明细</div>
  <table class="fee-table">
    <thead>
      <tr>
        <th>项目</th><th>单价 (元)</th><th>人数</th><th>小计 (元)</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td>成人</td>
        <td class="right">${fmt(d.adultPrice)}</td>
        <td>${d.adultCount || 0}</td>
        <td class="right">${fmt(d.adultSubtotal)}</td>
      </tr>
      <tr>
        <td>儿童</td>
        <td class="right">${fmt(d.childPrice)}</td>
        <td>${d.childCount || 0}</td>
        <td class="right">${fmt(d.childSubtotal)}</td>
      </tr>
      <tr class="total-row">
        <td colspan="3">总费用</td>
        <td class="right">￥${fmt(d.totalAmount)}</td>
      </tr>
      <tr>
        <td colspan="3">订金金额</td>
        <td class="right">￥${fmt(d.depositAmount)}</td>
      </tr>
      <tr>
        <td colspan="3">已付金额</td>
        <td class="right">￥${fmt(d.paidAmount)}</td>
      </tr>
      <tr>
        <td colspan="3">应付余额</td>
        <td class="right">￥${fmt(d.balanceDue)}</td>
      </tr>
      ${d.discountDesc ? `
      <tr>
        <td colspan="3">优惠说明</td>
        <td>${d.discountDesc}</td>
      </tr>` : ''}
    </tbody>
  </table>

  <!-- 页脚 -->
  <div class="footer">
    <p>注：请妥善保管此申请书。参团人详细信息请在收到确认书后填写并寄回。</p>
    <div class="signature">
      <span class="sig-item">经办人签字</span>
      <span class="sig-item">顾客签字</span>
      <span class="sig-item">日期：____年____月____日</span>
    </div>
  </div>

  <!-- 打印按钮（仅在屏幕上可见） -->
  <div class="no-print">
    <button onclick="window.print()">打印本页</button>
  </div>

</div>
<script>
  // 页面加载后自动弹出打印对话框
  window.onload = function() {
    setTimeout(function() { window.print(); }, 300);
  };
</script>
</body>
</html>`
}
