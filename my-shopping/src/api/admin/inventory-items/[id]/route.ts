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