import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Layout from "../components/Layout";
import * as shop from "../api/shop";

const STATUS_TONE = {
  CREATED: "tag-accent",
  PAID: "tag-success",
  DELIVERED: "tag-success",
  CANCELLED: "tag-error",
  FAILED: "tag-error",
  REFUNDED: "tag",
};

export default function Orders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    shop
      .listOrders()
      .then(({ data }) => setOrders(data.content ?? data ?? []))
      .catch(() => setOrders([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <Layout>
      <div className="shell" style={{ maxWidth: 760 }}>
        <h1 style={{ fontSize: 32, marginBottom: 28 }}>Your orders</h1>

        {loading ? (
          <p>Loading…</p>
        ) : orders.length === 0 ? (
          <div className="empty-state">
            <h3>No orders yet</h3>
            <p>Once you check out, your orders will show up here.</p>
            <Link to="/products" className="btn btn-accent">Browse the floor</Link>
          </div>
        ) : (
          <div className="panel">
            {orders.map((o, idx) => (
              <Link
                key={o.id}
                to={`/orders/${o.id}`}
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  padding: 18,
                  borderBottom: idx < orders.length - 1 ? "1px solid var(--line)" : "none",
                }}
              >
                <div>
                  <p style={{ fontWeight: 600, fontSize: 15 }}>Order #{o.id}</p>
                  <p style={{ fontSize: 13, marginTop: 4 }}>{new Date(o.createdAt).toLocaleDateString()}</p>
                </div>
                <span className={`tag ${STATUS_TONE[o.status] || ""}`}>{o.status}</span>
                <span className="price">${Number(o.totalAmount).toFixed(2)}</span>
              </Link>
            ))}
          </div>
        )}
      </div>
    </Layout>
  );
}
