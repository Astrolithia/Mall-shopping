// 导入并重新导出 admin 路由的处理函数
import { GET, PUT } from '../../../admin/products/[id]/route'
console.log('加载 /app/products/[id] 路由');
export { GET, PUT } 