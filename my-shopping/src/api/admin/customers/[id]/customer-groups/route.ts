import type { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"

const BACKEND_URL = "http://localhost:8080/api"

export async function POST(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const { id } = req.params
    const { add, remove } = req.body as { add?: string[], remove?: string[] }

    console.log("Updating customer groups:", { 
      customerId: id, 
      addGroups: add,
      removeGroups: remove 
    });

    let response;

    // 处理添加群组
    if (add && add.length > 0) {
      response = await fetch(
        `${BACKEND_URL}/customers/${id}/customer-groups`,
        {
          method: "POST",
          credentials: "include",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify({ groupIds: add })
        }
      )
    }

    // 处理移除群组
    if (remove && remove.length > 0) {
      response = await fetch(
        `${BACKEND_URL}/customers/${id}/customer-groups/remove`,
        {
          method: "POST",
          credentials: "include",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify({ groupIds: remove })
        }
      )
    }

    if (!response?.ok) {
      const errorData = await response.json()
      console.error("Failed to update customer groups:", errorData)
      throw new Error(errorData.message || "Failed to update customer groups")
    }

    const data = await response.json()
    console.log("Customer groups updated successfully:", data)

    return res.json(data)
  } catch (error) {
    console.error("Error updating customer groups:", error)
    throw error
  }
} 