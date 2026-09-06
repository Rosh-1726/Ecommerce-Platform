import { Link } from "react-router-dom";

const FALLBACK_IMG =
  "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='400' height='400'><rect width='400' height='400' fill='%23EFE9DD'/><text x='50%25' y='50%25' font-family='sans-serif' font-size='16' fill='%23A69F8C' text-anchor='middle'>No photo</text></svg>";

export default function ProductCard({ product }) {
  const outOfStock = product.stock <= 0 || product.active === false;
  return (
    <Link
      to={`/products/${product.id}`}
      className="panel"
      style={{ display: "flex", flexDirection: "column", overflow: "hidden" }}
    >
      <div style={{ aspectRatio: "1 / 1", background: "var(--accent-tint)", overflow: "hidden", position: "relative" }}>
        <img
          src={product.imageUrl || FALLBACK_IMG}
          alt={product.name}
          style={{ width: "100%", height: "100%", objectFit: "cover" }}
          onError={(e) => { e.currentTarget.src = FALLBACK_IMG; }}
        />
        {outOfStock && (
          <span className="tag tag-error" style={{ position: "absolute", top: 10, left: 10 }}>
            Sold out
          </span>
        )}
      </div>
      <div style={{ padding: "16px 16px 18px", display: "flex", flexDirection: "column", gap: 6, flex: 1 }}>
        {product.categoryName && (
          <span style={{ fontSize: 12, color: "var(--ink-soft)" }}>{product.categoryName}</span>
        )}
        <h3 style={{ fontSize: 17 }}>{product.name}</h3>
        <div style={{ marginTop: "auto", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <span className="price" style={{ fontSize: 17 }}>
            ${Number(product.price).toFixed(2)}
          </span>
          {typeof product.stock === "number" && !outOfStock && (
            <span style={{ fontSize: 12, color: "var(--ink-soft)" }}>{product.stock} left</span>
          )}
        </div>
      </div>
    </Link>
  );
}
