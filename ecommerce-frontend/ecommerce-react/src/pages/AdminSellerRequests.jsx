import { useEffect, useState } from "react";
import * as shop from "../api/shop";
import { apiErrorMessage } from "../api/client";
import { useToast } from "../context/ToastContext";

export default function AdminSellerRequests() {
  const { notify } = useToast();
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState(null);

  function load() {
    setLoading(true);
    shop
      .listPendingSellerRequests()
      .then(({ data }) => setRequests(data))
      .catch(() => setRequests([]))
      .finally(() => setLoading(false));
  }

  useEffect(load, []);

  async function approve(id) {
    setBusyId(id);
    try {
      await shop.approveSellerRequest(id);
      notify("Seller request approved");
      load();
    } catch (err) {
      notify(apiErrorMessage(err), "error");
    } finally {
      setBusyId(null);
    }
  }

  async function reject(id) {
    setBusyId(id);
    try {
      await shop.rejectSellerRequest(id, "Not a fit right now");
      notify("Seller request rejected");
      load();
    } catch (err) {
      notify(apiErrorMessage(err), "error");
    } finally {
      setBusyId(null);
    }
  }

  if (loading) return <p>Loading…</p>;

  if (requests.length === 0) {
    return (
      <div className="empty-state">
        <h3>No pending requests</h3>
        <p>New seller applications will show up here for review.</p>
      </div>
    );
  }

  return (
    <div className="panel">
      {requests.map((r, idx) => (
        <div key={r.id} style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: 18, borderBottom: idx < requests.length - 1 ? "1px solid var(--line)" : "none" }}>
          <div>
            <h3 style={{ fontSize: 16 }}>{r.storeName}</h3>
            <p style={{ fontSize: 13, marginTop: 4 }}>{r.userEmail}</p>
            {r.reason && <p style={{ fontSize: 13, marginTop: 4 }}>"{r.reason}"</p>}
            {r.documentUrl && (
              <a href={r.documentUrl} target="_blank" rel="noreferrer" style={{ fontSize: 13, color: "var(--accent-deep)", fontWeight: 600 }}>
                View document
              </a>
            )}
          </div>
          <div style={{ display: "flex", gap: 8 }}>
            <button className="btn btn-accent btn-sm" disabled={busyId === r.id} onClick={() => approve(r.id)}>Approve</button>
            <button className="btn btn-danger btn-sm" disabled={busyId === r.id} onClick={() => reject(r.id)}>Reject</button>
          </div>
        </div>
      ))}
    </div>
  );
}
