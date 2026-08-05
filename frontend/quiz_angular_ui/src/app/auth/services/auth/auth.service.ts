import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';

import {
  SignUpRequest,
  SignUpResponse,
  LoginAuthRequest,
  LoginAuthResponse,
  ForgotPasswordRequest,
  ResetPasswordRequest
} from '../../models/dtos';

import { UserStorageService } from '../user-storage/user-storage.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly BASE_URL = 'http://localhost:8080/api/auth';

  constructor(
    private http: HttpClient,
    private storage: UserStorageService
  ) {}

  // SIGN UP
  signup(data: SignUpRequest): Observable<SignUpResponse> {
    return this.http.post<SignUpResponse>(`${this.BASE_URL}/signup`,data);
  }

  // LOGIN
  login(data: LoginAuthRequest): Observable<LoginAuthResponse> {

    return this.http
      .post<LoginAuthResponse>(`${this.BASE_URL}/login`, data)
      .pipe(
        tap((response: LoginAuthResponse) => {

          // Save JWT
          this.storage.saveToken(response.token);

          // Save complete authenticated user
          this.storage.saveUser({

            // Local DB ID (if used anywhere)
            id: response.id,

            // Auth Service ID (used across Question & Quiz services)
            authServiceId: response.authServiceId,

            username: response.username,

            email: response.email,

            role: response.role,

            enabled: true,
            accountNonExpired: true,
            accountNonLocked: true,
            credentialsNonExpired: true
          } as any);
          console.log('Logged in user:', this.storage.getUser());
        })
      );
  }

  // USERS
  getUserById(id: number): Observable<any> {
    return this.http.get(`${this.BASE_URL}/${id}`, {headers: {
          Authorization: `Bearer ${this.storage.getToken()}`}});
  }

  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.BASE_URL}/all`,{headers: {Authorization: `Bearer ${this.storage.getToken()}`}});
  }

  // PASSWORD
  forgotPassword(data: ForgotPasswordRequest): Observable<string> {
    const params = new HttpParams().set('email', data.email);
    return this.http.post(`${this.BASE_URL}/forgot-password`, null,{params,responseType: 'text'});
  }

  resetPassword(data: ResetPasswordRequest): Observable<string> {

    const params = new HttpParams()
      .set('email', data.email)
      .set('token', data.token)
      .set('newPassword', data.newPassword);

    return this.http.post(`${this.BASE_URL}/reset-password`,null,{
        params,
        responseType: 'text'
      });
  }

  // ADMIN
  disableUser(id: number): Observable<any> {
    return this.http.put(`${this.BASE_URL}/disable/${id}`, null ,{headers: {Authorization: `Bearer ${this.storage.getToken()}`}});
  }

  // PROFILE
  uploadProfilePhoto(username: string, file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post(`${this.BASE_URL}/${username}/upload-profile-photo`,formData,
      {responseType: 'text'});
  }
}