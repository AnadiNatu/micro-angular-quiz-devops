// src/app/auth/models/dtos.ts

export enum UserRoles {
  ADMIN = 'ADMIN',
  PARTICIPANT = 'PARTICIPANT',
  CURATOR = 'CURATOR'
}

export interface SignUpRequest {
  username: string;
  password: string;
  email: string;
  role: string;
}

export interface SignUpResponse {
  id: number;
  username: string;
  email: string;
  role: string;
  enabled: boolean;
}

export interface UsersDTO {
  id: number;
  username: string;
  email: string;
  phoneNumber?: string;
  profilePicture?: string;
  role: string;
  enabled: boolean;
  accountNonExpired: boolean;
  accountNonLocked: boolean;
  credentialsNonExpired: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface LoginAuthRequest {
  username: string;
  password: string;
}

export interface LoginAuthResponse {
  token: string;
  username: string;
  role: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  email: string;
  token: string;
  newPassword: string;
}