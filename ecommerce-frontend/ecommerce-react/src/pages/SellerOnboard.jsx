import { useState } from "react";
import Layout from "../components/Layout";
import * as shop from "../api/shop";
import { apiErrorMessage } from "../api/client";
import { useAuth } from "../context/AuthContext";

export default function SellerOnboard() {
  const { user } = useAuth();
  const [storeName, setStoreName] = useState("");
  const [reason, setReason] = useState("");
  const [file, setFile] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [done, setDone] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    if (!file) {
      setError("Attach an ID or business document — it's required for review.");
      return;
    }
    setSubmitting(true);
    try {
      await shop.requestSeller(storeName, reason, file);
      setDone(true);
    } catch (err) {
      setError(apiErrorMessage(err, "Couldn't submit your request."));
    } finally {
      setSubmitting(false);
    }
  }

  if (user?.role === "ROLE_SELLER") {
    return (
      <Layout>
        <div className="shell" style={{ maxWidth: 560 }}>
          <div className="empty-state">
            <h3>You already run a stall</h3>
            <p>Head to your seller dashboard to list products.</p>
          </div>
        </div>
      </Layout>
    );
  }

  if (done) {
    return (
      <Layout>
        <div className="shell" style={{ maxWidth: 560 }}>
          <div className="empty-state">
            <h3>Request sent</h3>
            <p>An admin will review your stall request. You'll be able to list products once it's approved — sign in again afterwards to pick up your new role.</p>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="shell" style={{ maxWidth: 560 }}>
        <h1 style={{ fontSize: 32, marginBottom: 8 }}>Open a stall</h1>
        <p style={{ marginBottom: 28 }}>Tell us a bit about what you'd like to sell. An admin reviews every request before it goes live.</p>

        {error && <div className="banner banner-error">{error}</div>}

        <form onSubmit={handleSubmit} className="panel" style={{ padding: 28 }}>
          <div className="field">
            <label htmlFor="storeName">Store name</label>
            <input id="storeName" required value={storeName} onChange={(e) => setStoreName(e.target.value)} placeholder="e.g. Maple & Co" />
          </div>
          <div className="field">
            <label htmlFor="reason">Why do you want to sell here? (optional)</label>
            <textarea id="reason" rows={3} value={reason} onChange={(e) => setReason(e.target.value)} placeholder="A line or two about what you make" />
          </div>
          <div className="field">
            <label htmlFor="document">Verification document</label>
            <input id="document" type="file" required accept="image/*,.pdf" onChange={(e) => setFile(e.target.files[0])} />
            <span className="field-hint">An ID or business proof, so admins can verify who's selling.</span>
          </div>
          <button className="btn btn-accent btn-block" type="submit" disabled={submitting}>
            {submitting ? <span className="spinner" /> : "Submit request"}
          </button>
        </form>
      </div>
    </Layout>
  );
}
