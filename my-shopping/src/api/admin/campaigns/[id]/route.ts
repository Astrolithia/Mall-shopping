import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"

const BACKEND_URL = "http://localhost:8080/api"

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    // 从 req.params 中获取 id
    const id = req.params.id
    console.log('获取营销活动详情, ID:', id)

    const response = await fetch(`${BACKEND_URL}/campaigns/${id}`, {
      headers: {
        'Content-Type': 'application/json'
      },
      cache: 'no-store'
    })

    if (!response.ok) {
      const errorData = await response.json()
      console.error('获取营销活动详情失败:', errorData)
      
      // 返回符合 Medusa 错误格式的响应
      return res.status(response.status).json({
        message: errorData.message || '获取营销活动详情失败',
        code: 'UNKNOWN',
        type: response.status === 404 ? 'not_found' : 'unknown'
      })
    }

    const data = await response.json()
    console.log('后端返回的活动数据:', data)

    return res.json(data)

  } catch (error) {
    console.error("获取营销活动详情失败:", error)
    return res.status(500).json({
      message: error.message || "获取营销活动详情失败",
      code: 'UNKNOWN',
      type: 'unknown'
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
    
    console.log('更新营销活动, ID:', id)
    console.log('更新数据:', updateData)

    // 转换请求数据格式以匹配后端期望
    const requestData = {
      description: updateData.description,
      startsAt: updateData.starts_at,
      endsAt: updateData.ends_at,
      name: updateData.name,
      campaignIdentifier: updateData.campaign_identifier,
      currency: updateData.currency,
      budget: updateData.budget ? {
        type: updateData.budget.type,
        currencyCode: updateData.budget.currency_code,
        limit: updateData.budget.limit,
        used: updateData.budget.used
      } : undefined
    }

    const response = await fetch(`${BACKEND_URL}/campaigns/${id}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestData)
    })

    if (!response.ok) {
      const errorData = await response.json()
      console.error('更新营销活动失败:', errorData)
      
      return res.status(response.status).json({
        message: errorData.message || '更新营销活动失败',
        code: 'UNKNOWN',
        type: response.status === 404 ? 'not_found' : 'unknown'
      })
    }

    const data = await response.json()
    console.log('更新成功:', data)

    return res.json(data)

  } catch (error) {
    console.error("更新营销活动失败:", error)
    return res.status(500).json({
      message: error.message || "更新营销活动失败",
      code: 'UNKNOWN',
      type: 'unknown'
    })
  }
}

export async function DELETE(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const { id } = req.params
    console.log('删除营销活动, ID:', id)

    const response = await fetch(`${BACKEND_URL}/campaigns/${id}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      const errorData = await response.json()
      console.error('删除营销活动失败:', errorData)
      
      return res.status(response.status).json({
        message: errorData.message || '删除营销活动失败',
        code: 'UNKNOWN',
        type: response.status === 404 ? 'not_found' : 'unknown'
      })
    }

    // 返回符合 Medusa Admin UI 期望的格式
    return res.json({
      id: id.toString(),
      object: "campaign",
      deleted: true
    })

  } catch (error) {
    console.error("删除营销活动失败:", error)
    return res.status(500).json({
      message: error.message || "删除营销活动失败",
      code: 'UNKNOWN',
      type: 'unknown'
    })
  }
} 