import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"

const BACKEND_URL = "http://localhost:8080/api"

export async function POST(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const { id } = req.params
    const { add = [], remove = [] } = req.body
    
    console.log('管理活动的促销活动, 活动ID:', id)
    console.log('添加的促销:', add)
    console.log('移除的促销:', remove)

    // 验证请求数据
    if ((!add || !Array.isArray(add) || add.length === 0) && 
        (!remove || !Array.isArray(remove) || remove.length === 0)) {
      return res.status(400).json({
        message: "必须提供要添加或移除的促销ID列表"
      })
    }

    const response = await fetch(
      `${BACKEND_URL}/campaigns/${id}/promotions`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ add, remove })
      }
    )

    if (!response.ok) {
      const errorData = await response.json()
      console.error('管理活动促销失败:', errorData)
      
      return res.status(response.status).json({
        message: errorData.message || '管理活动促销失败',
        code: 'UNKNOWN',
        type: response.status === 404 ? 'not_found' : 'unknown'
      })
    }

    const data = await response.json()
    console.log('管理活动促销成功:', data)

    return res.json(data)

  } catch (error) {
    console.error("管理活动促销失败:", error)
    return res.status(500).json({
      message: error.message || "管理活动促销失败",
      code: 'UNKNOWN',
      type: 'unknown'
    })
  }
}

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const { id } = req.params
    const { limit = 10, offset = 0 } = req.query
    
    console.log('获取活动的促销列表, 活动ID:', id)
    console.log('查询参数:', { limit, offset })
    
    // 计算页码
    const page = Math.floor(Number(offset) / Number(limit))
    
    // 使用新的端点获取特定活动的促销列表
    const response = await fetch(
      `${BACKEND_URL}/campaigns/${id}/promotions?page=${page}&size=${limit}`,
      {
        headers: {
          'Content-Type': 'application/json'
        },
        cache: 'no-store'  // 添加这个以确保获取最新数据
      }
    )

    if (!response.ok) {
      const errorData = await response.json()
      console.error('获取活动促销列表失败:', errorData)
      
      return res.status(response.status).json({
        message: errorData.message || '获取活动促销列表失败',
        code: 'UNKNOWN',
        type: response.status === 404 ? 'not_found' : 'unknown'
      })
    }

    const data = await response.json()
    console.log('获取活动促销列表成功:', data)

    // 转换响应格式以匹配 Medusa Admin UI 期望的格式
    return res.json({
      promotions: data.promotions,
      count: data.count,
      limit: data.limit,
      offset: data.offset
    })

  } catch (error) {
    console.error("获取活动促销列表失败:", error)
    return res.status(500).json({
      message: error.message || "获取活动促销列表失败",
      code: 'UNKNOWN',
      type: 'unknown'
    })
  }
} 