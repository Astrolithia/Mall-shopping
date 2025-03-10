import { MedusaRequest, MedusaResponse } from "@medusajs/medusa"
import { CategoryCreateRequest, CategoryListResponse } from "@/types/api"

console.log('正在加载 app/categories 路由');

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
): Promise<void> {
  console.log('app/categories: 进入 GET 方法');
  try {
    console.log('完整请求信息:', {
      url: req.url,
      path: req.path,
      method: req.method,
      query: req.query,
      headers: req.headers
    });

    const url = `http://localhost:8080/api/categories?${new URLSearchParams({
      name: (req.query.name as string) || '',
      page: String(req.query.page || 0),
      size: String(req.query.size || 20),
      ...(req.query.parentId ? { parentId: String(req.query.parentId) } : {})
    })}`;

    console.log('请求分类接口URL:', url);

    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (!response.ok) {
      throw new Error('获取分类列表失败');
    }

    const data = await response.json();
    console.log('后端返回的分类数据:', data);

    const responseData = {
      categories: data.categories || [],
      count: data.count || 0,
      offset: data.offset || 0,
      limit: data.limit || 20
    };

    console.log('发送到前端的分类响应:', responseData);
    return res.json(responseData);

  } catch (error) {
    console.error('获取分类列表失败:', error)
    res.status(500).json({
      message: error.message || '获取分类列表失败'
    })
  }
}

// 仍然可以重用 admin 路由的 POST 方法
export { POST } from '../../admin/categories/route' 
