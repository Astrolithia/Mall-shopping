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
  console.log('admin/product-categories: 进入 GET 方法');
  try {
    console.log('完整请求信息:', {
      url: req.url,
      path: req.path,
      method: req.method,
      query: req.query,
      headers: req.headers
    });

    // 移除 parentId=null 的情况
    const queryParams = new URLSearchParams({
      name: '',
      page: '0',
      size: '20'
    });

    // 只有当 parent_category_id 有值时才添加到查询参数中
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
    console.log('后端返回的分类数据:', data);

    // 递归转换分类数据的函数
    const formatCategory = (cat: any) => ({
      id: cat.id,
      name: cat.name,
      handle: cat.handle,
      description: cat.description || '',
      is_internal: cat.isInternal,
      is_active: cat.isActive,
      rank: cat.rank || 0,
      parent_category_id: cat.parent_category_id,
      parent_category: cat.parentCategory ? formatCategory(cat.parentCategory) : null,
      category_children: Array.isArray(cat.children) 
        ? cat.children.map(formatCategory)
        : [],
      created_at: cat.created_at,
      updated_at: cat.updated_at,
      deleted_at: null
    });

    // 转换为 Medusa Admin UI 期望的格式
    const formattedCategories = data.categories.map(formatCategory);

    // 构造符合官方文档的响应格式
    const responseData = {
      product_categories: formattedCategories,
      count: data.count,
      offset: data.offset,
      limit: data.limit
    };

    console.log('发送到前端的分类响应:', JSON.stringify(responseData, null, 2));
    return res.json(responseData);

  } catch (error) {
    console.error('获取分类列表失败:', error);
    res.status(500).json({
      message: error.message || '获取分类列表失败'
    });
  }
}

export async function POST(
  req: MedusaRequest,
  res: MedusaResponse
): Promise<void> {
  console.log('\n!!! POST 请求开始处理 !!!');
  
  try {
    // 记录详细的请求信息
    logRequest(req, '[POST]');
    console.log('原始请求体:', JSON.stringify(req.body, null, 2));

    // 检查请求体是否为空
    if (!req.body || Object.keys(req.body).length === 0) {
      console.error('请求体为空');
      return res.status(400).json({
        message: "请求体不能为空",
        type: "invalid_request",
        code: "empty_body"
      });
    }

    // 按照 Medusa 工作流的格式构造请求数据
    const workflowInput = {
      product_categories: [{
        name: req.body.name,
        handle: req.body.handle,
        description: req.body.description,
        is_internal: req.body.is_internal,
        is_active: req.body.is_active,
        rank: req.body.rank || 0,
        parent_category_id: req.body.parent_category?.id || 
                          req.body.parent_category_id ||
                          null
      }],
      additional_data: req.body.metadata || {}
    };

    console.log('工作流输入数据:', JSON.stringify(workflowInput, null, 2));

    // 发送请求到后端
    console.log('开始发送请求到后端...');
    const response = await fetch('http://localhost:8080/api/categories', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify({
        name: workflowInput.product_categories[0].name,
        handle: workflowInput.product_categories[0].handle,
        description: workflowInput.product_categories[0].description,
        isInternal: workflowInput.product_categories[0].is_internal,
        isActive: workflowInput.product_categories[0].is_active,
        rank: workflowInput.product_categories[0].rank,
        parentCategoryId: workflowInput.product_categories[0].parent_category_id 
          ? Number(workflowInput.product_categories[0].parent_category_id)
          : null,
        metadata: workflowInput.additional_data
      })
    });

    console.log('收到后端响应');
    console.log('响应状态:', response.status);
    
    const responseText = await response.text();
    console.log('原始响应内容:', responseText);

    if (!response.ok) {
      console.error('后端返回错误状态:', response.status);
      console.error('错误详情:', responseText);
      return res.status(response.status).json({
        message: `创建分类失败: ${responseText}`,
        type: "backend_error",
        code: `status_${response.status}`
      });
    }

    let data;
    try {
      data = JSON.parse(responseText);
      console.log('解析后的后端数据:', JSON.stringify(data, null, 2));
    } catch (parseError) {
      console.error('解析响应JSON失败:', parseError);
      return res.status(500).json({
        message: "解析后端响应失败",
        type: "parse_error",
        code: "invalid_json"
      });
    }

    // 转换为 Medusa Admin UI 期望的格式
    const formattedCategory = {
      id: data.id,
      name: data.name,
      handle: data.handle,
      description: data.description || '',
      is_internal: data.isInternal,
      is_active: data.isActive,
      rank: data.rank || 0,
      parent_category_id: data.parent_category_id,
      parent_category: data.parentCategory ? {
        id: data.parentCategory.id,
        name: data.parentCategory.name,
        handle: data.parentCategory.handle,
        description: data.parentCategory.description || '',
        is_internal: data.parentCategory.isInternal,
        is_active: data.parentCategory.isActive,
        rank: data.parentCategory.rank || 0,
        created_at: data.parentCategory.created_at,
        updated_at: data.parentCategory.updated_at,
        deleted_at: null
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
            created_at: child.created_at,
            updated_at: child.updated_at,
            deleted_at: null
          }))
        : [],
      created_at: data.created_at,
      updated_at: data.updated_at,
      deleted_at: null
    };

    // 构造符合官方文档的响应格式
    const responseData = {
      product_category: formattedCategory
    };

    console.log('发送到前端的响应:', JSON.stringify(responseData, null, 2));
    return res.status(201).json(responseData);

  } catch (error) {
    console.error('创建分类时发生错误:', error);
    console.error('错误堆栈:', error.stack);
    return res.status(500).json({
      message: error.message || '创建分类失败',
      type: "server_error",
      code: "unexpected_error",
      stack: process.env.NODE_ENV === 'development' ? error.stack : undefined
    });
  }
} 