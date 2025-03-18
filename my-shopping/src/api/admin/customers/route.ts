import type { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"
import { QueryClient } from "@tanstack/react-query"

const BACKEND_URL = "http://localhost:8080/api"

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    console.log("Fetching customers list");
    const { offset = 0, limit = 10 } = req.query
    
    // 计算页码
    const page = Math.floor(Number(offset) / Number(limit))
    const size = Number(limit)

    const response = await fetch(
      `${BACKEND_URL}/customers?page=${page}&size=${size}`,
      {
        credentials: "include",
        headers: {
          'Cache-Control': 'no-cache',
          'Pragma': 'no-cache'
        }
      }
    )

    if (!response.ok) {
      throw new Error('Failed to fetch customers')
    }

    const data = await response.json()
    console.log("Customers list fetched:", data);

    // 转换响应格式以匹配 Medusa 期望的格式
    return res.json({
      customers: data.customers,
      count: data.count,
      offset: data.offset,
      limit: data.limit
    })
  } catch (error) {
    console.error("Error fetching customers:", error);
    throw error;
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