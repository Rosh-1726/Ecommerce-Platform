import { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import Layout from "../components/Layout";
import * as shop from "../api/shop";
import { apiErrorMessage } from "../api/client";
import { useToast } from "../context/ToastContext";

const STATUS_TONE = {
  CREATED: "tag-accent",
  PAID: "tag-success",
  DELIVERED: "tag-success",
  CANCELLED: "tag-error",
  FAILED: "tag-error",
  REFUNDED: "tag",
};

export default function OrderDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { notify } = useToast();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);

  function load() {
    setLoading(true);
    shop
      .getOrder(id)
      .then(({ data }) => setOrder(data))
      .catch((err) => notify(apiErrorMessage(err, "Couldn't load this order."), "error"))
      .finally(() => setLoading(false));
  }

  useEffect(load, [id]);

  async function handlePay() {
    setBusy(true);
    try {
      const { data } = await shop.createPaymentSession(order.id);
      if (data.url) {
        window.location.href = data.url;
      } else {
        notify("Stripe didn't return a checkout link.", "error");
      }
    } catch (err) {
      notify(apiErrorMessage(err, "Payment couldn't be started. Stripe keys may not be configured."), "error");
    } finally {
      setBusy(false);
    }
  }

  async function handleCancel() {
    setBusy(true);
    try {
      await shop.cancelOrder(order.id);
      notify("Order cancelled");
      load();
    } catch (err) {
      notify(apiErrorMessage(err, "This order can no longer be cancelled."), "error");
    } finally {
      setBusy(false);
    }
  }

  if (loading) {
    return <Layout><div className="shell"><p>Loading…</p></div></Layout>;
  }
  if (!order) {
    return (
      <Layout>
        <div className="shell">
          <div className="empty-state">
            <h3>Order not found</h3>
            <Link to="/orders" className="btn btn-outline">Back to orders</Link>
          </div>
        </div>
      </Layout>
    );
  }

  const canPay = order.status === "CREATED";
  const canCancel = order.status === "CREATED";

  return (
    <Layout>
      <div className="shell" style={{ maxWidth: 720 }}>
        <Link to="/orders" style={{ fontSize: 13, color: "var(--ink-soft)" }}>← Back to orders</Link>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", margin: "16px 0 28px" }}>
          <div>
            <h1 style={{ fontSize: 30 }}>Order #{order.id}</h1>
            <p style={{ marginTop: 6 }}>Placed {new Date(order.createdAt).toLocaleString()}</p>
          </div>
          <span className={`tag ${STATUS_TONE[order.status] || ""}`} style={{ fontSize: 13, padding: "6px 14px" }}>
            {order.status}
          </span>
        </div>

        <div className="panel" style={{ marginBottom: 24 }}>
          {order.items.map((item, idx) => (
            <div key={item.id} style={{ display: "flex", justifyContent: "space-between", padding: 16, borderBottom: idx < order.items.length - 1 ? "1px solid var(--line)" : "none" }}>
              <span>{item.productName} × {item.quantity}</span>
              <span className="price">${Number(item.priceAtPurchase * item.quantity).toFixed(2)}</span>
            </div>
          ))}
          <div style={{ display: "flex", justifyContent: "space-between", padding: 16, background: "var(--paper)" }}>
            <strong>Total</strong>
            <strong className="price">${Number(order.totalAmount).toFixed(2)}</strong>
          </div>
        </div>

        <div className="panel" style={{ padding: 18, marginBottom: 24 }}>
          <p style={{ fontSize: 13, fontWeight: 700, marginBottom: 6 }}>Shipping to</p>
          <p>{order.shippingAddress}</p>
        </div>

        <div style={{ display: "flex", gap: 12 }}>
          {canPay && (
            <button className="btn btn-accent" disabled={busy} onClick={handlePay}>
              {busy ? <span className="spinner" /> : "Pay now"}
            </button>
          )}
          {canCancel && (
            <button className="btn btn-danger" disabled={busy} onClick={handleCancel}>
              Cancel order
            </button>
          )}
        </div>
      </div>
    </Layout>
  );
}
