import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const id = req.params.id
    const fields = req.query.fields as string

    console.log('\n=== 开始获取分类详情 ===')
    console.log('分类ID:', id)
    console.log('请求字段:', fields)

    const response = await fetch(`http://localhost:8080/api/categories/${id}?${fields ? `fields=${fields}` : ''}`, {
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

    const response = await fetch(`http://localhost:8080/api/product-categories/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(updateData)
    })

    if (!response.ok) {
      throw new Error('更新分类失败')
    }

    const updatedCategory = await response.json()
    console.log('更新后的分类:', updatedCategory)

    res.json(updatedCategory)
  } catch (error) {
    console.error('更新分类失败:', error)
    res.status(500).json({
      message: error.message || '更新分类失败'
    })
  }
}

export async function DELETE(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const id = req.params.id

    console.log('\n=== 开始删除分类 ===')
    console.log('分类ID:', id)

    const response = await fetch(`http://localhost:8080/admin/product-categories/${id}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error('删除分类失败')
    }

    res.status(200).json({
      id,
      object: 'product_category',
      deleted: true
    })
  } catch (error) {
    console.error('删除分类失败:', error)
    res.status(500).json({
      message: error.message || '删除分类失败'
    })
  }
} 