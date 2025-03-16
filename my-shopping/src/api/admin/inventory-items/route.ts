import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const offset = req.query.offset as string
    const limit = req.query.limit as string

    console.log('\n=== 开始获取库存列表 ===')
    console.log('偏移量:', offset)
    console.log('限制数:', limit)

    const response = await fetch(
      `http://localhost:8080/api/inventories?${new URLSearchParams({
        offset: offset || '0',
        limit: limit || '10'
      })}`,
      {
        method: 'GET',
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
    res.json({
      inventory_items: data.inventory_items.map((item: any) => {
        // 确保 item.sku 存在
        if (!item.sku) {
          return {
            id: item.id,
            sku: '',
            title: '',
            requires_shipping: true
          }
        }

        // 从 SKU 解析尺码和颜色信息
        const skuParts = item.sku.split('-')
        const type = skuParts[0] || '' // SHIRT, SWEATPANTS 等
        const size = skuParts[1] || '' // XL, S, M 等
        const color = skuParts[2] || '' // WHITE, BLACK 等
        
        // 构建标题
        const title = color ? `${size} / ${color}` : size

        return {
          id: item.id,
          sku: item.sku,
          title: title,
          thumbnail: null,
          location_levels: [{
            location_id: item.location?.id,
            stocked_quantity: item.quantity || 0,
            available_quantity: item.quantity || 0,
            reserved_quantity: 0
          }],
          requires_shipping: true,
          material: null,
          weight: null,
          length: null,
          height: null,
          width: null,
          origin_country: null,
          hs_code: null,
          mid_code: null,
          description: null,
          manage_inventory: item.manageInventory,
          allow_backorder: item.allowBackorder,
          metadata: item.metadata || {},
          created_at: item.createdAt,
          updated_at: item.updatedAt
        }
      }),
      count: data.count,
      offset: parseInt(offset || '0'),
      limit: parseInt(limit || '10')
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