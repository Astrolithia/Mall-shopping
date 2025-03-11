import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const id = req.params.id
    const fields = req.query.fields as string

    console.log('\n=== 开始获取分类编辑详情 ===')
    console.log('分类ID:', id)
    console.log('请求字段:', fields)

    const response = await fetch(`http://localhost:8080/api/categories/${id}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      if (response.status === 404) {
        return res.status(404).json({
          message: `Product category with id: ${id} was not found`,
          type: "not_found",
          code: "UNKNOWN"
        })
      }
      throw new Error(`获取分类详情失败: ${response.status}`)
    }

    const data = await response.json()
    console.log('获取到的分类数据:', data)

    res.json(data)
  } catch (error) {
    console.error('获取分类详情失败:', error)
    res.status(500).json({
      message: error.message || '获取分类详情失败'
    })
  }
}

export async function PUT(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const id = req.params.id
    const updateData = req.body

    console.log('\n=== 开始更新分类 ===')
    console.log('分类ID:', id)
    console.log('更新数据:', updateData)

    const response = await fetch(`http://localhost:8080/api/categories/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(updateData)
    })

    if (!response.ok) {
      throw new Error('更新分类失败')
    }

    const updatedData = await response.json()
    console.log('更新后的分类:', updatedData)

    // 转换为 Medusa Admin UI 期望的格式
    const formattedCategory = {
      id: updatedData.product_category.id,
      name: updatedData.product_category.name,
      handle: updatedData.product_category.handle,
      description: updatedData.product_category.description,
      is_internal: updatedData.product_category.is_internal,
      is_active: updatedData.product_category.is_active,
      rank: updatedData.product_category.rank,
      parent_category_id: updatedData.product_category.parent_category_id,
      created_at: updatedData.product_category.created_at,
      updated_at: updatedData.product_category.updated_at,
      deleted_at: null,
      metadata: updatedData.product_category.metadata || {}
    }

    res.json({
      product_category: formattedCategory
    })
  } catch (error) {
    console.error('更新分类失败:', error)
    res.status(500).json({
      message: error.message || '更新分类失败'
    })
  }
} 