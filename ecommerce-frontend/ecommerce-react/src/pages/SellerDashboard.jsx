import { useEffect, useState } from "react";
import Layout from "../components/Layout";
import * as shop from "../api/shop";
import { apiErrorMessage } from "../api/client";
import { useToast } from "../context/ToastContext";

export default function SellerDashboard() {
  const { notify } = useToast();
  const [categories, setCategories] = useState([]);
  const [fields, setFields] = useState({ name: "", description: "", price: "", categoryId: "", stock: "" });
  const [file, setFile] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    shop.getCategories().then(({ data }) => setCategories(data)).catch(() => setCategories([]));
  }, []);

  function update(key, value) {
    setFields((f) => ({ ...f, [key]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      await shop.createProduct(fields, file);
      notify("Product listed on the floor");
      setFields({ name: "", description: "", price: "", categoryId: "", stock: "" });
      setFile(null);
      e.target.reset();
    } catch (err) {
      setError(apiErrorMessage(err, "Couldn't list this product."));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Layout>
      <div className="shell" style={{ maxWidth: 640 }}>
        <h1 style={{ fontSize: 32, marginBottom: 8 }}>Your stall</h1>
        <p style={{ marginBottom: 28 }}>List a new product on the floor.</p>

        {error && <div className="banner banner-error">{error}</div>}

        <form onSubmit={handleSubmit} className="panel" style={{ padding: 28 }}>
          <div className="field">
            <label htmlFor="name">Product name</label>
            <input id="name" required value={fields.name} onChange={(e) => update("name", e.target.value)} />
          </div>
          <div className="field">
            <label htmlFor="description">Description</label>
            <textarea id="description" required rows={3} value={fields.description} onChange={(e) => update("description", e.target.value)} />
          </div>
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 14 }}>
            <div className="field">
              <label htmlFor="price">Price ($)</label>
              <input id="price" type="number" step="0.01" min="0" required value={fields.price} onChange={(e) => update("price", e.target.value)} />
            </div>
            <div className="field">
              <label htmlFor="stock">Stock</label>
              <input id="stock" type="number" min="0" required value={fields.stock} onChange={(e) => update("stock", e.target.value)} />
            </div>
          </div>
          <div className="field">
            <label htmlFor="categoryId">Category</label>
            <select id="categoryId" required value={fields.categoryId} onChange={(e) => update("categoryId", e.target.value)}>
              <option value="" disabled>Choose a category</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </div>
          <div className="field">
            <label htmlFor="file">Product photo</label>
            <input id="file" type="file" accept="image/*" onChange={(e) => setFile(e.target.files[0])} />
          </div>
          <button className="btn btn-accent btn-block" type="submit" disabled={submitting}>
            {submitting ? <span className="spinner" /> : "List product"}
          </button>
        </form>
      </div>
    </Layout>
  );
}
