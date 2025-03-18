import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"
import { ReservationRequest, ReservationResponse } from "@/types/api"

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const { offset = 0, limit = 10 } = req.query
    const page = Math.floor(Number(offset) / Number(limit))

    const response = await fetch(
      `http://localhost:8080/api/reservations?page=${page}&size=${limit}`,
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    )

    if (!response.ok) {
      throw new Error('获取预订列表失败')
    }

    const data = await response.json()

    // 转换为 Medusa Admin UI 期望的格式
    const formattedReservations = data.reservations.map(reservation => ({
      id: reservation.id,
      line_item_id: reservation.lineItemId,
      location_id: reservation.locationId,
      quantity: reservation.quantity,
      external_id: reservation.externalId,
      description: reservation.description,
      inventory_item_id: reservation.inventoryItemId
    }))

    res.json({
      reservations: formattedReservations,
      count: data.count,
      offset: Number(offset),
      limit: Number(limit)
    })

  } catch (error) {
    console.error('获取预订列表失败:', error)
    res.status(500).json({
      message: error.message || '获取预订列表失败'
    })
  }
}

export async function POST(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    console.log('=== 开始创建预订 ===')
    const body = req.body as ReservationRequest
    console.log('请求数据:', body)

    // 构造请求数据
    const requestData = {
      line_item_id: body.line_item_id,
      inventory_item_id: body.inventory_item_id,
      location_id: body.location_id,
      quantity: body.quantity,
      description: body.description,
      external_id: body.external_id,
      metadata: body.metadata || {}
    }

    const response = await fetch('http://localhost:8080/api/reservations', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestData)
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || '创建预订失败')
    }

    const data = await response.json()

    // 转换为 Medusa Admin UI 期望的格式
    const formattedReservation: ReservationResponse = {
      id: data.id,
      line_item_id: data.lineItemId,
      location_id: data.locationId,
      quantity: data.quantity,
      external_id: data.externalId,
      description: data.description,
      inventory_item_id: data.inventoryItemId
    }

    res.json({
      reservation: formattedReservation
    })

  } catch (error) {
    console.error('创建预订失败:', error)
    res.status(500).json({
      message: error.message || '创建预订失败'
    })
  }
} 