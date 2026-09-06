import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Layout from "../components/Layout";
import * as shop from "../api/shop";
import { apiErrorMessage } from "../api/client";
import { useCart } from "../context/CartContext";
import { useToast } from "../context/ToastContext";

const FALLBACK_IMG =
  "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='300' height='300'><rect width='300' height='300' fill='%23EFE9DD'/></svg>";

export default function Wishlist() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const { refreshCart } = useCart();
  const { notify } = useToast();

  function load() {
    setLoading(true);
    shop
      .getWishlist()
      .then(({ data }) => setItems(data.products || []))
      .catch(() => setItems([]))
      .finally(() => setLoading(false));
  }

  useEffect(load, []);

  async function remove(productId) {
    try {
      await shop.removeFromWishlist(productId);
      setItems((prev) => prev.filter((p) => p.id !== productId));
    } catch (err) {
      notify(apiErrorMessage(err), "error");
    }
  }

  async function moveToCart(productId) {
    try {
      await shop.addToCart(productId, 1);
      await refreshCart();
      notify("Added to your bag");
    } catch (err) {
      notify(apiErrorMessage(err), "error");
    }
  }

  return (
    <Layout>
      <div className="shell">
        <h1 style={{ fontSize: 32, marginBottom: 28 }}>Saved for later</h1>

        {loading ? (
          <p>Loading…</p>
        ) : items.length === 0 ? (
          <div className="empty-state">
            <h3>Nothing saved yet</h3>
            <p>Tap "Save for later" on anything you're not ready to buy.</p>
            <Link to="/products" className="btn btn-accent">Browse the floor</Link>
          </div>
        ) : (
          <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 20 }}>
            {items.map((p) => (
              <div key={p.id} className="panel" style={{ overflow: "hidden" }}>
                <Link to={`/products/${p.id}`} style={{ aspectRatio: "1 / 1", background: "var(--accent-tint)", display: "block", overflow: "hidden" }}>
                  <img src={p.imageUrl || FALLBACK_IMG} alt={p.name} style={{ width: "100%", height: "100%", objectFit: "cover" }} onError={(e) => { e.currentTarget.src = FALLBACK_IMG; }} />
                </Link>
                <div style={{ padding: 14 }}>
                  <h3 style={{ fontSize: 15, marginBottom: 6 }}>{p.name}</h3>
                  <p className="price" style={{ marginBottom: 12 }}>${Number(p.price).toFixed(2)}</p>
                  <div style={{ display: "flex", gap: 8 }}>
                    <button className="btn btn-accent btn-sm" style={{ flex: 1 }} onClick={() => moveToCart(p.id)}>Add to bag</button>
                    <button className="btn btn-ghost btn-sm" onClick={() => remove(p.id)}>Remove</button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </Layout>
  );
}
