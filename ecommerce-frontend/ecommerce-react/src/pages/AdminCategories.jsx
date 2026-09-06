import { useEffect, useState } from "react";
import * as shop from "../api/shop";
import { apiErrorMessage } from "../api/client";
import { useToast } from "../context/ToastContext";

export default function AdminCategories() {
  const { notify } = useToast();
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [submitting, setSubmitting] = useState(false);

  function load() {
    setLoading(true);
    shop.getCategories().then(({ data }) => setCategories(data)).finally(() => setLoading(false));
  }

  useEffect(load, []);

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    try {
      await shop.createCategory({ name, description });
      notify("Category created");
      setName("");
      setDescription("");
      load();
    } catch (err) {
      notify(apiErrorMessage(err, "Couldn't create the category."), "error");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div style={{ display: "grid", gridTemplateColumns: "1fr 360px", gap: 32, alignItems: "flex-start" }}>
      <div className="panel">
        {loading ? (
          <p style={{ padding: 20 }}>Loading…</p>
        ) : categories.length === 0 ? (
          <div className="empty-state">No categories yet</div>
        ) : (
          categories.map((c, idx) => (
            <div key={c.id} style={{ padding: 16, borderBottom: idx < categories.length - 1 ? "1px solid var(--line)" : "none" }}>
              <h3 style={{ fontSize: 16 }}>{c.name}</h3>
              {c.description && <p style={{ fontSize: 13, marginTop: 4 }}>{c.description}</p>}
            </div>
          ))
        )}
      </div>

      <form onSubmit={handleSubmit} className="panel" style={{ padding: 22 }}>
        <h3 style={{ fontSize: 17, marginBottom: 16 }}>New category</h3>
        <div className="field">
          <label htmlFor="cname">Name</label>
          <input id="cname" required value={name} onChange={(e) => setName(e.target.value)} />
        </div>
        <div className="field">
          <label htmlFor="cdesc">Description</label>
          <textarea id="cdesc" rows={2} value={description} onChange={(e) => setDescription(e.target.value)} />
        </div>
        <button className="btn btn-accent btn-block" type="submit" disabled={submitting}>
          {submitting ? <span className="spinner" /> : "Create category"}
        </button>
      </form>
    </div>
  );
}
