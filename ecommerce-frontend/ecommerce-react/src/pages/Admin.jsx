import { NavLink, Routes, Route, Navigate } from "react-router-dom";
import Layout from "../components/Layout";
import AdminCategories from "./AdminCategories";
import AdminSellerRequests from "./AdminSellerRequests";

export default function Admin() {
  const tabStyle = ({ isActive }) => ({
    padding: "10px 18px",
    fontSize: 14,
    fontWeight: 600,
    color: isActive ? "var(--ink)" : "var(--ink-soft)",
    borderBottom: isActive ? "2px solid var(--accent)" : "2px solid transparent",
  });

  return (
    <Layout>
      <div className="shell">
        <h1 style={{ fontSize: 32, marginBottom: 20 }}>Admin</h1>
        <div className="hairline-bottom" style={{ display: "flex", gap: 4, marginBottom: 28 }}>
          <NavLink to="/admin/categories" style={tabStyle}>Categories</NavLink>
          <NavLink to="/admin/seller-requests" style={tabStyle}>Seller requests</NavLink>
        </div>
        <Routes>
          <Route index element={<Navigate to="categories" replace />} />
          <Route path="categories" element={<AdminCategories />} />
          <Route path="seller-requests" element={<AdminSellerRequests />} />
        </Routes>
      </div>
    </Layout>
  );
}
