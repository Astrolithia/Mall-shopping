import { MedusaRequest, MedusaResponse } from "@medusajs/medusa"

// 立即执行的日志，确认文件被加载
console.log('\n=== 文件加载：admin/product-categories/route.ts ===');

// 添加路由加载日志
console.log('\n=== 加载 admin/product-categories 路由 ===');

// 添加请求追踪函数
function logRequest(req: MedusaRequest, prefix: string = '') {
  console.log(`${prefix} 请求信息:`, {
    url: req.url,
    path: req.path,
    method: req.method,
    query: req.query,
    body: req.body,
    headers: {
      'content-type': req.headers['content-type'],
      'accept': req.headers.accept
    }
  });
}

// 添加一个调试函数
function debugRequest(req: MedusaRequest) {
  const fullUrl = new URL(req.url || '', 'http://localhost:9000');
  console.log('\n=== 请求调试信息 ===');
  console.log('完整URL:', fullUrl.toString());
  console.log('路径:', fullUrl.pathname);
  console.log('方法:', req.method);
  console.log('查询参数:', Object.fromEntries(fullUrl.searchParams));
  console.log('请求头:', req.headers);
  console.log('请求体:', req.body);
  console.log('========================\n');
}

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
): Promise<void> {
  try {
    // 从 URL 中获取分类 ID
    const url = new URL(req.url || '', 'http://localhost:9000');
    const pathParts = url.pathname.split('/');
    const categoryId = pathParts[pathParts.length - 1] === 'edit' 
      ? pathParts[pathParts.length - 2] 
      : pathParts[pathParts.length - 1];

    console.log('\n=== 获取单个分类 ===');
    console.log('请求URL:', url.toString());
    console.log('分类ID:', categoryId);
    console.log('路径部分:', pathParts);

    // 检查是否是列表请求
    const isListRequest = !categoryId || categoryId === 'product-categories';
    if (isListRequest) {
      // 如果是列表请求，调用原来的列表处理逻辑
      return await handleCategoryList(req, res);
    }

    if (!categoryId || isNaN(Number(categoryId))) {
      return res.status(400).json({
        message: 'Invalid category ID',
        type: 'invalid_data',
        code: 'invalid_id',
        __isMedusaError: true,
        date: new Date()
      });
    }

    // 发送获取请求到后端
    const response = await fetch(`http://localhost:8080/api/categories/${categoryId}`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });

    console.log('获取请求状态:', response.status);

    if (response.status === 404) {
      return res.status(404).json({
        message: `Product category with id: ${categoryId} was not found`,
        type: 'not_found',
        __isMedusaError: true,
        code: undefined,
        date: new Date()
      });
    }

    if (!response.ok) {
      const errorText = await response.text();
      return res.status(response.status).json({
        message: errorText || 'Failed to get category',
        type: 'error',
        __isMedusaError: true,
        code: 'unexpected_error',
        date: new Date()
      });
    }

    // 获取分类数据
    const data = await response.json();
    console.log('后端返回的数据:', data);

    // 转换为 Medusa Admin UI 期望的格式
    const formattedCategory = {
      id: data.id,
      name: data.name,
      handle: data.handle || data.name.toLowerCase().replace(/\s+/g, '-'),
      description: data.description || '',
      is_internal: data.isInternal || false,
      is_active: data.isActive || true,
      rank: data.rank || 0,
      parent_category_id: data.parentCategoryId,
      parent_category: data.parentCategory ? {
        id: data.parentCategory.id,
        name: data.parentCategory.name,
        handle: data.parentCategory.handle || data.parentCategory.name.toLowerCase().replace(/\s+/g, '-'),
        description: data.parentCategory.description || '',
        is_internal: data.parentCategory.isInternal || false,
        is_active: data.parentCategory.isActive || true,
        rank: data.parentCategory.rank || 0,
        created_at: data.parentCategory.createdAt,
        updated_at: data.parentCategory.updatedAt
      } : null,
      category_children: Array.isArray(data.children) 
        ? data.children.map(child => ({
            id: child.id,
            name: child.name,
            handle: child.handle || child.name.toLowerCase().replace(/\s+/g, '-'),
            description: child.description || '',
            is_internal: child.isInternal || false,
            is_active: child.isActive || true,
            rank: child.rank || 0,
            created_at: child.createdAt,
            updated_at: child.updatedAt
          }))
        : [],
      created_at: data.createdAt,
      updated_at: data.updatedAt
    };

    console.log('发送到前端的数据:', formattedCategory);

    // 返回分类数据
    return res.json({
      product_category: formattedCategory
    });

  } catch (error: any) {
    console.error('获取分类时发生错误:', error);
    return res.status(500).json({
      message: error.message || '获取分类失败',
      type: 'server_error',
      code: 'unexpected_error',
      __isMedusaError: true,
      date: new Date()
    });
  }
}

// 将原来的 GET 方法重命名为 handleCategoryList
async function handleCategoryList(req: MedusaRequest, res: MedusaResponse): Promise<void> {
  console.log('admin/product-categories: 进入 GET 方法');
  try {
    console.log('完整请求信息:', {
      url: req.url,
      path: req.path,
      method: req.method,
      query: req.query,
      headers: req.headers
    });

    // 构建查询参数
    const queryParams = new URLSearchParams();
    
    // 添加分页参数
    queryParams.append('page', String(Math.max(0, Number(req.query.offset || 0) / Number(req.query.limit || 20))));
    queryParams.append('size', String(req.query.limit || 20));

    // 添加搜索参数
    if (req.query.q) {
      queryParams.append('name', String(req.query.q));
    }

    // 添加父分类参数
    if (req.query.parent_category_id && req.query.parent_category_id !== 'null') {
      queryParams.append('parentId', String(req.query.parent_category_id));
    }

    const url = `http://localhost:8080/api/categories?${queryParams}`;
    console.log('请求分类接口URL:', url);

    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error('后端返回错误:', errorText);
      throw new Error(`获取分类列表失败: ${errorText}`);
    }

    const data = await response.json();
    console.log('后端返回的原始数据:', data);

    // 确保数据存在且格式正确
    if (!data || !Array.isArray(data.product_categories)) {
      console.error('后端返回的数据格式不正确:', data);
      throw new Error('后端返回的数据格式不正确');
    }

    // 递归转换分类数据的函数
    const formatCategory = (cat: any) => ({
      id: cat.id,
      name: cat.name,
      handle: cat.handle,
      description: cat.description || '',
      is_internal: cat.is_internal,
      is_active: cat.is_active,
      rank: cat.rank || 0,
      parent_category_id: cat.parent_category_id,
      parent_category: cat.parent_category ? formatCategory(cat.parent_category) : null,
      category_children: Array.isArray(cat.category_children) 
        ? cat.category_children.map(formatCategory)
        : [],
      created_at: cat.created_at,
      updated_at: cat.updated_at,
      deleted_at: cat.deleted_at
    });

    // 转换为 Medusa Admin UI 期望的格式
    const formattedCategories = data.product_categories.map(formatCategory);

    // 构造符合官方文档的响应格式
    const responseData = {
      product_categories: formattedCategories,
      count: data.count,
      offset: data.offset || Number(req.query.offset || 0),
      limit: data.limit || Number(req.query.limit || 20)
    };

    console.log('发送到前端的响应数据:', JSON.stringify(responseData, null, 2));
    return res.json(responseData);

  } catch (error) {
    console.error('获取分类列表失败:', error);
    return res.status(500).json({
      message: error.message || '获取分类列表失败',
      type: "server_error",
      code: "unexpected_error"
    });
  }
}

export async function POST(
  req: MedusaRequest,
  res: MedusaResponse
): Promise<void> {
  try {
    // 从 URL 中获取分类 ID
    const url = new URL(req.url || '', 'http://localhost:9000');
    const categoryId = url.pathname.split('/').pop();

    console.log('\n=== 开始更新分类 ===');
    console.log('请求URL:', url.toString());
    console.log('分类ID:', categoryId);
    console.log('更新数据:', req.body);

    if (!categoryId || isNaN(Number(categoryId))) {
      return res.status(400).json({
        message: 'Invalid category ID',
        type: 'invalid_data',
        code: 'invalid_id'
      });
    }

    // 构造更新数据
    const updateData = {
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

    // 发送更新请求到后端
    const response = await fetch(`http://localhost:8080/api/categories/${categoryId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify(updateData)
    });

    console.log('更新请求状态:', response.status);

    if (response.status === 404) {
      return res.status(404).json({
        message: `ProductCategory with id: ${categoryId} was not found`,
        type: 'not_found'
      });
    }

    if (!response.ok) {
      const errorText = await response.text();
      return res.status(response.status).json({
        message: errorText || 'Failed to update category',
        type: 'error'
      });
    }

    // 获取更新后的数据
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
      category_children: Array.isArray(data.children) 
        ? data.children.map(child => ({
            id: child.id,
            name: child.name,
            handle: child.handle,
            description: child.description || '',
            is_internal: child.isInternal,
            is_active: child.isActive,
            rank: child.rank || 0,
            created_at: child.createdAt,
            updated_at: child.updatedAt
          }))
        : [],
      created_at: data.createdAt,
      updated_at: data.updatedAt
    };

    // 返回更新后的分类数据
    return res.json({
      product_category: formattedCategory
    });

  } catch (error: any) {
    console.error('更新分类时发生错误:', error);
    return res.status(500).json({
      message: error.message || '更新分类失败',
      type: 'server_error',
      code: 'unexpected_error'
    });
  }
}

export async function DELETE(
  req: MedusaRequest,
  res: MedusaResponse
): Promise<void> {
  try {
    // 从 URL 中获取分类 ID
    const url = new URL(req.url || '', 'http://localhost:9000');
    const categoryId = url.pathname.split('/').pop();

    console.log('\n=== 开始删除分类 ===');
    console.log('请求URL:', url.toString());
    console.log('分类ID:', categoryId);

    // 检查是否是内部调用
    const isInternalCall = req.headers['x-medusa-access'] === 'internal';
    if (isInternalCall) {
      console.log('跳过内部调用');
      return res.status(200).json({
        id: categoryId,
        object: "product_category",
        deleted: true
      });
    }

    if (!categoryId || isNaN(Number(categoryId))) {
      return res.status(400).json({
        message: 'Invalid category ID',
        name: 'Error',
        __isMedusaError: true,
        type: 'invalid_data',
        code: 'invalid_id',
        date: new Date()
      });
    }

    // 发送删除请求到后端
    const response = await fetch(`http://localhost:8080/api/categories/${categoryId}`, {
      method: 'DELETE',
      headers: {
        'Accept': 'application/json',
        'x-medusa-access': 'external'
      }
    });

    console.log('删除请求状态:', response.status);

    // 处理不同的响应状态
    if (response.status === 404) {
      return res.status(404).json({
        message: `ProductCategory with id: ${categoryId} was not found`,
        name: 'Error',
        __isMedusaError: true,
        type: 'not_found',
        code: undefined,
        date: new Date()
      });
    }

    if (response.status === 400) {
      return res.status(400).json({
        message: 'Cannot delete category with children',
        name: 'Error',
        __isMedusaError: true,
        type: 'invalid_data',
        code: 'category_has_children',
        date: new Date()
      });
    }

    if (!response.ok) {
      const errorText = await response.text();
      return res.status(500).json({
        message: errorText || 'Failed to delete category',
        name: 'Error',
        __isMedusaError: true,
        type: 'unknown_error',
        code: undefined,
        date: new Date()
      });
    }

    // 删除成功
    return res.status(200).json({
      id: categoryId,
      object: "product_category",
      deleted: true
    });

  } catch (error: any) {
    console.error('删除分类时发生错误:', error);
    return res.status(500).json({
      message: error.message || '删除分类失败',
      name: 'Error',
      __isMedusaError: true,
      type: 'server_error',
      code: 'unexpected_error',
      date: new Date()
    });
  }
} 