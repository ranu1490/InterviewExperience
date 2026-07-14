export interface User {
  id: number;
  username: string;
  email: string;
  fullName?: string;
  bio?: string;
  avatarUrl?: string;
  provider: string;
  roles: string[];
  banned: boolean;
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  user: User;
}

export interface SignupRequest {
  username: string;
  email: string;
  password: string;
  fullName?: string;
}

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface UpdateProfileRequest {
  fullName?: string;
  bio?: string;
  avatarUrl?: string;
}
