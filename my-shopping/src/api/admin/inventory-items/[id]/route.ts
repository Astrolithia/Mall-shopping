import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const { id } = req.params

    const response = await fetch(
      `http://localhost:8080/api/inventories/${id}`,
      {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      }
    )

    if (!response.ok) {
      throw new Error('获取库存项目详情失败')
    }

    const data = await response.json()
    
    // 返回包含所有属性的响应
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
    console.error('获取库存项目详情失败:', error)
    res.status(500).json({
      message: error.message || '获取库存项目详情失败'
    })
  }
}

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

    // 从路径中判断是否是属性更新
    const isAttributesUpdate = req.url.includes('/attributes')

    const response = await fetch(
      `http://localhost:8080/api/inventories/${id}`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          height: updateData.height,
          width: updateData.width,
          length: updateData.length,
          weight: updateData.weight,
          mid_code: updateData.mid_code,
          hs_code: updateData.hs_code,
          origin_country: updateData.origin_country,
          requires_shipping: updateData.requires_shipping
        })
      }
    )

    if (!response.ok) {
      throw new Error('更新库存项目失败')
    }

    const data = await response.json()

    // 返回简化的响应
    res.json({
      inventory_item: {
        id: data.id,
        requires_shipping: true
      }
    })
  } catch (error) {
    console.error('更新库存项目失败:', error)
    res.status(500).json({
      message: error.message || '更新库存项目失败'
    })
  }
} 