export interface User {
  id: number;
  email: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  user: User;
  sessionId: string;
  roles: string[];
}

export interface SessionInfo {
  authenticated: boolean;
  user?: User;
  roles: string[];
}
