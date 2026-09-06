import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import Layout from "../components/Layout";
import ProductCard from "../components/ProductCard";
import * as shop from "../api/shop";

export default function Products() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [params, setParams] = useSearchParams();
  const activeCategory = params.get("category") || "";
  const [query, setQuery] = useState("");

  useEffect(() => {
    Promise.allSettled([shop.getProducts(), shop.getCategories()]).then(([p, c]) => {
      if (p.status === "fulfilled") setProducts(p.value.data);
      if (c.status === "fulfilled") setCategories(c.value.data);
      setLoading(false);
    });
  }, []);

  const filtered = useMemo(() => {
    return products.filter((p) => {
      const matchesCategory = !activeCategory || String(p.categoryId) === activeCategory;
      const matchesQuery = !query || p.name.toLowerCase().includes(query.toLowerCase());
      return matchesCategory && matchesQuery;
    });
  }, [products, activeCategory, query]);

  function setCategory(id) {
    if (!id) {
      params.delete("category");
    } else {
      params.set("category", id);
    }
    setParams(params);
  }

  return (
    <Layout>
      <div className="shell">
        <div style={{ marginBottom: 32 }}>
          <h1 style={{ fontSize: 36, marginBottom: 8 }}>The floor</h1>
          <p>Everything currently in stock, straight from the sellers who made it.</p>
        </div>

        <div style={{ display: "flex", gap: 24, alignItems: "flex-start" }}>
          <aside style={{ width: 220, flexShrink: 0 }}>
            <div className="field">
              <label htmlFor="search">Search</label>
              <input id="search" placeholder="Find something…" value={query} onChange={(e) => setQuery(e.target.value)} />
            </div>
            <p style={{ fontSize: 13, fontWeight: 700, color: "var(--ink-soft)", marginBottom: 10, marginTop: 20 }}>
              Categories
            </p>
            <div style={{ display: "flex", flexDirection: "column", gap: 4 }}>
              <button
                onClick={() => setCategory("")}
                className="btn-ghost"
                style={{ textAlign: "left", padding: "6px 0", fontWeight: !activeCategory ? 700 : 500, color: !activeCategory ? "var(--ink)" : "var(--ink-soft)" }}
              >
                All items
              </button>
              {categories.map((c) => (
                <button
                  key={c.id}
                  onClick={() => setCategory(String(c.id))}
                  className="btn-ghost"
                  style={{
                    textAlign: "left",
                    padding: "6px 0",
                    fontWeight: activeCategory === String(c.id) ? 700 : 500,
                    color: activeCategory === String(c.id) ? "var(--ink)" : "var(--ink-soft)",
                  }}
                >
                  {c.name}
                </button>
              ))}
            </div>
          </aside>

          <div style={{ flex: 1 }}>
            {loading ? (
              <p>Loading…</p>
            ) : filtered.length === 0 ? (
              <div className="empty-state">
                <h3>Nothing matches yet</h3>
                <p>Try a different category or search term.</p>
              </div>
            ) : (
              <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 20 }}>
                {filtered.map((p) => (
                  <ProductCard key={p.id} product={p} />
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </Layout>
  );
}
