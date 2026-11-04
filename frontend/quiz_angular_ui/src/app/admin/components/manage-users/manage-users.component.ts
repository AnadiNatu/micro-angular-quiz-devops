import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { UsersDTO } from '../../../auth/models/dtos';
import { AdminServiceService } from '../../services/admin-service.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../auth/services/auth/auth.service';

@Component({
  selector: 'app-manage-users',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './manage-users.component.html',
  styleUrl: './manage-users.component.css'
})
export class ManageUsersComponent implements OnInit {

  users: UsersDTO[] = [];
  filteredUsers: UsersDTO[] = [];

  searchText = '';
  selectedRole = '';
  showEnabledOnly = false;

  isLoading = true;
  hasError = false;
  errorMessage = '';

  constructor(
    private adminService: AdminServiceService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading = true;
    this.hasError = false;

    this.adminService.getAllUsers().subscribe({
      next: users => {
        this.users = users ?? [];
        this.applyFilters();
        this.isLoading = false;
      },
      error: err => {
        console.error(err);
        this.hasError = true;
        this.errorMessage = err?.error?.message ?? 'Unable to load users.';
        this.isLoading = false;
      }
    });
  }

  refresh(): void {
    this.loadUsers();
  }

  applyFilters(): void {
    let list = [...this.users];

    if (this.searchText.trim()) {
      const term = this.searchText.trim().toLowerCase();
      list = list.filter(u =>
        u.username?.toLowerCase().includes(term) ||
        u.email?.toLowerCase().includes(term));
    }

    if (this.selectedRole) {
      list = list.filter(u => u.role === this.selectedRole);
    }

    if (this.showEnabledOnly) {
      list = list.filter(u => u.enabled);
    }

    this.filteredUsers = list.sort((a,b)=>
      (a.username ?? '').localeCompare(b.username ?? '')
    );
  }

  onSearch(): void { this.applyFilters(); }
  onRoleChange(): void { this.applyFilters(); }
  onEnabledToggle(): void { this.applyFilters(); }

  clearFilters(): void {
    this.searchText = '';
    this.selectedRole = '';
    this.showEnabledOnly = false;
    this.applyFilters();
  }

  viewUser(user: UsersDTO): void {
    const id = (user as any).authServiceId ?? user.id;
    
    if(id == null){
      alert('User id not found.');
      return;
    }

    this.router.navigate(['/admin/user-details', id]);
  }

  disableUser(user: UsersDTO): void {
  const id = (user as any).authServiceId ?? user.id;

  if (id == null) {
    alert('User ID not found.');
    return;
  }

  if (!user.enabled) {
    return;
  }

  if (!confirm(`Are you sure you want to disable ${user.username}?`)) {
    return;
  }

  this.authService.disableUser(id).subscribe({
    next: () => {
      user.enabled = false;
      this.applyFilters();
      alert(`${user.username} has been disabled successfully.`);
    },
    error: (err) => {
      console.error('Disable user error:', err);
      alert(
        err?.error?.message ??
        'Failed to disable user. Please try again.'
      );
    }
  });
}

enableUser(user : UsersDTO) : void{
  const id = (user as any).authServiceId ?? user.id;

  if(id == null){
    alert('User ID not found');
    return;
  }

  if(!confirm(`Enable ${user.username}?`)){
    return;
  }

  this.authService.enableUser(id).subscribe({
    next : () => {
      user.enabled = true;
      this.applyFilters();
      alert(`${user.username} has been enabled successfully.`);
    },
    error : err => {
      console.error('Enable user error:', err);
      alert(err?.error?.message ?? 'Failed to enable user');
    }
  })
}

  getRoleClass(role: string): string {
    switch ((role ?? '').toUpperCase()) {
      case 'ADMIN': return 'role-admin';
      case 'CURATOR': return 'role-curator';
      case 'PARTICIPANT': return 'role-participant';
      default: return '';
    }
  }

  trackUser(_: number, user: UsersDTO): number {
    return ((user as any).authServiceId ?? user.id) as number;
  }

  goToDashboard(): void {
  this.router.navigate(['/admin']);
}
}

