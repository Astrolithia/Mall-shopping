import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const { id } = req.params
    console.log('获取库存位置详情, ID:', id)

    if (!id) {
      return res.status(400).json({
        message: "库存位置ID不能为空"
      })
    }

    const response = await fetch(
      `http://localhost:8080/api/locations/${id}`,
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
          message: `库存位置不存在 (ID: ${id})`
        })
      }
      throw new Error(`获取库存位置失败: ${response.status}`)
    }

    const data = await response.json()
    console.log('获取到的库存位置数据:', data)

    // 转换为 Medusa Admin UI 期望的格式
    const formattedLocation = {
      id: data.id.toString(),
      name: data.name,
      address_id: data.id.toString(),
      address: data.address,
      city: data.city,
      country_code: data.countryCode,
      created_at: data.createdAt,
      updated_at: data.updatedAt,
      deleted_at: data.deletedAt,
      metadata: data.metadata || {}
    }

    res.json({
      stock_location: formattedLocation
    })

  } catch (error) {
    console.error('获取库存位置详情失败:', error)
    res.status(500).json({
      message: error.message || '获取库存位置详情失败'
    })
  }
} 