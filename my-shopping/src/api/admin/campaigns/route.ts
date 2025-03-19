import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"

const BACKEND_URL = "http://localhost:8080/api"

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const { limit = 20, offset = 0 } = req.query
    
    console.log('获取营销活动列表, 查询参数:', { limit, offset })
    
    // 计算页码
    const page = Math.floor(Number(offset) / Number(limit))
    
    // 构建后端请求URL
    const backendUrl = `${BACKEND_URL}/campaigns?page=${page}&size=${limit}`
    console.log('请求后端URL:', backendUrl)

    const response = await fetch(backendUrl, {
      headers: {
        'Content-Type': 'application/json'
      },
      cache: 'no-store'
    })

    if (!response.ok) {
      throw new Error('获取营销活动列表失败')
    }

    const data = await response.json()
    console.log('后端返回的原始数据:', data)
    
    return res.json(data)

  } catch (error) {
    console.error("获取营销活动列表失败:", error)
    return res.status(500).json({
      message: error.message || "获取营销活动列表失败"
    })
  }
}

export async function POST(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    console.log("创建营销活动, 请求体:", req.body);
    
    // 转换请求数据格式以匹配后端期望
    const requestData = {
      name: req.body.name,
      description: req.body.description,
      campaignIdentifier: req.body.campaign_identifier,
      startsAt: req.body.starts_at,
      endsAt: req.body.ends_at,
      budget: req.body.budget ? {
        type: req.body.budget.type,
        currencyCode: req.body.budget.currency_code,
        limit: req.body.budget.limit,
        used: req.body.budget.used || 0
      } : null
    };

    const response = await fetch(`${BACKEND_URL}/campaigns`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(requestData),
    });

    if (!response.ok) {
      const errorData = await response.json();
      console.error("创建营销活动失败:", errorData);
      throw new Error(errorData.message || "创建营销活动失败");
    }

    const data = await response.json();
    console.log("营销活动创建成功:", data);

    return res.json(data);
  } catch (error) {
    console.error("创建营销活动失败:", error);
    return res.status(500).json({
      message: error.message || "创建营销活动失败"
    });
  }
} 