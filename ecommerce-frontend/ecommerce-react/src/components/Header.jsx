import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { useState } from "react";

function initials(user) {
  if (!user) return "";
  return `${user.firstName?.[0] || ""}${user.lastName?.[0] || ""}`.toUpperCase();
}

export default function Header() {
  const { user, logout } = useAuth();
  const { itemCount } = useCart();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  const navLinkStyle = ({ isActive }) => ({
    fontSize: 14,
    fontWeight: 600,
    color: isActive ? "var(--ink)" : "var(--ink-soft)",
    borderBottom: isActive ? "2px solid var(--accent)" : "2px solid transparent",
    paddingBottom: 4,
  });

  return (
    <header className="hairline-bottom" style={{ background: "var(--paper)", position: "sticky", top: 0, zIndex: 20 }}>
      <div
        className="shell"
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          height: 78,
          gap: 24,
        }}
      >
        <NavLink to="/" style={{ display: "flex", alignItems: "baseline", gap: 8 }}>
          <span style={{ fontFamily: "var(--font-display)", fontSize: 26, fontWeight: 700 }}>
            The Commons
          </span>
        </NavLink>

        <nav style={{ display: "flex", gap: 28, flex: 1, justifyContent: "center" }}>
          <NavLink to="/products" style={navLinkStyle}>Shop</NavLink>
          {user?.role === "ROLE_SELLER" && (
            <NavLink to="/seller/dashboard" style={navLinkStyle}>My stall</NavLink>
          )}
          {user?.role === "ROLE_USER" && (
            <NavLink to="/sell" style={navLinkStyle}>Start selling</NavLink>
          )}
          {user?.role === "ROLE_ADMIN" && (
            <NavLink to="/admin" style={navLinkStyle}>Admin</NavLink>
          )}
        </nav>

        <div style={{ display: "flex", alignItems: "center", gap: 18 }}>
          {user && (
            <NavLink to="/wishlist" aria-label="Wishlist" style={{ fontSize: 14, color: "var(--ink-soft)", fontWeight: 600 }}>
              Saved
            </NavLink>
          )}
          {user && (
            <NavLink to="/cart" aria-label="Cart" style={{ position: "relative", fontSize: 14, fontWeight: 600 }}>
              Bag
              {itemCount > 0 && (
                <span
                  style={{
                    position: "absolute",
                    top: -10,
                    right: -16,
                    background: "var(--accent)",
                    color: "#fff",
                    fontSize: 11,
                    fontWeight: 700,
                    borderRadius: 999,
                    minWidth: 18,
                    height: 18,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    padding: "0 4px",
                  }}
                >
                  {itemCount}
                </span>
              )}
            </NavLink>
          )}

          {user ? (
            <div style={{ position: "relative" }}>
              <button
                onClick={() => setMenuOpen((v) => !v)}
                style={{
                  width: 36,
                  height: 36,
                  borderRadius: "50%",
                  background: "var(--ink)",
                  color: "var(--paper)",
                  border: "none",
                  fontWeight: 700,
                  fontSize: 13,
                }}
              >
                {initials(user)}
              </button>
              {menuOpen && (
                <div
                  className="panel"
                  style={{
                    position: "absolute",
                    right: 0,
                    top: 44,
                    minWidth: 180,
                    padding: 8,
                    boxShadow: "0 10px 24px rgba(27,35,31,0.14)",
                  }}
                  onMouseLeave={() => setMenuOpen(false)}
                >
                  <div style={{ padding: "8px 10px", fontSize: 13, color: "var(--ink-soft)" }}>
                    {user.firstName} {user.lastName}
                  </div>
                  <button
                    className="btn btn-ghost btn-block"
                    style={{ justifyContent: "flex-start" }}
                    onClick={() => { setMenuOpen(false); navigate("/orders"); }}
                  >
                    My orders
                  </button>
                  <button
                    className="btn btn-ghost btn-block"
                    style={{ justifyContent: "flex-start", color: "var(--error)" }}
                    onClick={() => { logout(); navigate("/"); }}
                  >
                    Sign out
                  </button>
                </div>
              )}
            </div>
          ) : (
            <NavLink to="/login" className="btn btn-primary btn-sm">Sign in</NavLink>
          )}
        </div>
      </div>
    </header>
  );
}
