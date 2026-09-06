import { useEffect, useState } from "react";
import { useSearchParams, Link } from "react-router-dom";
import AuthShell from "./AuthShell";
import * as authApi from "../../api/auth";
import { apiErrorMessage } from "../../api/client";

export default function VerifyEmail() {
  const [params] = useSearchParams();
  const token = params.get("token");
  const [status, setStatus] = useState("checking");
  const [message, setMessage] = useState("");

  useEffect(() => {
    if (!token) {
      setStatus("error");
      setMessage("This verification link is missing its token.");
      return;
    }
    authApi
      .verifyEmail(token)
      .then(() => {
        setStatus("success");
      })
      .catch((err) => {
        setStatus("error");
        setMessage(apiErrorMessage(err, "This link has expired or was already used."));
      });
  }, [token]);

  return (
    <AuthShell eyebrow="Account verification" title={status === "success" ? "You're verified" : "Verifying your email"}>
      {status === "checking" && <p>Hold on a second…</p>}
      {status === "success" && (
        <>
          <p style={{ marginBottom: 24 }}>Your email is confirmed. You can sign in now.</p>
          <Link to="/login" className="btn btn-accent btn-block">Sign in</Link>
        </>
      )}
      {status === "error" && (
        <>
          <div className="banner banner-error">{message}</div>
          <Link to="/register" className="btn btn-outline btn-block">Back to registration</Link>
        </>
      )}
    </AuthShell>
  );
}
