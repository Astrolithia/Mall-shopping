import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const { id } = req.params
    console.log('获取库存项目详情, ID:', id)

    if (!id) {
      return res.status(404).json({
        type: "not_found",
        message: "Inventory item not found",
        code: "inventory_item.not_found"
      })
    }

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
      if (response.status === 404) {
        return res.status(404).json({
          type: "not_found",
          message: "Inventory item not found",
          code: "inventory_item.not_found"
        })
      }
      throw new Error(`获取库存项目失败: ${response.status}`)
    }

    const data = await response.json()
    console.log('获取到的库存项目数据:', data)

    // 转换为 Medusa Admin UI 期望的格式
    const formattedItem = {
      id: data.id,
      sku: data.sku,
      title: `${data.sku} (${data.quantity} units)`,
      thumbnail: null,
      location_levels: [{
        location_id: data.location?.id || 1,
        stocked_quantity: data.quantity || 0,
        available_quantity: data.quantity || 0,
        reserved_quantity: 0
      }],
      requires_shipping: true,
      material: data.metadata?.material || null,
      weight: data.weight || null,
      length: data.length || null,
      height: data.height || null,
      width: data.width || null,
      origin_country: data.origin_country || null,
      hs_code: data.hs_code || null,
      mid_code: data.mid_code || null,
      description: data.metadata?.description || null,
      manage_inventory: data.manageInventory || true,
      allow_backorder: data.allowBackorder || false,
      metadata: data.metadata || {},
      created_at: data.createdAt,
      updated_at: data.updatedAt
    }

    res.json({
      inventory_item: formattedItem
    })

  } catch (error) {
    console.error('获取库存项目详情失败:', error)
    res.status(404).json({
      type: "not_found",
      message: "Inventory item not found",
      code: "inventory_item.not_found"
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
          height: updateData.height,
          width: updateData.width,
          length: updateData.length,
          weight: updateData.weight,
          mid_code: updateData.mid_code,
          hs_code: updateData.hs_code,
          origin_country: updateData.origin_country,
          requires_shipping: updateData.requires_shipping,
          metadata: updateData.metadata
        })
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

export async function DELETE(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const { id } = req.params

    console.log('\n=== 开始删除库存项目 ===')
    console.log('库存项目ID:', id)

    // 1. 首先检查库存项目是否存在
    const checkResponse = await fetch(
      `http://localhost:8080/api/inventories/${id}`,
      {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      }
    )

    if (!checkResponse.ok) {
      throw new Error('库存项目不存在')
    }

    // 2. 执行删除操作
    const response = await fetch(
      `http://localhost:8080/api/inventories/${id}`,
      {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json'
        }
      }
    )

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || '删除库存项目失败')
    }

    // 3. 返回 200 状态码和空对象
    res.status(200).json({})

  } catch (error) {
    console.error('删除库存项目失败:', error)
    
    // 根据错误类型返回不同的状态码
    if (error.message === '库存项目不存在') {
      res.status(404).json({
        message: '库存项目不存在'
      })
    } else {
      res.status(500).json({
        message: error.message || '删除库存项目失败'
      })
    }
  }
} 