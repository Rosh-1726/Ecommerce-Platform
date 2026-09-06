export default function Footer() {
  return (
    <footer className="hairline-top" style={{ padding: "28px 0", marginTop: 40 }}>
      <div className="shell" style={{ display: "flex", justifyContent: "space-between", flexWrap: "wrap", gap: 12 }}>
        <span style={{ fontFamily: "var(--font-display)", fontSize: 18, fontWeight: 600 }}>
          The Commons
        </span>
        <span style={{ fontSize: 13, color: "var(--ink-soft)" }}>
          A marketplace where independent sellers set up stall.
        </span>
      </div>
    </footer>
  );
}
