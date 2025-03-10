import { MedusaRequest, MedusaResponse } from "@medusajs/medusa"
import { CategoryCreateRequest, CategoryListResponse } from "@/types/api"

console.log('正在加载 admin/categories 路由');

export async function GET(
  req: MedusaRequest,
  res: MedusaResponse
): Promise<void> {
  console.log('进入 GET 方法');
  try {
    console.log('请求对象:', {
      method: req.method,
      url: req.url,
      query: req.query,
      headers: req.headers
    });

    const { name, page = 0, size = 20, parentId } = req.query

    console.log('\n=== 开始获取分类列表 ===');
    console.log('查询参数:', { name, page, size, parentId });

    const url = `http://localhost:8080/api/categories?${new URLSearchParams({
      name: name as string || '',
      page: String(page),
      size: String(size),
      ...(parentId ? { parentId: String(parentId) } : {})
    })}`;
    console.log('请求后端URL:', url);

    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    console.log('后端响应状态:', response.status);

    if (!response.ok) {
      const errorText = await response.text();
      console.error('后端返回错误:', errorText);
      throw new Error(`获取分类列表失败: ${errorText}`);
    }

    const data: CategoryListResponse = await response.json()
    console.log('获取到的分类数据:', data)

    res.json(data)
  } catch (error) {
    console.error('获取分类列表失败:', error)
    res.status(500).json({
      message: error.message || '获取分类列表失败'
    })
  }
}

export async function POST(
  req: MedusaRequest,
  res: MedusaResponse
): Promise<void> {
  try {
    console.log('\n=== 开始创建分类 ===')
    const categoryData = req.body as CategoryCreateRequest
    console.log('创建分类数据:', categoryData)

    const response = await fetch('http://localhost:8080/api/categories', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(categoryData)
    })

    if (!response.ok) {
      throw new Error('创建分类失败')
    }

    const category = await response.json()
    console.log('创建的分类:', category)

    res.json(category)
  } catch (error) {
    console.error('创建分类失败:', error)
    res.status(500).json({
      message: error.message || '创建分类失败'
    })
  }
} 