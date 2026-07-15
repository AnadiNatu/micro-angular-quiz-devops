// src/app/auth/services/user-storage/user-storage.service.ts

import { Injectable } from '@angular/core';
import { UsersDTO, UserRoles } from '../../models/dtos';
import { ActivatedRoute, Router } from '@angular/router';
import { AdminServiceService } from '../../../admin/services/admin-service.service';

const TOKEN_KEY = 'token';
const USER_KEY  = 'user';

@Injectable({
  providedIn: 'root'
})
export class UserStorageService {

//  constructor(
//   private route: ActivatedRoute,
//   private router: Router,
//   private adminService: AdminServiceService,
//   private storage: UserStorageService
// ) {}

  // --------------------------------------------------------------------
  // Token
  // --------------------------------------------------------------------

  saveToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  // --------------------------------------------------------------------
  // User
  // --------------------------------------------------------------------

  saveUser(user: UsersDTO): void {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  getUser(): UsersDTO | null {
    const raw = localStorage.getItem(USER_KEY);

    if (!raw) {
      return null;
    }

    return JSON.parse(raw);
  }

  // --------------------------------------------------------------------
  // User IDs
  // --------------------------------------------------------------------

  /**
   * Backward compatible.
   *
   * Existing components already call getUserId().
   *
   * Prefer authServiceId if present.
   * Otherwise fall back to id.
   */
  getUserId(): number | null {

    const user: any = this.getUser();

    if (!user) {
      return null;
    }

    return user.authServiceId ?? user.id ?? null;
  }

  /**
   * Preferred method for all new development.
   */
  getAuthUserId(): number | null {

    const user: any = this.getUser();

    return user?.authServiceId ?? null;
  }

  /**
   * Semantic alias used by Question & Quiz creation.
   */
  getCreatorAuthUserId(): number | null {
    return this.getAuthUserId();
  }

  // --------------------------------------------------------------------
  // User Info
  // --------------------------------------------------------------------

  getUserName(): string | null {
    return this.getUser()?.username ?? null;
  }

  getCreatorUsername(): string | null {
    return this.getUserName();
  }

  getUserEmail(): string | null {
    return (this.getUser() as any)?.email ?? null;
  }

  getUserRole(): UserRoles | null {

    const user = this.getUser();

    if (!user) {
      return null;
    }

    return user.role as UserRoles;
  }

  getCreatorRole(): UserRoles | null {
    return this.getUserRole();
  }

  // --------------------------------------------------------------------
  // Login State
  // --------------------------------------------------------------------

  isLoggedIn(): boolean {
    return !!this.getToken() && !!this.getUser();
  }

  isAdminLoggedIn(): boolean {
    return this.isLoggedIn() &&
      this.getUserRole() === UserRoles.ADMIN;
  }

  isCuratorLoggedIn(): boolean {
    return this.isLoggedIn() &&
      this.getUserRole() === UserRoles.CURATOR;
  }

  isParticipantLoggedIn(): boolean {
    return this.isLoggedIn() &&
      this.getUserRole() === UserRoles.PARTICIPANT;
  }

  isQuizTester(): boolean {
    return this.isAdminLoggedIn()
        || this.isCuratorLoggedIn();

}

private static TEST_MODE = "quiz_test_mode";


enableQuizTester() {
    localStorage.setItem(UserStorageService.TEST_MODE,"true");
}

disableQuizTester() {
    localStorage.removeItem(UserStorageService.TEST_MODE);
}

  // --------------------------------------------------------------------
  // Logout
  // --------------------------------------------------------------------

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }
}