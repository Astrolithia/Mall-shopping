import { MedusaRequest, MedusaResponse } from "@medusajs/medusa"

console.log('正在加载 admin/product-categories 路由');

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