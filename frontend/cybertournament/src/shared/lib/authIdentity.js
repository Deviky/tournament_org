const decodeBase64Url = (value) => {
  const normalized = value.replace(/-/g, "+").replace(/_/g, "/");
  const padded = normalized.padEnd(normalized.length + ((4 - (normalized.length % 4)) % 4), "=");
  return atob(padded);
};

export const parseJwtPayload = (token) => {
  if (!token) return null;

  try {
    const [, payload] = token.split(".");
    if (!payload) return null;
    return JSON.parse(decodeBase64Url(payload));
  } catch {
    return null;
  }
};

export const getIdentityFromToken = (token) => {
  const payload = parseJwtPayload(token);
  if (!payload) {
    return {
      currentUserId: null,
      currentRole: null,
      currentSubject: null,
    };
  }

  const numericSubject = payload.sub ? Number(payload.sub) : null;
  const numericUserId = payload.userId ? Number(payload.userId) : null;

  return {
    currentUserId: Number.isFinite(numericUserId)
      ? numericUserId
      : Number.isFinite(numericSubject)
        ? numericSubject
        : null,
    currentRole: payload.role || null,
    currentSubject: payload.sub || null,
  };
};

export const hasDirectProfileByIdentity = (role, userId) => {
  if (!role || !userId) return false;
  return role === "PLAYER" || role === "ORGANIZER";
};

export const getProfilePathByIdentity = (role, userId) => {
  if (!role || !userId) return "/profile";

  switch (role) {
    case "PLAYER":
      return `/players/${userId}`;
    case "ORGANIZER":
      return `/organizations/${userId}`;
    default:
      return "/profile";
  }
};

export const getRoleLabel = (role) => {
  switch (role) {
    case "PLAYER":
      return "Игрок";
    case "ORGANIZER":
      return "Организатор";
    case "MODERATOR":
      return "Модератор";
    case "ADMIN":
      return "Администратор";
    default:
      return "Пользователь";
  }
};
