import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const offset = req.query.offset as string
    const limit = req.query.limit as string

    console.log('\n=== 开始获取预订列表 ===')
    console.log('偏移量:', offset)
    console.log('限制数:', limit)

    const response = await fetch(
      `http://localhost:8080/api/reservations?${new URLSearchParams({
        offset: offset || '0',
        limit: limit || '10'
      })}`,
      {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      }
    )

    if (!response.ok) {
      throw new Error('获取预订列表失败')
    }

    const data = await response.json()
    console.log('获取到的预订数据:', data)

    // 直接返回后端数据，因为格式已经符合 Medusa Admin UI 的要求
    res.json(data)

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
    const createData = req.body

    console.log('\n=== 开始创建预订 ===')
    console.log('创建数据:', createData)

    const response = await fetch('http://localhost:8080/api/reservations', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(createData)
    })

    if (!response.ok) {
      throw new Error('创建预订失败')
    }

    const data = await response.json()
    console.log('创建的预订:', data)

    // 直接返回后端数据，因为格式已经符合 Medusa Admin UI 的要求
    res.json(data)

  } catch (error) {
    console.error('创建预订失败:', error)
    res.status(500).json({
      message: error.message || '创建预订失败'
    })
  }
} 