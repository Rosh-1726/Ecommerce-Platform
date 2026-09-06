import client from "./client";

export function register({ firstName, lastName, email, password }) {
  const form = new FormData();
  form.append("firstName", firstName);
  form.append("lastName", lastName);
  form.append("email", email);
  form.append("password", password);
  return client.post("/auth/register", form, {
    headers: { "Content-Type": "multipart/form-data" },
  });
}

export function login({ email, password }) {
  return client.post("/auth/login", { email, password });
}

export function verifyEmail(token) {
  return client.get("/auth/verify-email", { params: { token } });
}
