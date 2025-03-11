import { MedusaRequest, MedusaResponse } from "@medusajs/medusa"

export async function POST(
  req: MedusaRequest,
  res: MedusaResponse
): Promise<void> {
  try {
    console.log('\n=== 开始创建分类 ===');
    console.log('请求数据:', req.body);

    // 构造创建数据
    const categoryData = {
      name: req.body.name,
      handle: req.body.handle,
      description: req.body.description,
      isInternal: req.body.is_internal,
      isActive: req.body.is_active,
      rank: req.body.rank,
      parentCategoryId: req.body.parent_category_id 
        ? Number(req.body.parent_category_id)
        : null
    };

    // 发送创建请求到后端
    const response = await fetch('http://localhost:8080/api/categories', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify(categoryData)
    });

    console.log('创建请求状态:', response.status);

    if (!response.ok) {
      const errorText = await response.text();
      return res.status(response.status).json({
        message: errorText || 'Failed to create category',
        type: 'error'
      });
    }

    // 获取创建后的数据
    const data = await response.json();

    // 转换为 Medusa Admin UI 期望的格式
    const formattedCategory = {
      id: data.id,
      name: data.name,
      handle: data.handle,
      description: data.description || '',
      is_internal: data.isInternal,
      is_active: data.isActive,
      rank: data.rank || 0,
      parent_category_id: data.parentCategoryId,
      parent_category: data.parentCategory ? {
        id: data.parentCategory.id,
        name: data.parentCategory.name,
        handle: data.parentCategory.handle,
        description: data.parentCategory.description || '',
        is_internal: data.parentCategory.isInternal,
        is_active: data.parentCategory.isActive,
        rank: data.parentCategory.rank || 0,
        created_at: data.parentCategory.createdAt,
        updated_at: data.parentCategory.updatedAt
      } : null,
      category_children: [],
      created_at: data.createdAt,
      updated_at: data.updatedAt
    };

    // 返回创建的分类数据
    return res.json({
      product_category: formattedCategory
    });

  } catch (error: any) {
    console.error('创建分类时发生错误:', error);
    return res.status(500).json({
      message: error.message || '创建分类失败',
      type: 'server_error',
      code: 'unexpected_error'
    });
  }
} 