export enum UserRole {
  DEV_ROLE = 'DEV_ROLE',
  USER_ROLE = 'USER_ROLE',
  ADMIN_ROLE = 'ADMIN_ROLE'
}

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  roles: UserRole[];
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  user: User;
}

// Made with Bob