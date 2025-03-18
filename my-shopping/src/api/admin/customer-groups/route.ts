import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"

const BACKEND_URL = "http://localhost:8080/api"

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    console.log("获取客户群组列表");
    const { offset = 0, limit = 10 } = req.query
    
    // 计算页码
    const page = Math.floor(Number(offset) / Number(limit))
    const size = Number(limit)

    const response = await fetch(
      `${BACKEND_URL}/customers/groups?page=${page}&size=${size}`,
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    )

    if (!response.ok) {
      throw new Error('获取客户群组列表失败')
    }

    const data = await response.json()
    console.log('获取到的客户群组数据:', data)

    // 转换为 Medusa Admin UI 期望的格式
    const formattedResponse = {
      customer_groups: data.customer_groups || [],
      count: data.count || 0,
      offset: Number(offset),
      limit: Number(limit)
    }

    return res.json(formattedResponse)
  } catch (error) {
    console.error("获取客户群组列表失败:", error);
    return res.status(500).json({
      message: error.message || "获取客户群组列表失败"
    })
  }
}

export async function POST(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    console.log("创建客户群组");
    const requestData = req.body

    const response = await fetch(`${BACKEND_URL}/customers/groups`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(requestData),
    })

    if (!response.ok) {
      const errorData = await response.json();
      console.error("创建客户群组失败:", errorData);
      throw new Error(errorData.message || "创建客户群组失败");
    }

    const data = await response.json()
    console.log("客户群组创建成功:", data);

    return res.json(data)
  } catch (error) {
    console.error("创建客户群组失败:", error);
    return res.status(500).json({
      message: error.message || "创建客户群组失败"
    })
  }
} 