import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"

const BACKEND_URL = "http://localhost:8080/api"

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const { q, limit = 20, offset = 0, fields } = req.query
    
    console.log('获取促销活动列表, 查询参数:', { limit, offset, q, fields })
    
    // 计算页码
    const page = Math.floor(Number(offset) / Number(limit))
    
    // 构建后端请求URL
    let backendUrl = `${BACKEND_URL}/promotions?page=${page}&size=${limit}`
    if (q) {
      backendUrl += `&q=${encodeURIComponent(q as string)}`
    }
    if (fields) {
      backendUrl += `&fields=${encodeURIComponent(fields as string)}`
    }
    console.log('请求后端URL:', backendUrl)

    const response = await fetch(backendUrl, {
      headers: {
        'Content-Type': 'application/json'
      },
      cache: 'no-store'
    })

    if (!response.ok) {
      throw new Error('获取促销活动列表失败')
    }

    const data = await response.json()
    console.log('后端返回的原始数据:', data)
    
    return res.json(data)

  } catch (error) {
    console.error("获取促销活动列表失败:", error)
    return res.status(500).json({
      message: error.message || "获取促销活动列表失败"
    })
  }
}

export async function POST(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    console.log("创建促销活动, 请求体:", req.body);
    
    // 转换请求数据格式以匹配后端期望
    const requestData = {
      code: req.body.code,
      type: req.body.type,
      isAutomatic: req.body.is_automatic || false,
      campaignId: req.body.campaign_id,
      status: req.body.status || "draft",
      applicationMethod: req.body.application_method ? {
        description: req.body.application_method.description,
        value: req.body.application_method.value,
        currencyCode: req.body.application_method.currency_code,
        maxQuantity: req.body.application_method.max_quantity,
        type: req.body.application_method.type,
        targetType: req.body.application_method.target_type,
        allocation: req.body.application_method.allocation,
        targetRules: req.body.application_method.target_rules,
        buyRules: req.body.application_method.buy_rules,
        applyToQuantity: req.body.application_method.apply_to_quantity,
        buyRulesMinQuantity: req.body.application_method.buy_rules_min_quantity
      } : null,
      rules: req.body.rules
    };

    const response = await fetch(`${BACKEND_URL}/promotions`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(requestData),
    });

    if (!response.ok) {
      const errorData = await response.json();
      console.error("创建促销活动失败:", errorData);
      throw new Error(errorData.message || "创建促销活动失败");
    }

    const data = await response.json();
    console.log("促销活动创建成功:", data);

    return res.json(data);
  } catch (error) {
    console.error("创建促销活动失败:", error);
    return res.status(500).json({
      message: error.message || "创建促销活动失败"
    });
  }
} 