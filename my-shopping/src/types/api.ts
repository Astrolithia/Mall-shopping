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