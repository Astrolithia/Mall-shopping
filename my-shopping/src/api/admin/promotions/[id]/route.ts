import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"

const BACKEND_URL = "http://localhost:8080/api"

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const { id } = req.params
    console.log("获取促销活动详情, ID:", id)

    if (!id) {
      return res.status(404).json({
        type: "not_found",
        message: "Promotion not found",
        code: "promotion.not_found"
      })
    }

    const response = await fetch(
      `${BACKEND_URL}/promotions/${id}`,
      {
        headers: {
          'Content-Type': 'application/json'
        },
        cache: 'no-store'
      }
    )

    if (!response.ok) {
      if (response.status === 404) {
        return res.status(404).json({
          type: "not_found",
          message: "Promotion not found",
          code: "promotion.not_found"
        })
      }
      throw new Error('获取促销活动详情失败')
    }

    const data = await response.json()
    return res.json(data)
  } catch (error) {
    console.error("获取促销活动详情失败:", error)
    return res.status(500).json({
      message: error.message || "获取促销活动详情失败"
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

    console.log('\n=== 开始更新促销活动 ===')
    console.log('促销活动ID:', id)
    console.log('更新数据:', updateData)

    // 转换请求数据格式以匹配后端期望
    const requestData = {
      code: updateData.code,
      type: updateData.type,
      isAutomatic: updateData.is_automatic,
      campaignId: updateData.campaign_id,
      status: updateData.status,
      applicationMethod: updateData.application_method ? {
        description: updateData.application_method.description,
        value: updateData.application_method.value,
        currencyCode: updateData.application_method.currency_code,
        maxQuantity: updateData.application_method.max_quantity,
        type: updateData.application_method.type,
        targetType: updateData.application_method.target_type,
        allocation: updateData.application_method.allocation,
        targetRules: updateData.application_method.target_rules,
        buyRules: updateData.application_method.buy_rules,
        applyToQuantity: updateData.application_method.apply_to_quantity,
        buyRulesMinQuantity: updateData.application_method.buy_rules_min_quantity
      } : null,
      rules: updateData.rules
    }

    const response = await fetch(
      `${BACKEND_URL}/promotions/${id}`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
      }
    )

    if (!response.ok) {
      const errorData = await response.json()
      console.error('更新促销活动失败:', errorData)
      throw new Error(errorData.message || '更新促销活动失败')
    }

    const data = await response.json()
    console.log('促销活动更新成功:', data)

    // 格式化响应以匹配 Medusa Admin UI 期望的格式
    return res.json({
      promotion: {
        id: data.promotion.id,
        code: data.promotion.code,
        type: data.promotion.type,
        is_automatic: data.promotion.isAutomatic,
        campaign_id: data.promotion.campaignId,
        status: data.promotion.status,
        rules: data.promotion.rules || [],
        application_method: data.promotion.applicationMethod,
        created_at: data.promotion.createdAt,
        updated_at: data.promotion.updatedAt,
        deleted_at: data.promotion.deletedAt,
        metadata: data.promotion.metadata || {}
      }
    })

  } catch (error) {
    console.error('更新促销活动失败:', error)
    return res.status(500).json({
      message: error.message || '更新促销活动失败'
    })
  }
}

export async function DELETE(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const { id } = req.params
    console.log("开始删除促销活动, ID:", id)

    const response = await fetch(
      `${BACKEND_URL}/promotions/${id}`,
      {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json'
        }
      }
    )

    if (!response.ok) {
      if (response.status === 404) {
        return res.status(404).json({
          type: "not_found",
          message: "Promotion not found",
          code: "promotion.not_found"
        })
      }
      throw new Error('删除促销活动失败')
    }

    // 返回符合 Medusa Admin UI 期望的格式
    return res.json({
      id,
      object: "promotion",
      deleted: true
    })

  } catch (error) {
    console.error("删除促销活动失败:", error)
    return res.status(500).json({
      message: error.message || "删除促销活动失败"
    })
  }
} 