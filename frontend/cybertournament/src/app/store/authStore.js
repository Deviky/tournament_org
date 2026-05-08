import { create } from "zustand";
import { authApi } from "@/shared/api/authApi";
import { getIdentityFromToken } from "@/shared/lib/authIdentity";

const buildAuthState = (accessToken, refreshToken) => {
  const identity = getIdentityFromToken(accessToken);

  return {
    accessToken,
    refreshToken,
    isAuth: !!accessToken && !!refreshToken,
    currentUserId: identity.currentUserId,
    currentRole: identity.currentRole,
    currentSubject: identity.currentSubject,
  };
};

const clearStorage = () => {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("refreshToken");
};

export const useAuthStore = create((set) => ({
  ...buildAuthState(
    localStorage.getItem("accessToken"),
    localStorage.getItem("refreshToken")
  ),

  setSession: (accessToken, refreshToken) => {
    localStorage.setItem("accessToken", accessToken);
    localStorage.setItem("refreshToken", refreshToken);
    set(buildAuthState(accessToken, refreshToken));
  },

  clearSession: () => {
    clearStorage();
    set(buildAuthState(null, null));
  },

  initAuth: () => {
    const accessToken = localStorage.getItem("accessToken");
    const refreshToken = localStorage.getItem("refreshToken");
    set(buildAuthState(accessToken, refreshToken));
  },

  login: async (data) => {
    const res = await authApi.login(data);
    const { accessToken, refreshToken } = res;
    localStorage.setItem("accessToken", accessToken);
    localStorage.setItem("refreshToken", refreshToken);
    set(buildAuthState(accessToken, refreshToken));
  },

  logout: async () => {
    try {
      const refreshToken = localStorage.getItem("refreshToken");

      await authApi.logout(refreshToken);

    } catch (e) {
      console.warn("logout error (ignored)", e);
    } finally {
      clearStorage();
      set(buildAuthState(null, null));
    }
  }
}));
