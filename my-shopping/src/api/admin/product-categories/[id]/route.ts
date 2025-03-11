import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http"

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const id = req.params.id
    const fields = req.query.fields as string
    const include_descendants_tree = req.query.include_descendants_tree === 'true'

    console.log('\n=== 开始获取分类详情 ===')
    console.log('分类ID:', id)
    console.log('请求字段:', fields)
    console.log('包含子分类树:', include_descendants_tree)

    const response = await fetch(`http://localhost:8080/api/categories/${id}${include_descendants_tree ? '?include_descendants_tree=true' : ''}`, {
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

    // 格式化响应数据
    if (data.product_categories) {
      // 如果是列表响应
      res.json({
        product_categories: data.product_categories.map(category => ({
          id: category.id,
          name: category.name,
          handle: category.handle,
          description: category.description,
          is_internal: category.is_internal,
          is_active: category.is_active,
          rank: category.rank,
          parent_category_id: category.parent_category_id,
          parent_category: category.parent_category,
          category_children: category.category_children || [],
          created_at: category.created_at,
          updated_at: category.updated_at,
          deleted_at: null
        })),
        count: data.count,
        offset: data.offset,
        limit: data.limit
      })
    } else {
      // 如果是单个分类响应
      res.json({
        product_category: {
          id: data.product_category.id,
          name: data.product_category.name,
          handle: data.product_category.handle,
          description: data.product_category.description,
          is_internal: data.product_category.is_internal,
          is_active: data.product_category.is_active,
          rank: data.product_category.rank,
          parent_category_id: data.product_category.parent_category_id,
          parent_category: data.product_category.parent_category,
          category_children: data.product_category.category_children || [],
          created_at: data.product_category.created_at,
          updated_at: data.product_category.updated_at,
          deleted_at: null,
          metadata: data.product_category.metadata || {}
        }
      })
    }
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

    // 转换请求数据格式以匹配后端期望
    const requestData = {
      name: updateData.name,
      handle: updateData.handle,
      description: updateData.description,
      is_internal: updateData.is_internal,
      is_active: updateData.is_active,
      rank: updateData.rank,
      metadata: updateData.metadata || {}
    }

    const response = await fetch(`http://localhost:8080/api/categories/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestData)
    })

    if (!response.ok) {
      throw new Error('更新分类失败')
    }

    // 更新成功后，重新获取分类详情
    const getResponse = await fetch(`http://localhost:8080/api/categories/${id}?include_descendants_tree=true`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (!getResponse.ok) {
      throw new Error('获取更新后的分类详情失败')
    }

    const updatedData = await getResponse.json()
    console.log('更新后的分类:', updatedData)

    // 返回符合 Medusa Admin UI 期望的格式
    res.json({
      product_category: {
        id: updatedData.product_category.id,
        name: updatedData.product_category.name,
        handle: updatedData.product_category.handle,
        description: updatedData.product_category.description,
        is_internal: updatedData.product_category.is_internal,
        is_active: updatedData.product_category.is_active,
        rank: updatedData.product_category.rank,
        parent_category_id: updatedData.product_category.parent_category_id,
        parent_category: updatedData.product_category.parent_category,
        category_children: updatedData.product_category.category_children || [],
        created_at: updatedData.product_category.created_at,
        updated_at: updatedData.product_category.updated_at,
        deleted_at: null,
        metadata: updatedData.product_category.metadata || {}
      }
    })
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

    const response = await fetch(`http://localhost:8080/api/categories/${id}`, {
      method: 'DELETE',
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
      throw new Error('删除分类失败')
    }

    // 返回符合 Medusa Admin UI 期望的格式
    res.status(200).json({
      id,
      object: 'product_category',
      deleted: true
    })
  } catch (error) {
    console.error('删除分类失败:', error)
    res.status(500).json({
      message: error.message || '删除分类失败',
      type: "error",
      code: "UNKNOWN"
    })
  }
}

export async function POST(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const id = req.params.id
    const updateData = req.body

    console.log('\n=== 开始更新分类(POST) ===')
    console.log('分类ID:', id)
    console.log('更新数据:', updateData)

    // 转换请求数据格式以匹配后端期望
    const requestData = {
      name: updateData.name,
      handle: updateData.handle,
      description: updateData.description,
      is_internal: updateData.is_internal,
      is_active: updateData.is_active,
      rank: updateData.rank,
      metadata: updateData.metadata || {}
    }

    const response = await fetch(`http://localhost:8080/api/categories/${id}`, {
      method: 'PUT',  // 后端仍然使用 PUT
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestData)
    })

    if (!response.ok) {
      throw new Error('更新分类失败')
    }

    // 更新成功后，重新获取分类详情
    const getResponse = await fetch(`http://localhost:8080/api/categories/${id}?include_descendants_tree=true`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (!getResponse.ok) {
      throw new Error('获取更新后的分类详情失败')
    }

    const updatedData = await getResponse.json()
    console.log('更新后的分类:', updatedData)

    // 返回符合 Medusa Admin UI 期望的格式
    res.json({
      product_category: {
        id: updatedData.product_category.id,
        name: updatedData.product_category.name,
        handle: updatedData.product_category.handle,
        description: updatedData.product_category.description,
        is_internal: updatedData.product_category.is_internal,
        is_active: updatedData.product_category.is_active,
        rank: updatedData.product_category.rank,
        parent_category_id: updatedData.product_category.parent_category_id,
        parent_category: updatedData.product_category.parent_category,
        category_children: updatedData.product_category.category_children || [],
        created_at: updatedData.product_category.created_at,
        updated_at: updatedData.product_category.updated_at,
        deleted_at: null,
        metadata: updatedData.product_category.metadata || {}
      }
    })
  } catch (error) {
    console.error('更新分类失败:', error)
    res.status(500).json({
      message: error.message || '更新分类失败'
    })
  }
} 