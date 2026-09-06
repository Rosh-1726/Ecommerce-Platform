import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function ProtectedRoute({ children, role }) {
  const { user } = useAuth();
  const location = useLocation();

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  if (role && user.role !== role) {
    return (
      <div className="shell page-body">
        <div className="empty-state">
          <h3>This corner isn't open to you yet</h3>
          <p>Your account doesn't have the right role for this page.</p>
        </div>
      </div>
    );
  }
  return children;
}
