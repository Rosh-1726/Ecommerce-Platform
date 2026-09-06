import { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import Layout from "../components/Layout";
import * as shop from "../api/shop";
import { apiErrorMessage } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { useToast } from "../context/ToastContext";

const FALLBACK_IMG =
  "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='600' height='600'><rect width='600' height='600' fill='%23EFE9DD'/><text x='50%25' y='50%25' font-family='sans-serif' font-size='20' fill='%23A69F8C' text-anchor='middle'>No photo</text></svg>";

export default function ProductDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { refreshCart } = useCart();
  const { notify } = useToast();

  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [qty, setQty] = useState(1);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    setLoading(true);
    shop
      .getProduct(id)
      .then(({ data }) => setProduct(data))
      .catch((err) => setError(apiErrorMessage(err, "Couldn't find this product.")))
      .finally(() => setLoading(false));
  }, [id]);

  async function handleAddToCart() {
    if (!user) return navigate("/login", { state: { from: { pathname: `/products/${id}` } } });
    setBusy(true);
    try {
      await shop.addToCart(product.id, qty);
      await refreshCart();
      notify(`Added ${qty} × ${product.name} to your bag`);
    } catch (err) {
      notify(apiErrorMessage(err), "error");
    } finally {
      setBusy(false);
    }
  }

  async function handleWishlist() {
    if (!user) return navigate("/login", { state: { from: { pathname: `/products/${id}` } } });
    try {
      await shop.addToWishlist(product.id);
      notify("Saved to your wishlist");
    } catch (err) {
      notify(apiErrorMessage(err), "error");
    }
  }

  if (loading) {
    return (
      <Layout>
        <div className="shell"><p>Loading…</p></div>
      </Layout>
    );
  }

  if (error || !product) {
    return (
      <Layout>
        <div className="shell">
          <div className="empty-state">
            <h3>Product not found</h3>
            <p>{error}</p>
            <Link to="/products" className="btn btn-outline">Back to the floor</Link>
          </div>
        </div>
      </Layout>
    );
  }

  const outOfStock = product.stock <= 0 || product.active === false;

  return (
    <Layout>
      <div className="shell" style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 48 }}>
        <div className="panel" style={{ aspectRatio: "1 / 1", overflow: "hidden" }}>
          <img
            src={product.imageUrl || FALLBACK_IMG}
            alt={product.name}
            style={{ width: "100%", height: "100%", objectFit: "cover" }}
            onError={(e) => { e.currentTarget.src = FALLBACK_IMG; }}
          />
        </div>
        <div>
          {product.categoryName && <span className="tag" style={{ marginBottom: 14 }}>{product.categoryName}</span>}
          <h1 style={{ fontSize: 34, marginTop: 14, marginBottom: 12 }}>{product.name}</h1>
          <p className="price" style={{ fontSize: 26, marginBottom: 20 }}>${Number(product.price).toFixed(2)}</p>
          <p style={{ marginBottom: 28, lineHeight: 1.7 }}>{product.description}</p>

          {outOfStock ? (
            <div className="banner banner-error" style={{ maxWidth: 360 }}>This item is currently sold out.</div>
          ) : (
            <>
              <div style={{ display: "flex", alignItems: "center", gap: 14, marginBottom: 20 }}>
                <label htmlFor="qty" style={{ fontSize: 14, fontWeight: 600 }}>Quantity</label>
                <select id="qty" value={qty} onChange={(e) => setQty(Number(e.target.value))} style={{ width: 80, padding: "8px 10px", border: "1px solid var(--line-strong)", borderRadius: "var(--radius)" }}>
                  {Array.from({ length: Math.min(product.stock, 10) }, (_, i) => i + 1).map((n) => (
                    <option key={n} value={n}>{n}</option>
                  ))}
                </select>
                <span style={{ fontSize: 13, color: "var(--ink-soft)" }}>{product.stock} in stock</span>
              </div>
              <div style={{ display: "flex", gap: 12 }}>
                <button className="btn btn-accent" disabled={busy} onClick={handleAddToCart}>
                  {busy ? <span className="spinner" /> : "Add to bag"}
                </button>
                <button className="btn btn-outline" onClick={handleWishlist}>Save for later</button>
              </div>
            </>
          )}
        </div>
      </div>
    </Layout>
  );
}
