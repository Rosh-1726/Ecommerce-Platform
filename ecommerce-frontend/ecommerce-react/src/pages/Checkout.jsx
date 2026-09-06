import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Layout from "../components/Layout";
import * as shop from "../api/shop";
import { apiErrorMessage } from "../api/client";
import { useCart } from "../context/CartContext";
import { useToast } from "../context/ToastContext";

export default function Checkout() {
  const { cart, refreshCart } = useCart();
  const { notify } = useToast();
  const navigate = useNavigate();
  const [address, setAddress] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  const items = cart?.items || [];
  const total = items.reduce((sum, i) => sum + i.price * i.quantity, 0);

  async function handlePlaceOrder(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      const { data } = await shop.createOrderFromCart(address);
      await refreshCart();
      notify("Order placed — head to your orders to pay");
      navigate(`/orders/${data.id}`);
    } catch (err) {
      setError(apiErrorMessage(err, "Couldn't place the order."));
    } finally {
      setSubmitting(false);
    }
  }

  if (items.length === 0) {
    return (
      <Layout>
        <div className="shell">
          <div className="empty-state">
            <h3>Your bag is empty</h3>
            <p>Add something to the bag before checking out.</p>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="shell" style={{ maxWidth: 720 }}>
        <h1 style={{ fontSize: 32, marginBottom: 28 }}>Checkout</h1>

        <div className="panel" style={{ padding: 20, marginBottom: 24 }}>
          {items.map((item) => (
            <div key={item.productId} style={{ display: "flex", justifyContent: "space-between", padding: "8px 0" }}>
              <span>{item.productName} × {item.quantity}</span>
              <span className="price">${Number(item.price * item.quantity).toFixed(2)}</span>
            </div>
          ))}
          <div className="hairline-top" style={{ display: "flex", justifyContent: "space-between", paddingTop: 14, marginTop: 8 }}>
            <strong>Total</strong>
            <strong className="price">${total.toFixed(2)}</strong>
          </div>
        </div>

        {error && <div className="banner banner-error">{error}</div>}

        <form onSubmit={handlePlaceOrder} className="panel" style={{ padding: 24 }}>
          <div className="field">
            <label htmlFor="address">Shipping address</label>
            <textarea id="address" required rows={3} value={address} onChange={(e) => setAddress(e.target.value)} placeholder="Street, city, state, ZIP" />
          </div>
          <button className="btn btn-accent btn-block" type="submit" disabled={submitting}>
            {submitting ? <span className="spinner" /> : "Place order"}
          </button>
          <p className="field-hint" style={{ marginTop: 12, textAlign: "center" }}>
            You'll be able to pay from your order once it's placed.
          </p>
        </form>
      </div>
    </Layout>
  );
}
