import client from "./client";

// ---- Categories ----
export const getCategories = () => client.get("/categories");
export const createCategory = (payload) => client.post("/categories", payload);

// ---- Products ----
export const getProducts = () => client.get("/products");
export const getProduct = (id) => client.get(`/products/${id}`);
export const createProduct = (fields, file) => {
  const form = new FormData();
  Object.entries(fields).forEach(([k, v]) => form.append(k, v));
  if (file) form.append("file", file);
  return client.post("/products", form, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

// ---- Cart ----
export const getCart = () => client.get("/cart");
export const addToCart = (productId, quantity) =>
  client.post("/cart/add", { productId, quantity });
export const updateCartQuantity = (productId, quantity) =>
  client.put("/cart/update", { productId, quantity });
export const removeFromCart = (productId) =>
  client.delete(`/cart/remove/${productId}`);
export const clearCart = () => client.delete("/cart/clear");

// ---- Wishlist ----
export const getWishlist = () => client.get("/wishlist");
export const addToWishlist = (productId) =>
  client.post(`/wishlist/add/${productId}`);
export const removeFromWishlist = (productId) =>
  client.delete(`/wishlist/remove/${productId}`);

// ---- Orders ----
export const createOrderFromCart = (shippingAddress) =>
  client.post("/orders", { shippingAddress });
export const createDirectOrder = (productId, quantity, shippingAddress) =>
  client.post("/orders/direct", { productId, quantity, shippingAddress });
// Backend returns a Spring Page<OrderSummaryResponse>; the list is in `content`.
export const listOrders = () => client.get("/orders");
export const getOrder = (id) => client.get(`/orders/${id}`);
export const cancelOrder = (id) => client.patch(`/orders/${id}/cancel`);

// ---- Payments (Stripe) ----
export const createPaymentSession = (orderId) =>
  client.post(`/payments/create/${orderId}`);
export const confirmPayment = (sessionId) =>
  client.get("/payments/confirm", { params: { session_id: sessionId } });

// ---- Seller ----
export const requestSeller = (storeName, reason, document) => {
  const form = new FormData();
  form.append("storeName", storeName);
  if (reason) form.append("reason", reason);
  form.append("document", document);
  return client.post("/seller/request", form, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};
export const listPendingSellerRequests = () =>
  client.get("/seller/seller-requests");
export const approveSellerRequest = (id) =>
  client.post(`/seller/approve/${id}`);
export const rejectSellerRequest = (id, reason) =>
  client.post(`/seller/reject/${id}`, null, { params: { reason } });
