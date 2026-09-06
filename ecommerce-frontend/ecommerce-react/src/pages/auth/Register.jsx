import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import AuthShell from "./AuthShell";
import { useAuth } from "../../context/AuthContext";

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [fields, setFields] = useState({ firstName: "", lastName: "", email: "", password: "" });
  const [error, setError] = useState("");
  const [done, setDone] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  function update(key, value) {
    setFields((f) => ({ ...f, [key]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    const result = await register(fields);
    setSubmitting(false);
    if (result.ok) {
      setDone(true);
    } else {
      setError(result.message);
    }
  }

  if (done) {
    return (
      <AuthShell eyebrow="Almost there" title="Check your inbox">
        <p style={{ marginBottom: 24 }}>
          We've sent a verification link to <strong>{fields.email}</strong>. Open it
          to activate your account, then come back and sign in.
        </p>
        <button className="btn btn-outline btn-block" onClick={() => navigate("/login")}>
          Go to sign in
        </button>
      </AuthShell>
    );
  }

  return (
    <AuthShell
      eyebrow="Join the floor"
      title="Create your account"
      footer={<>Already have one? <Link to="/login" style={{ color: "var(--accent-deep)", fontWeight: 600 }}>Sign in</Link></>}
    >
      {error && <div className="banner banner-error">{error}</div>}
      <form onSubmit={handleSubmit}>
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 14 }}>
          <div className="field">
            <label htmlFor="firstName">First name</label>
            <input id="firstName" required value={fields.firstName} onChange={(e) => update("firstName", e.target.value)} />
          </div>
          <div className="field">
            <label htmlFor="lastName">Last name</label>
            <input id="lastName" required value={fields.lastName} onChange={(e) => update("lastName", e.target.value)} />
          </div>
        </div>
        <div className="field">
          <label htmlFor="email">Email</label>
          <input id="email" type="email" required value={fields.email} onChange={(e) => update("email", e.target.value)} placeholder="you@example.com" />
        </div>
        <div className="field">
          <label htmlFor="password">Password</label>
          <input id="password" type="password" required minLength={6} value={fields.password} onChange={(e) => update("password", e.target.value)} placeholder="At least 6 characters" />
        </div>
        <button className="btn btn-accent btn-block" disabled={submitting} type="submit">
          {submitting ? <span className="spinner" /> : "Create account"}
        </button>
      </form>
    </AuthShell>
  );
}
