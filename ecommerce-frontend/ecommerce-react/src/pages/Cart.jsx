import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Layout from "../components/Layout";
import * as shop from "../api/shop";
import { apiErrorMessage } from "../api/client";
import { useCart } from "../context/CartContext";
import { useToast } from "../context/ToastContext";

const FALLBACK_IMG =
  "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='120' height='120'><rect width='120' height='120' fill='%23EFE9DD'/></svg>";

export default function Cart() {
  const { cart, refreshCart } = useCart();
  const { notify } = useToast();
  const navigate = useNavigate();
  const [busyId, setBusyId] = useState(null);

  const items = cart?.items || [];
  const total = items.reduce((sum, i) => sum + i.price * i.quantity, 0);

  async function changeQty(productId, quantity) {
    if (quantity < 1) return;
    setBusyId(productId);
    try {
      await shop.updateCartQuantity(productId, quantity);
      await refreshCart();
    } catch (err) {
      notify(apiErrorMessage(err), "error");
    } finally {
      setBusyId(null);
    }
  }

  async function removeItem(productId) {
    setBusyId(productId);
    try {
      await shop.removeFromCart(productId);
      await refreshCart();
      notify("Removed from your bag");
    } catch (err) {
      notify(apiErrorMessage(err), "error");
    } finally {
      setBusyId(null);
    }
  }

  return (
    <Layout>
      <div className="shell" style={{ maxWidth: 880 }}>
        <h1 style={{ fontSize: 32, marginBottom: 28 }}>Your bag</h1>

        {items.length === 0 ? (
          <div className="empty-state">
            <h3>Your bag is empty</h3>
            <p>Whatever catches your eye on the floor will land here.</p>
            <Link to="/products" className="btn btn-accent">Browse the floor</Link>
          </div>
        ) : (
          <>
            <div className="panel" style={{ marginBottom: 24 }}>
              {items.map((item, idx) => (
                <div
                  key={item.productId}
                  style={{
                    display: "flex",
                    alignItems: "center",
                    gap: 16,
                    padding: 18,
                    borderBottom: idx < items.length - 1 ? "1px solid var(--line)" : "none",
                  }}
                >
                  <img
                    src={item.imageUrl || FALLBACK_IMG}
                    alt={item.productName}
                    style={{ width: 76, height: 76, objectFit: "cover", borderRadius: "var(--radius)", background: "var(--accent-tint)" }}
                    onError={(e) => { e.currentTarget.src = FALLBACK_IMG; }}
                  />
                  <div style={{ flex: 1 }}>
                    <h3 style={{ fontSize: 16 }}>{item.productName}</h3>
                    <p style={{ fontSize: 13, marginTop: 4 }}>${Number(item.price).toFixed(2)} each</p>
                  </div>
                  <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                    <button className="btn btn-outline btn-sm" disabled={busyId === item.productId} onClick={() => changeQty(item.productId, item.quantity - 1)}>−</button>
                    <span style={{ minWidth: 24, textAlign: "center", fontWeight: 600 }}>{item.quantity}</span>
                    <button className="btn btn-outline btn-sm" disabled={busyId === item.productId} onClick={() => changeQty(item.productId, item.quantity + 1)}>+</button>
                  </div>
                  <span className="price" style={{ width: 80, textAlign: "right" }}>
                    ${Number(item.price * item.quantity).toFixed(2)}
                  </span>
                  <button className="btn btn-ghost btn-sm" style={{ color: "var(--error)" }} disabled={busyId === item.productId} onClick={() => removeItem(item.productId)}>
                    Remove
                  </button>
                </div>
              ))}
            </div>

            <div className="panel" style={{ padding: 20, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
              <div>
                <p style={{ fontSize: 13 }}>Subtotal</p>
                <p className="price" style={{ fontSize: 24 }}>${total.toFixed(2)}</p>
              </div>
              <button className="btn btn-accent" onClick={() => navigate("/checkout")}>
                Continue to checkout
              </button>
            </div>
          </>
        )}
      </div>
    </Layout>
  );
}
