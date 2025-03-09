import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http";
import { GET as ADMIN_GET } from '../../../admin/collections/[id]/route';
import { GET as PRODUCTS_GET } from '../../../admin/collections/[id]/products/route';

export const GET = ADMIN_GET;
export const GET_PRODUCTS = PRODUCTS_GET; 