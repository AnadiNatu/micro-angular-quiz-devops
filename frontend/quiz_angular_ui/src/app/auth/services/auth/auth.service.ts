// src/app/auth/services/auth/auth.service.ts

import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, tap } from "rxjs";
import { SignUpRequest, SignUpResponse, LoginAuthRequest, LoginAuthResponse, ForgotPasswordRequest, ResetPasswordRequest } from "../../models/dtos";
import { UserStorageService } from "../user-storage/user-storage.service";
import { environment } from "../../../../environments/environment";

@Injectable({ providedIn: 'root' })
export class AuthService {

  private BASE_URL = environment.apiBaseUrl +
   '/api/auth';

  constructor(
    private http: HttpClient,
    private storage: UserStorageService
  ) {}

  // POST /api/auth/signup
  signup(data: SignUpRequest): Observable<SignUpResponse> {
    return this.http.post<SignUpResponse>(`${this.BASE_URL}/signup`, data);
  }

  // POST /api/auth/login  →  { token, username, role }
  login(data: LoginAuthRequest): Observable<LoginAuthResponse> {
    return this.http.post<LoginAuthResponse>(`${this.BASE_URL}/login`, data).pipe(
      tap((response: LoginAuthResponse) => {
        this.storage.saveToken(response.token);
        this.storage.saveUser({
          id: 0,                       // will be enriched by /api/auth/{id} if needed
          username: response.username,
          email: '',
          role: response.role,
          enabled: true,
          accountNonExpired: true,
          accountNonLocked: true,
          credentialsNonExpired: true
        });
      })
    );
  }

  // GET /api/auth/{id}
  getUserById(id: number): Observable<any> {
    return this.http.get(`${this.BASE_URL}/${id}`, {
      headers: { Authorization: `Bearer ${this.storage.getToken()}` }
    });
  }

  // GET /api/auth/all  (ADMIN only)
  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.BASE_URL}/all`, {
      headers: { Authorization: `Bearer ${this.storage.getToken()}` }
    });
  }

  // POST /api/auth/forgot-password?email=...
  forgotPassword(data: ForgotPasswordRequest): Observable<string> {
    const params = new HttpParams().set('email', data.email);
    return this.http.post(`${this.BASE_URL}/forgot-password`, null, {
      params,
      responseType: 'text'
    });
  }

  // POST /api/auth/reset-password?email=...&token=...&newPassword=...
  resetPassword(data: ResetPasswordRequest): Observable<string> {
    const params = new HttpParams()
      .set('email', data.email)
      .set('token', data.token)
      .set('newPassword', data.newPassword);
    return this.http.post(`${this.BASE_URL}/reset-password`, null, {
      params,
      responseType: 'text'
    });
  }

  // PUT /api/auth/disable/{id}  (ADMIN only)
  disableUser(id: number): Observable<any> {
    return this.http.put(`${this.BASE_URL}/disable/${id}`, null, {
      headers: { Authorization: `Bearer ${this.storage.getToken()}` }
    });
  }

  uploadProfilePhoto(username: string, file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(
      `${this.BASE_URL}/${username}/upload-profile-photo`,
      formData,
      { responseType: 'text' }
    );
  }
}