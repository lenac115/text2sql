export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  email: string;
  name: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  name: string;
}

export interface AddressDto {
  recipient: string;
  phone: string;
  zipCode: string;
  addressLine1: string;
  addressLine2?: string;
}

export interface UserProfile {
  id: number;
  email: string;
  name: string;
  role: 'USER' | 'ADMIN';
  defaultAddress: AddressDto | null;
  createdAt: string;
}

export interface Category {
  id: number;
  name: string;
  productCount: number;
}

export interface Product {
  id: number;
  name: string;
  description: string | null;
  imageUrl: string | null;
  price: number;
  stock: number;
  categoryId: number | null;
  categoryName: string | null;
  createdAt: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface CartItem {
  cartItemId: number;
  productId: number;
  productName: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
}

export interface Cart {
  items: CartItem[];
  totalAmount: number;
}

export type OrderStatus =
  | 'PAYMENT_PENDING'
  | 'PAID'
  | 'SHIPPING'
  | 'DELIVERED'
  | 'CANCELLED'
  | 'REFUNDED';

export interface OrderItem {
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface Order {
  orderId: number;
  totalAmount: number;
  discountAmount: number;
  finalAmount: number;
  status: OrderStatus;
  orderedAt: string;
  shippingAddress: AddressDto | null;
  items: OrderItem[];
}

export interface OrderCreateRequest {
  items: { productId: number; quantity: number }[];
  userCouponId?: number | null;
  shippingAddress?: AddressDto | null;
}

export type PaymentMethod = 'CARD' | 'KAKAO_PAY' | 'NAVER_PAY' | 'BANK_TRANSFER';

export type PaymentStatus = 'PENDING' | 'SUCCEEDED' | 'FAILED' | 'CANCELLED';

export interface Payment {
  paymentId: number;
  orderId: number;
  method: PaymentMethod;
  status: PaymentStatus;
  amount: number;
  pgTransactionId: string | null;
  failureReason: string | null;
  requestedAt: string;
  confirmedAt: string | null;
}

export interface UserCoupon {
  id: number;
  couponName: string;
  code: string;
  discountType: 'FIXED' | 'PERCENTAGE';
  discountValue: number;
  minOrderAmount: number;
  maxDiscountAmount: number;
  used: boolean;
  expiresAt: string;
  issuedAt: string;
}

export type DealStatus = 'UPCOMING' | 'ACTIVE' | 'SOLD_OUT' | 'ENDED';

export interface TimeDeal {
  id: number;
  title: string;
  productId: number;
  productName: string;
  originalPrice: number;
  dealPrice: number;
  discountRate: number;
  totalStock: number;
  remainingStock: number;
  maxPerUser: number;
  status: DealStatus;
  startAt: string;
  endAt: string;
}

export interface OrderStatusEvent {
  orderId: number;
  previousStatus: OrderStatus;
  currentStatus: OrderStatus;
  changedAt: string;
}

export interface QueryRequest {
  question: string;
}

export interface QueryMetaData {
  executionTimeMs: number;
  rowCount: number;
  cached: boolean;
}

export interface QueryResultData {
  summary: string;
  generatedSql: string;
  columns: string[];
  rows: Record<string, unknown>[];
  metadata: QueryMetaData;
}

export interface QueryError {
  code: string;
  message: string;
  detail: Record<string, unknown> | null;
}

export interface QueryResponse {
  success: boolean;
  data: QueryResultData | null;
  error: QueryError | null;
}