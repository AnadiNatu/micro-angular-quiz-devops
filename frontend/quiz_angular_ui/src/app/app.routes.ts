import { Routes } from '@angular/router';
import { HomeComponent } from './auth/components/home/home.component';
import { LoginComponent } from './auth/components/login/login.component';
import { SignupComponent } from './auth/components/signup/signup.component';
import { ForgotPasswordComponent } from './auth/components/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './auth/components/reset-password/reset-password.component';

export const routes: Routes = [
  { path: '',                  redirectTo: 'home', pathMatch: 'full' },
  { path: 'home',              component: HomeComponent },
  { path: 'login',             component: LoginComponent },
  { path: 'signup',            component: SignupComponent },
  { path: 'forgot-password',   component: ForgotPasswordComponent },
  { path: 'reset-password',    component: ResetPasswordComponent },

  {
    path: 'admin',
    loadChildren: () =>
      import('./admin/admin.routes').then(m => m.adminRoutes)
  },

  // Wildcard always last
  { path: '**', redirectTo: 'home' }
];