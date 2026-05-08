import axios from "axios";

export const api = axios.create({
  baseURL: "http://localhost:8777",
});

const FALLBACK_ERROR_MESSAGE = "Сервер вернул некорректный ответ";

export class ApiClientError extends Error {
  constructor(message, options = {}) {
    super(message);
    this.name = "ApiClientError";
    this.isApiClientError = true;
    this.status = options.status ?? null;
    this.payload = options.payload;
    this.code = options.code ?? "API_ERROR";
  }
}

const isObject = (value) => typeof value === "object" && value !== null;

const hasApiErrorFlag = (payload) => "isError" in payload || "error" in payload;

const getApiErrorFlag = (payload) =>
  "isError" in payload ? payload.isError : "error" in payload ? payload.error : false;

export const isApiResponse = (payload) =>
  isObject(payload) && "message" in payload && "data" in payload;

export const getErrorMessage = (error, fallback = "Произошла ошибка") => {
  if (error instanceof ApiClientError && error.message) {
    return error.message;
  }

  if (axios.isAxiosError(error)) {
    const payload = error.response?.data;

    if (isApiResponse(payload) && payload.message) {
      return payload.message;
    }

    if (typeof payload === "string" && payload.trim()) {
      return payload;
    }

    if (error.message) {
      return error.message;
    }
  }

  if (error instanceof Error && error.message) {
    return error.message;
  }

  return fallback;
};

const extractApiData = (payload, status = null) => {
  if (!isApiResponse(payload)) {
    throw new ApiClientError(FALLBACK_ERROR_MESSAGE, {
      status,
      payload,
      code: "INVALID_API_RESPONSE",
    });
  }

  if (getApiErrorFlag(payload)) {
    throw new ApiClientError(payload.message || "Ошибка запроса", {
      status,
      payload,
      code: "API_RESPONSE_ERROR",
    });
  }

  return payload.data;
};

export const unwrap = (promise) =>
  promise.then((response) => extractApiData(response.data, response.status));

export const withUserId = (userId, config = {}) => ({
  ...config,
  headers: {
    ...config.headers,
    "X-User-Id": userId,
  },
});

export const withPlayerId = (playerId, config = {}) => ({
  ...config,
  headers: {
    ...config.headers,
    "X-Player-Id": playerId,
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (res) => res,
  async (err) => {
    const original = err.config;
    const requestUrl = String(original?.url || "");
    const hasRefreshToken = !!localStorage.getItem("refreshToken");
    const tokenExpiredHeader = err.response?.headers?.["x-token-expired"] === "true";
    const canTrySilentRefresh =
      err.response?.status === 401 &&
      !requestUrl.includes("/auth/") &&
      hasRefreshToken;

    const isExpired = tokenExpiredHeader || canTrySilentRefresh;

    if (isExpired && original && !original._retry) {
      original._retry = true;

      const refreshToken = localStorage.getItem("refreshToken");

      if (!refreshToken) {
        throw err;
      }

      try {
        const res = await api.post("/auth/refresh", { refreshToken });
        const refreshData = extractApiData(res.data, res.status);
        const newToken = refreshData.accessToken;

        localStorage.setItem("accessToken", newToken);
        original.headers = {
          ...(original.headers || {}),
          Authorization: `Bearer ${newToken}`,
        };

        return api(original);
      } catch (e) {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        window.location.href = "/login";
        throw e;
      }
    }

    const payload = err.response?.data;

    if (err.response && isApiResponse(payload)) {
      throw new ApiClientError(payload.message || "Ошибка запроса", {
        status: err.response.status,
        payload,
        code:
          hasApiErrorFlag(payload) && getApiErrorFlag(payload)
            ? "API_RESPONSE_ERROR"
            : "HTTP_ERROR",
      });
    }

    if (err.response && payload && !isApiResponse(payload)) {
      throw new ApiClientError(FALLBACK_ERROR_MESSAGE, {
        status: err.response.status,
        payload,
        code: "INVALID_API_RESPONSE",
      });
    }

    throw err;
  }
);
