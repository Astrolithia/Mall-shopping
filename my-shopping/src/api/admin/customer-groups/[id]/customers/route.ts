import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"

const BACKEND_URL = "http://localhost:8080/api"

export async function POST(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const { id } = req.params
    
    // 打印整个请求体，看看前端发送了什么
    console.log("收到的请求体:", req.body);
    
    // 从请求体中获取 add 和 remove 参数
    const add = req.body?.add || [];
    const remove = req.body?.remove || [];
    
    console.log("向客户群组添加/移除客户, 群组ID:", id, "添加客户IDs:", add, "移除客户IDs:", remove);

    // 如果既没有要添加的客户，也没有要移除的客户，返回错误
    if ((!add || !Array.isArray(add) || add.length === 0) && 
        (!remove || !Array.isArray(remove) || remove.length === 0)) {
      return res.status(400).json({
        message: "必须提供要添加或移除的客户ID列表"
      })
    }

    let response;
    
    // 如果有要添加的客户，发送添加请求
    if (add && Array.isArray(add) && add.length > 0) {
      response = await fetch(`${BACKEND_URL}/customers/groups/${id}/customers`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ customer_ids: add })
      });
      
      if (!response.ok) {
        if (response.status === 404) {
          return res.status(404).json({
            message: "客户群组不存在"
          })
        }
        throw new Error('向客户群组添加客户失败')
      }
      
      console.log("向客户群组添加客户成功");
    }
    
    // 如果有要移除的客户，发送移除请求
    if (remove && Array.isArray(remove) && remove.length > 0) {
      response = await fetch(`${BACKEND_URL}/customers/groups/${id}/customers`, {
        method: "DELETE",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ customer_ids: remove })
      });
      
      if (!response.ok) {
        if (response.status === 404) {
          return res.status(404).json({
            message: "客户群组不存在"
          })
        }
        throw new Error('从客户群组中移除客户失败')
      }
      
      console.log("从客户群组中移除客户成功");
    }

    // 获取更新后的客户群组详情
    const getResponse = await fetch(
      `${BACKEND_URL}/customers/groups/${id}`,
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    )

    if (!getResponse.ok) {
      throw new Error('获取更新后的客户群组详情失败')
    }

    const data = await getResponse.json()
    console.log("客户群组更新成功:", data);

    return res.json(data)
  } catch (error) {
    console.error("更新客户群组失败:", error);
    return res.status(500).json({
      message: error.message || "更新客户群组失败"
    })
  }
} 