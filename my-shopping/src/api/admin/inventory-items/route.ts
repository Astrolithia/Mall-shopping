import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const { offset = 0, limit = 10 } = req.query
    const page = Math.floor(Number(offset) / Number(limit))

    const response = await fetch(
      `http://localhost:8080/api/inventories?page=${page}&size=${limit}`,
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    )

    if (!response.ok) {
      throw new Error('获取库存列表失败')
    }

    const data = await response.json()
    console.log('获取到的库存数据:', data)

    // 转换为 Medusa Admin UI 期望的格式
    const formattedItems = data.inventory_items.map(item => ({
      id: item.id,
      sku: item.sku,
      title: `${item.sku} (${item.quantity} units)`, // 添加更多描述性信息
      thumbnail: null,
      location_levels: [{
        location_id: item.location?.id || 1,
        stocked_quantity: item.quantity || 0,
        available_quantity: item.quantity || 0,
        reserved_quantity: 0
      }],
      requires_shipping: true,
      material: item.metadata?.material || null,
      weight: item.weight || null,
      length: item.length || null,
      height: item.height || null,
      width: item.width || null,
      origin_country: item.origin_country || null,
      hs_code: item.hs_code || null,
      mid_code: item.mid_code || null,
      description: item.metadata?.description || null,
      manage_inventory: item.manageInventory || true,
      allow_backorder: item.allowBackorder || false,
      metadata: item.metadata || {},
      created_at: item.createdAt,
      updated_at: item.updatedAt
    }))

    res.json({
      inventory_items: formattedItems,
      count: data.count,
      offset: Number(offset),
      limit: Number(limit)
    })

  } catch (error) {
    console.error('获取库存列表失败:', error)
    res.status(500).json({
      message: error.message || '获取库存列表失败'
    })
  }
}

export async function POST(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const createData = req.body

    console.log('\n=== 开始创建库存项目 ===')
    console.log('创建数据:', createData)

    // 转换请求数据格式以匹配后端期望
    const requestData = {
      sku: createData.sku,
      quantity: 0, // 设置初始库存为0
      allowBackorder: false,
      manageInventory: true,
      locationId: 1, // 使用默认位置ID
      metadata: {
        title: createData.title,
        requires_shipping: createData.requires_shipping
      }
    }

    const response = await fetch('http://localhost:8080/api/inventories', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestData)
    })

    if (!response.ok) {
      throw new Error('创建库存项目失败')
    }

    const data = await response.json()
    console.log('创建的库存项目:', data)

    // 返回符合 Medusa Admin UI 期望的格式
    res.json({
      inventory_item: {
        id: data.id,
        sku: data.sku,
        title: createData.title,
        requires_shipping: createData.requires_shipping,
        metadata: data.metadata || {}
      }
    })
  } catch (error) {
    console.error('创建库存项目失败:', error)
    res.status(500).json({
      message: error.message || '创建库存项目失败'
    })
  }
} 