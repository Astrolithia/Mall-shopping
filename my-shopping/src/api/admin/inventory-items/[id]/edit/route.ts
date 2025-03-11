import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"

export async function POST(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const { id } = req.params
    const updateData = req.body

    console.log('\n=== 开始更新库存项目 ===')
    console.log('库存项目ID:', id)
    console.log('更新数据:', updateData)

    const response = await fetch(
      `http://localhost:8080/api/inventories/${id}`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(updateData)
      }
    )

    if (!response.ok) {
      throw new Error('更新库存项目失败')
    }

    const data = await response.json()

    // 返回完整的响应
    res.json({
      inventory_item: {
        id: data.id,
        sku: data.sku,
        height: data.height,
        width: data.width,
        length: data.length,
        weight: data.weight,
        mid_code: data.mid_code,
        hs_code: data.hs_code,
        origin_country: data.origin_country,
        requires_shipping: true,
        metadata: data.metadata || {}
      }
    })
  } catch (error) {
    console.error('更新库存项目失败:', error)
    res.status(500).json({
      message: error.message || '更新库存项目失败'
    })
  }
} 