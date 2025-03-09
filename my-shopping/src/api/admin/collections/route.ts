import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http";
import { CollectionCreateRequest } from "@/types/api";

export async function GET(
  req: MedusaRequest, 
  res: MedusaResponse
) {
  try {
    console.log('=== 开始获取系列列表 ===');
    // 获取查询参数，注意 offset 和 limit 的处理
    const { offset = 0, limit = 20, fields } = req.query;
    const page = Math.floor(Number(offset) / Number(limit));
    const size = Number(limit);

    // 添加随机参数以避免缓存
    const timestamp = Date.now();
    const response = await fetch(
      `http://localhost:8080/api/collections?page=${page}&size=${size}`,
      {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      }
    );

    if (!response.ok) {
      throw new Error(`获取系列列表失败: ${response.status}`);
    }

    const data = await response.json();
    console.log('后端返回的系列列表数据:', data);

    // 转换数据格式以匹配 Medusa Admin UI 的期望
    const formattedCollections = data.collections.map(collection => ({
      id: collection.id,
      title: collection.title,
      handle: collection.handle || '',
      description: collection.description || '',
      created_at: collection.createdAt,
      updated_at: collection.updatedAt,
      deleted_at: null,
      metadata: collection.metadata || {},
      products: [] // 列表页面不需要返回产品数据
    }));

    const responseData = {
      collections: formattedCollections,
      count: data.count,
      offset: Number(offset),
      limit: Number(limit)
    };

    console.log('发送到前端的系列列表响应:', responseData);
    res.json(responseData);

  } catch (error) {
    console.error('获取系列列表失败:', error);
    res.status(500).json({
      message: "获取系列列表失败",
      error: error.message
    });
  }
}

export async function POST(
  req: MedusaRequest, 
  res: MedusaResponse
) {
  try {
    console.log('=== 开始处理创建系列请求 ===');
    console.log('请求体:', req.body);

    // 从请求体中获取数据
    const {
      title,
      handle,
      metadata = {}
    } = req.body as CollectionCreateRequest;

    // 构造发送到后端的数据
    const collectionRequest = {
      title,
      handle,
      metadata: metadata
    };

    console.log('发送到后端的数据:', collectionRequest);

    // 调用后端API创建系列
    const response = await fetch('http://localhost:8080/api/collections', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(collectionRequest)
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || '创建系列失败');
    }

    const createdCollection = await response.json();

    // 格式化返回数据
    const formattedCollection = {
      id: createdCollection.id,
      title: createdCollection.title,
      handle: createdCollection.handle || '',
      description: createdCollection.description || '',
      created_at: createdCollection.createdAt,
      updated_at: createdCollection.updatedAt,
      deleted_at: null,
      metadata: createdCollection.metadata || {},
      products: []
    };

    // 返回带有重定向信息的响应
    res.status(201).json({
      collection: formattedCollection,
      redirect: {
        path: `/app/collections/${formattedCollection.id}`,
        replace: true
      }
    });

  } catch (error) {
    console.error('创建系列失败:', error);
    res.status(500).json({
      message: "创建系列失败",
      error: error.message
    });
  }
} 