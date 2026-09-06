import { Link } from "react-router-dom";

export default function AuthShell({ eyebrow, title, children, footer }) {
  return (
    <div className="page">
      <div className="shell" style={{ padding: "0 28px" }}>
        <Link to="/" style={{ display: "inline-block", padding: "28px 0", fontFamily: "var(--font-display)", fontSize: 22, fontWeight: 700 }}>
          The Commons
        </Link>
      </div>
      <div style={{ flex: 1, display: "flex", alignItems: "center", justifyContent: "center", padding: "20px 20px 80px" }}>
        <div className="panel" style={{ width: "100%", maxWidth: 420, padding: 40 }}>
          {eyebrow && <p style={{ fontSize: 13, fontWeight: 700, color: "var(--accent-deep)", marginBottom: 8 }}>{eyebrow}</p>}
          <h1 style={{ fontSize: 30, marginBottom: 28 }}>{title}</h1>
          {children}
          {footer && <div style={{ marginTop: 24, fontSize: 14, textAlign: "center", color: "var(--ink-soft)" }}>{footer}</div>}
        </div>
      </div>
    </div>
  );
}
