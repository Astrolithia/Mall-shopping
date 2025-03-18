import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"

const BACKEND_URL = "http://localhost:8080/api"

export async function DELETE(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const { id } = req.params
    
    // 打印整个请求体，看看前端发送了什么
    console.log("收到的请求体:", req.body);
    
    // 尝试从不同的位置获取 customer_ids
    let customer_ids = req.body?.customer_ids || [];
    
    // 如果 customer_ids 不在请求体的顶层，尝试其他可能的位置
    if (!customer_ids || customer_ids.length === 0) {
      if (req.body?.remove && Array.isArray(req.body.remove)) {
        // 前端可能发送的是 { remove: [ '7', '8' ] }
        customer_ids = req.body.remove;
      } else if (req.body?.payload?.customer_ids) {
        customer_ids = req.body.payload.customer_ids;
      } else if (req.body?.data?.customer_ids) {
        customer_ids = req.body.data.customer_ids;
      }
    }
    
    console.log("从客户群组中移除客户, 群组ID:", id, "客户IDs:", customer_ids);

    // 如果仍然没有找到有效的 customer_ids，返回错误
    if (!customer_ids || !Array.isArray(customer_ids) || customer_ids.length === 0) {
      return res.status(400).json({
        message: "必须提供要移除的客户ID列表"
      })
    }

    const response = await fetch(`${BACKEND_URL}/customers/groups/${id}/customers`, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ customer_ids })
    })

    if (!response.ok) {
      if (response.status === 404) {
        return res.status(404).json({
          message: "客户群组不存在"
        })
      }
      throw new Error('从客户群组中移除客户失败')
    }

    return res.status(200).json({})
  } catch (error) {
    console.error("从客户群组中移除客户失败:", error);
    return res.status(500).json({
      message: error.message || "从客户群组中移除客户失败"
    })
  }
}

export async function POST(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const { id } = req.params
    
    // 打印整个请求体，看看前端发送了什么
    console.log("收到的请求体:", req.body);
    
    // 尝试从不同的位置获取 customer_ids
    let customer_ids = req.body?.customer_ids || [];
    
    // 如果 customer_ids 不在请求体的顶层，尝试其他可能的位置
    if (!customer_ids || customer_ids.length === 0) {
      if (req.body?.add && Array.isArray(req.body.add)) {
        // 前端发送的是 { add: [ '7', '8' ] }
        customer_ids = req.body.add;
      } else if (req.body?.payload?.customer_ids) {
        customer_ids = req.body.payload.customer_ids;
      } else if (req.body?.data?.customer_ids) {
        customer_ids = req.body.data.customer_ids;
      }
    }
    
    console.log("向客户群组添加客户, 群组ID:", id, "客户IDs:", customer_ids);

    // 如果仍然没有找到有效的 customer_ids，返回错误
    if (!customer_ids || !Array.isArray(customer_ids) || customer_ids.length === 0) {
      return res.status(400).json({
        message: "必须提供要添加的客户ID列表"
      })
    }

    const response = await fetch(`${BACKEND_URL}/customers/groups/${id}/customers`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ customer_ids })
    })

    if (!response.ok) {
      if (response.status === 404) {
        return res.status(404).json({
          message: "客户群组不存在"
        })
      }
      throw new Error('向客户群组添加客户失败')
    }

    const data = await response.json()
    console.log("向客户群组添加客户成功:", data);

    return res.json(data)
  } catch (error) {
    console.error("向客户群组添加客户失败:", error);
    return res.status(500).json({
      message: error.message || "向客户群组添加客户失败"
    })
  }
} 