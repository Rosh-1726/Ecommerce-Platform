import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Layout from "../components/Layout";
import ProductCard from "../components/ProductCard";
import * as shop from "../api/shop";

export default function Home() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.allSettled([shop.getProducts(), shop.getCategories()]).then(
      ([p, c]) => {
        if (p.status === "fulfilled") setProducts(p.value.data);
        if (c.status === "fulfilled") setCategories(c.value.data);
        setLoading(false);
      }
    );
  }, []);

  const featured = products.slice(0, 8);

  return (
    <Layout>
      <section className="shell" style={{ display: "grid", gridTemplateColumns: "1.1fr 0.9fr", gap: 48, alignItems: "center", marginBottom: 72 }}>
        <div>
          <span className="tag tag-accent" style={{ marginBottom: 18 }}>A marketplace, not a warehouse</span>
          <h1 style={{ fontSize: 52, marginTop: 18, marginBottom: 20, maxWidth: "16ch" }}>
            Goods made by people who still sign their own name to them.
          </h1>
          <p style={{ fontSize: 17, maxWidth: "48ch", marginBottom: 28 }}>
            Every stall here is run by an independent seller — vetted, approved,
            and accountable for what they make. Browse the floor, save what
            catches your eye, and check out when you're ready.
          </p>
          <div style={{ display: "flex", gap: 14 }}>
            <Link to="/products" className="btn btn-accent">Browse the floor</Link>
            <Link to="/sell" className="btn btn-outline">Open a stall</Link>
          </div>
        </div>
        <div
          className="panel"
          style={{
            aspectRatio: "4 / 5",
            background: "linear-gradient(155deg, var(--accent-tint), var(--paper))",
            display: "flex",
            alignItems: "flex-end",
            padding: 28,
          }}
        >
          <div>
            <p style={{ fontSize: 13, color: "var(--accent-deep)", fontWeight: 700, marginBottom: 6 }}>
              {categories.length} categories open today
            </p>
            <h3 style={{ fontSize: 24 }}>{products.length} items on the floor right now</h3>
          </div>
        </div>
      </section>

      {categories.length > 0 && (
        <section className="shell" style={{ marginBottom: 56 }}>
          <div style={{ display: "flex", gap: 12, flexWrap: "wrap" }}>
            {categories.map((c) => (
              <Link key={c.id} to={`/products?category=${c.id}`} className="tag" style={{ padding: "8px 16px", fontSize: 13 }}>
                {c.name}
              </Link>
            ))}
          </div>
        </section>
      )}

      <section className="shell">
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "baseline", marginBottom: 24 }}>
          <h2 style={{ fontSize: 26 }}>Fresh on the floor</h2>
          <Link to="/products" style={{ fontSize: 14, fontWeight: 600, color: "var(--accent-deep)" }}>
            See everything
          </Link>
        </div>

        {loading ? (
          <p>Loading the floor…</p>
        ) : featured.length === 0 ? (
          <div className="empty-state">
            <h3>No stalls have stocked anything yet</h3>
            <p>Once a seller lists a product, it'll show up here.</p>
          </div>
        ) : (
          <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 20 }}>
            {featured.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        )}
      </section>
    </Layout>
  );
}
