import { Navigate } from "react-router-dom";
import { useAuthStore } from "@/app/store/authStore";

export const PrivateRoute = ({ children }) => {
  const isAuth = useAuthStore((s) => s.isAuth);

  if (!isAuth) {
    return <Navigate to="/login" replace />;
  }

  return children;
};