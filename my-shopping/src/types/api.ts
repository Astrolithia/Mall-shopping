export interface CollectionCreateRequest {
  title: string;
  handle?: string;
  description?: string;
  metadata?: Record<string, any>;
}

export interface ProductCreateRequest {
  title: string;
  subtitle?: string;
  description?: string;
  handle?: string;
  status?: string;
  thumbnail?: string;
  images?: Array<{ url: string }>;
  weight?: number;
  length?: number;
  height?: number;
  width?: number;
  origin_country?: string;
  material?: string;
  is_giftcard?: boolean;
  discountable?: boolean;
  options?: any[];
  variants?: any[];
  metadata?: Record<string, any>;
}

export interface UpdateRequest {
  title?: string;
  handle?: string;
  description?: string;
  status?: string;
  metadata?: Record<string, any>;
}

export type Category = {
  id: number
  name: string
  handle: string
  description: string | null
  is_internal: boolean
  is_active: boolean
  rank: number
  parent_category_id: number | null
  created_at: string
  updated_at: string
  metadata: Record<string, any>
  children?: Category[]
}

export type CategoryCreateRequest = {
  name: string
  handle?: string
  description?: string
  is_internal?: boolean
  is_active?: boolean
  rank?: number
  parent_category_id?: number
  metadata?: Record<string, any>
}

export type CategoryListResponse = {
  categories: Category[]
  count: number
  offset: number
  limit: number
}

export interface ReservationRequest {
  line_item_id: string
  inventory_item_id: string
  location_id: string
  quantity: number
  description?: string
  external_id?: string
  metadata?: Record<string, any>
}

export interface ReservationResponse {
  id: string
  line_item_id: string
  location_id: string
  quantity: number
  external_id?: string
  description?: string
  inventory_item_id: string
} 