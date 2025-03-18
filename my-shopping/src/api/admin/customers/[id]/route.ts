import { MedusaRequest, MedusaResponse } from "@medusajs/medusa"

const BACKEND_URL = "http://localhost:8080/api"

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
) {
  const { id } = req.params

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
  const { id } = req.params

  const response = await fetch(`${BACKEND_URL}/customers/${id}`, {
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

export async function DELETE(
  req: MedusaRequest,
  res: MedusaResponse
) {
  const { id } = req.params

  const response = await fetch(`${BACKEND_URL}/customers/${id}`, {
    method: "DELETE",
    credentials: "include",
  })

  const data = await response.json()
  return res.json(data)
} 