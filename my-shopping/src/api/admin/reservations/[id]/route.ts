import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"
import { ReservationResponse } from "@/types/api"

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const { id } = req.params
    console.log('获取预订详情, ID:', id)

    if (!id) {
      return res.status(404).json({
        type: "not_found",
        message: "Reservation not found",
        code: "reservation.not_found"
      })
    }

    const response = await fetch(
      `http://localhost:8080/api/reservations/${id}`,
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    )

    if (!response.ok) {
      if (response.status === 404) {
        return res.status(404).json({
          type: "not_found",
          message: "Reservation not found",
          code: "reservation.not_found"
        })
      }
      throw new Error(`获取预订失败: ${response.status}`)
    }

    const data = await response.json()
    console.log('获取到的预订数据:', data)

    // 获取库存项目详情
    const inventoryResponse = await fetch(
      `http://localhost:8080/api/inventories/${data.inventoryItemId}`,
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    )

    const inventoryData = await inventoryResponse.json()
    console.log('获取到的库存项目数据:', inventoryData)

    // 获取位置详情
    const locationResponse = await fetch(
      `http://localhost:8080/api/locations/${data.locationId}`,
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    )

    const locationData = await locationResponse.json()
    console.log('获取到的位置数据:', locationData)

    // 转换为 Medusa Admin UI 期望的格式
    const formattedReservation: ReservationResponse = {
      id: data.id,
      line_item_id: data.lineItemId,
      location_id: data.locationId,
      inventory_item_id: data.inventoryItemId,
      quantity: data.quantity,
      external_id: data.externalId || null,
      description: data.description || null,
      metadata: data.metadata || {}
    }

    res.json({
      reservation: formattedReservation
    })

  } catch (error) {
    console.error('获取预订详情失败:', error)
    res.status(404).json({
      type: "not_found",
      message: "Reservation not found",
      code: "reservation.not_found"
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
    console.log('更新预订, ID:', id)
    console.log('更新数据:', updateData)

    if (!id) {
      return res.status(404).json({
        type: "not_found",
        message: "Reservation not found",
        code: "reservation.not_found"
      })
    }

    const response = await fetch(
      `http://localhost:8080/api/reservations/${id}`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          description: updateData.description,
          metadata: updateData.metadata || {}
        })
      }
    )

    if (!response.ok) {
      if (response.status === 404) {
        return res.status(404).json({
          type: "not_found",
          message: "Reservation not found",
          code: "reservation.not_found"
        })
      }
      throw new Error(`更新预订失败: ${response.status}`)
    }

    const data = await response.json()
    console.log('更新后的预订数据:', data)

    // 转换为 Medusa Admin UI 期望的格式
    const formattedReservation: ReservationResponse = {
      id: data.id,
      line_item_id: data.lineItemId,
      location_id: data.locationId,
      inventory_item_id: data.inventoryItemId,
      quantity: data.quantity,
      external_id: data.externalId || null,
      description: data.description || null,
      metadata: data.metadata || {}
    }

    res.json({
      reservation: formattedReservation
    })

  } catch (error) {
    console.error('更新预订失败:', error)
    res.status(500).json({
      message: error.message || '更新预订失败'
    })
  }
} 