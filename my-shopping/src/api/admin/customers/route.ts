import type { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"
import { QueryClient } from "@tanstack/react-query"

const BACKEND_URL = "http://localhost:8080/api"

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const { q, limit = 10, offset = 0, expand, fields, groups } = req.query
    
    console.log("获取客户列表, 查询参数:", req.query);
    
    // 计算页码
    const page = Math.floor(Number(offset) / Number(limit))
    const size = Number(limit)
    
    let url = `${BACKEND_URL}/customers?page=${page}&size=${size}`
    
    // 如果指定了客户群组 ID，则获取该群组中的客户
    if (groups) {
      console.log("获取客户群组中的客户, 群组ID:", groups);
      url = `${BACKEND_URL}/customers/groups/${groups}/customers?page=${page}&size=${size}`
    }
    
    const response = await fetch(url, {
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    if (!response.ok) {
      throw new Error('获取客户列表失败')
    }
    
    const data = await response.json()
    console.log("获取到的客户数据:", data);
    
    // 确保响应格式符合 Medusa Admin UI 的期望
    return res.json({
      customers: data.customers || [],
      count: data.count || 0,
      offset: Number(offset),
      limit: Number(limit)
    })
  } catch (error) {
    console.error("获取客户列表失败:", error);
    return res.status(500).json({
      message: error.message || "获取客户列表失败"
    })
  }
}

export async function POST(
  req: MedusaRequest,
  res: MedusaResponse
) {
  // 转换请求数据格式以匹配后端期望
  const requestData = {
    email: req.body.email,
    companyName: req.body.company_name,
    firstName: req.body.first_name,
    lastName: req.body.last_name,
    phone: req.body.phone,
    metadata: req.body.metadata || {}
  }

  try {
    console.log("Creating customer with data:", requestData);

    const response = await fetch(`${BACKEND_URL}/customers`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
      body: JSON.stringify(requestData),
    })

    if (!response.ok) {
      const errorData = await response.json();
      console.error("Failed to create customer:", errorData);
      throw new Error(errorData.message || "Failed to create customer");
    }

    const data = await response.json()
    console.log("Customer created successfully:", data);

    // 创建成功后，使缓存失效并重新获取数据
    const queryClient = new QueryClient()
    await queryClient.invalidateQueries({ queryKey: ["admin_customers"] })
    await queryClient.refetchQueries({ queryKey: ["admin_customers"] })

    return res.json(data)
  } catch (error) {
    console.error("Error in customer creation:", error);
    throw error;
  }
} 