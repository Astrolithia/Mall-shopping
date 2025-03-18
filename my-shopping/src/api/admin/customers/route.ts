import { MedusaRequest, MedusaResponse } from "@medusajs/medusa"

const BACKEND_URL = "http://localhost:8080/api"

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
) {
  const { page = 0, size = 10 } = req.query

  const response = await fetch(
    `${BACKEND_URL}/customers?page=${page}&size=${size}`,
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
  const response = await fetch(`${BACKEND_URL}/customers`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    credentials: "include",
    body: JSON.stringify(req.body),
  })

  const data = await response.json()
  return res.json(data)
} 