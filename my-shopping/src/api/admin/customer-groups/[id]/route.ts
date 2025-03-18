import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"

const BACKEND_URL = "http://localhost:8080/api"

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const { id } = req.params
    console.log("获取客户群组详情, ID:", id);

    const response = await fetch(
      `${BACKEND_URL}/customers/groups/${id}`,
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    )

    if (!response.ok) {
      if (response.status === 404) {
        return res.status(404).json({
          message: "客户群组不存在"
        })
      }
      throw new Error('获取客户群组详情失败')
    }

    const data = await response.json()
    console.log('获取到的客户群组详情:', data)

    return res.json(data)
  } catch (error) {
    console.error("获取客户群组详情失败:", error);
    return res.status(500).json({
      message: error.message || "获取客户群组详情失败"
    })
  }
}

export async function POST(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const { id } = req.params
    console.log("更新客户群组, ID:", id);
    const requestData = req.body

    const response = await fetch(`${BACKEND_URL}/customers/groups/${id}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(requestData),
    })

    if (!response.ok) {
      if (response.status === 404) {
        return res.status(404).json({
          message: "客户群组不存在"
        })
      }
      const errorData = await response.json();
      console.error("更新客户群组失败:", errorData);
      throw new Error(errorData.message || "更新客户群组失败");
    }

    const data = await response.json()
    console.log("客户群组更新成功:", data);

    return res.json(data)
  } catch (error) {
    console.error("更新客户群组失败:", error);
    return res.status(500).json({
      message: error.message || "更新客户群组失败"
    })
  }
}

export async function DELETE(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const { id } = req.params
    console.log("删除客户群组, ID:", id);

    const response = await fetch(`${BACKEND_URL}/customers/groups/${id}`, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
      }
    })

    if (!response.ok) {
      if (response.status === 404) {
        return res.status(404).json({
          message: "客户群组不存在"
        })
      }
      throw new Error('删除客户群组失败')
    }

    return res.status(200).json({})
  } catch (error) {
    console.error("删除客户群组失败:", error);
    return res.status(500).json({
      message: error.message || "删除客户群组失败"
    })
  }
} 