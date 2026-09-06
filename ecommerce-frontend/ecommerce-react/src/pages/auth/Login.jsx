import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import AuthShell from "./AuthShell";
import { useAuth } from "../../context/AuthContext";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    const result = await login(email, password);
    setSubmitting(false);
    if (result.ok) {
      const dest = location.state?.from?.pathname || "/";
      navigate(dest, { replace: true });
    } else {
      setError(result.message);
    }
  }

  return (
    <AuthShell
      eyebrow="Welcome back"
      title="Sign in to your stall"
      footer={<>New here? <Link to="/register" style={{ color: "var(--accent-deep)", fontWeight: 600 }}>Create an account</Link></>}
    >
      {error && <div className="banner banner-error">{error}</div>}
      <form onSubmit={handleSubmit}>
        <div className="field">
          <label htmlFor="email">Email</label>
          <input id="email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} placeholder="you@example.com" />
        </div>
        <div className="field">
          <label htmlFor="password">Password</label>
          <input id="password" type="password" required value={password} onChange={(e) => setPassword(e.target.value)} placeholder="••••••••" />
        </div>
        <button className="btn btn-accent btn-block" disabled={submitting} type="submit">
          {submitting ? <span className="spinner" /> : "Sign in"}
        </button>
      </form>
    </AuthShell>
  );
}
