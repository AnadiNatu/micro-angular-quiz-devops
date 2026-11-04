import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { UsersDTO } from '../../../auth/models/dtos';
import { AdminServiceService } from '../../services/admin-service.service';
import { AuthService } from '../../../auth/services/auth/auth.service';

@Component({
  selector: 'app-user-details',
  standalone: true,
  imports: [CommonModule,RouterLink,DatePipe],
  templateUrl: './user-details.component.html',
  styleUrl: './user-details.component.css'
})
export class UserDetailsComponent implements OnInit{

  user: UsersDTO | null = null;

  isLoading=true;
  hasError=false;
  errorMessage='';

  constructor(
    private route:ActivatedRoute,
    private router:Router,
    private adminService:AdminServiceService,
    private authService:AuthService
  ){}

  ngOnInit():void{
    this.loadUser();
  }

loadUser(): void {
  const id = Number(this.route.snapshot.paramMap.get('id'));

  if (!Number.isInteger(id) || id <= 0) {
    this.hasError = true;
    this.errorMessage = 'Invalid user id.';
    this.isLoading = false;
    return;
  }

  this.isLoading = true;
  this.hasError = false;

  this.adminService.getUserDetails().subscribe({
    next: user => {
      this.user = user;
      this.isLoading = false;
    },
    error: err => {
      console.error('Load user details error:', err);
      this.hasError = true;
      this.errorMessage =
        err?.error?.message ?? 'Unable to load user.';
      this.isLoading = false;
    }
  });
}

  refresh():void{
    this.isLoading=true;
    this.hasError=false;
    this.loadUser();
  }

  disableUser(): void {
  if (!this.user) {
    return;
  }

  const id = (this.user as any).authServiceId ?? this.user.id;

  if (id == null) {
    alert('User ID not found.');
    return;
  }

  if (!confirm(`Are you sure you want to disable ${this.user.username}?`)) {
    return;
  }

  this.authService.disableUser(id).subscribe({
    next: () => {
      if (this.user) {
        this.user.enabled = false;
      }

      alert('User disabled successfully.');
    },
    error: err => {
      console.error('Disable user error:', err);
      alert(
        err?.error?.message ?? 'Failed to disable user.'
      );
    }
  });
}

enableUser(): void {
  if (!this.user){
    return;
  }

  const id = (this.user as any).authServiceId ?? this.user.id;

  if(id == null){
    alert('User ID not found');
    return;
  }

  if(!confirm(`Enable ${this.user.username}?`)){
  return;
  }

  this.authService.enableUser(id).subscribe({
    next : () => {
      if(this.user){
        this.user.enabled  = true;
      }
      alert('User enabled successfully');
    },
    error : err => {
      console.error('Enable user error:', err);
      alert(err?.error?.message ?? 'Failed to enable user');
    }
  });
}

  goBack():void{
    this.router.navigate(['/admin/manage-users']);
  }

  getProfileImage():string{
    return this.user?.profilePicture || 'assets/default-profile.png';
  }

  getStatusLabel():string{
    if(!this.user){return '-';}
    return this.user.enabled?'Active':'Disabled';
  }

  getStatusClass():string{
    if(!this.user){return '';}
    return this.user.enabled?'status-active':'status-disabled';
  }

  getRoleClass():string{
    switch((this.user?.role??'').toUpperCase()){
      case 'ADMIN': return 'role-admin';
      case 'CURATOR': return 'role-curator';
      case 'PARTICIPANT': return 'role-participant';
      default: return '';
    }
  }

}

