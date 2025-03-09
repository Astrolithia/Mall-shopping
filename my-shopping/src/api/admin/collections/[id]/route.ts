import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http";

export async function GET(
  req: MedusaRequest, 
  res: MedusaResponse
) {
  try {
    const id = req.params.id;
    console.log('正在获取系列详情，ID:', id);
    console.log('请求路径:', req.url);

    const response = await fetch(`http://localhost:8080/api/collections/${id}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (!response.ok) {
      throw new Error(`获取系列详情失败: ${response.status}`);
    }

    const collection = await response.json();
    console.log('后端返回的系列数据:', collection);

    // 格式化返回数据
    const formattedCollection = {
      id: collection.id,
      title: collection.title,
      handle: collection.handle || '',
      description: collection.description || '',
      created_at: collection.createdAt,
      updated_at: collection.updatedAt,
      deleted_at: null,
      metadata: collection.metadata || {},
      products: collection.products || []
    };

    console.log('发送到前端的响应:', { collection: formattedCollection });
    res.json({
      collection: formattedCollection
    });

  } catch (error) {
    console.error('获取系列详情失败:', error);
    res.status(404).json({
      type: "not_found",
      message: error.message,
      code: "collection.not_found"
    });
  }
} 