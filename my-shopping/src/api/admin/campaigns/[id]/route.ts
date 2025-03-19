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