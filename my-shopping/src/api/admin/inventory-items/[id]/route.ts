import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const { id } = req.params

    console.log('\n=== 开始获取库存项目详情 ===')
    console.log('库存项目ID:', id)

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
    console.log('获取到的库存项目详情:', data)

    // 转换为 Medusa Admin UI 期望的格式
    res.json({
      inventory_item: {
        id: data.id,
        sku: data.sku,
        origin_country: data.origin_country,
        hs_code: data.hs_code,
        mid_code: data.mid_code,
        material: data.material,
        weight: data.weight,
        length: data.length,
        height: data.height,
        width: data.width,
        requires_shipping: data.requires_shipping,
        metadata: data.metadata || {},
        created_at: data.created_at,
        updated_at: data.updated_at,
        deleted_at: data.deleted_at,
        location_levels: data.location_levels || []
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

    const response = await fetch(
      `http://localhost:8080/api/inventories/${id}`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          sku: updateData.sku,
          metadata: updateData.metadata
        })
      }
    )

    if (!response.ok) {
      throw new Error('更新库存项目失败')
    }

    const data = await response.json()
    console.log('更新后的库存项目:', data)

    res.json({
      inventory_item: {
        id: data.inventory_item.id,
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