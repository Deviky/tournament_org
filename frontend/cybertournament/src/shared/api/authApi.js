import { api, unwrap } from "./client";

export const authApi = {
  login: (data) => unwrap(api.post("/auth/login", data)),
  registerPlayer: (data) => unwrap(api.post("/auth/register/player", data)),
  registerOrg: (data) => unwrap(api.post("/auth/register/organization", data)),
  confirm: (token) => unwrap(api.get("/auth/confirm", { params: { token } })),
  resend: (email) => unwrap(api.post("/auth/resend_confirmation", null, { params: { email } })),
  forgot: (email) => unwrap(api.post("/auth/forgot_password", null, { params: { email } })),
  reset: (token, data) => unwrap(api.post("/auth/reset_password", data, { params: { token } })),
  refresh: (refreshToken) => unwrap(api.post("/auth/refresh", { refreshToken })),
  logout: (refreshToken) => unwrap(api.post("/auth/logout", { refreshToken })),
  createModerator: (data) => unwrap(api.post("/auth/create_moderator", data)),
};
