// src/app/auth/services/user-storage/user-storage.service.ts

import { Injectable } from '@angular/core';
import { UsersDTO, UserRoles } from '../../models/dtos';

const TOKEN_KEY = 'token';
const USER_KEY  = 'user';

@Injectable({ providedIn: 'root' })
export class UserStorageService {

  saveToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  saveUser(user: UsersDTO): void {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  getUser(): UsersDTO | null {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }

  getUserRole(): UserRoles | null {
    const user = this.getUser();
    if (!user) return null;
    // role comes back as plain string e.g. "ADMIN"
    return (user.role as UserRoles) ?? null;
  }

  getUserId(): number | null {
    return this.getUser()?.id ?? null;
  }

  getUserName(): string | null {
    return this.getUser()?.username ?? null;
  }

  isAdminLoggedIn(): boolean {
    return !!this.getToken() && this.getUserRole() === UserRoles.ADMIN;
  }

  isCuratorLoggedIn(): boolean {
    return !!this.getToken() && this.getUserRole() === UserRoles.CURATOR;
  }

  isParticipantLoggedIn(): boolean {
    return !!this.getToken() && this.getUserRole() === UserRoles.PARTICIPANT;
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }
}