import type { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"
import { QueryClient } from "@tanstack/react-query"

const BACKEND_URL = "http://localhost:8080/api"
const queryClient = new QueryClient()

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
) {
  // 从路径中获取ID
  const id = req.params.id

  const response = await fetch(
    `${BACKEND_URL}/customers/${id}`,
    {
      credentials: "include",
    }
  )

  const data = await response.json()
  return res.json(data)
}

export async function POST(
  req: MedusaRequest,
  res: MedusaResponse
) {
  // 从路径中获取ID
  const id = req.params.id
  
  // 转换请求数据格式以匹配后端期望
  const requestData = {
    email: req.body.email,
    companyName: req.body.company_name,
    firstName: req.body.first_name,
    lastName: req.body.last_name,
    phone: req.body.phone,
    metadata: req.body.metadata || {}
  }

  const response = await fetch(`${BACKEND_URL}/customers/${id}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    credentials: "include",
    body: JSON.stringify(requestData),
  })

  const data = await response.json()
  
  // 更新成功后，使缓存失效
  await queryClient.invalidateQueries({ queryKey: ["customers"] })
  
  return res.json(data)
}

export async function DELETE(
  req: MedusaRequest,
  res: MedusaResponse
) {
  // 从路径中获取ID
  const id = req.params.id

  const response = await fetch(`${BACKEND_URL}/customers/${id}`, {
    method: "DELETE",
    credentials: "include",
  })

  const data = await response.json()
  return res.json(data)
} 