import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { UsersDTO } from '../../../auth/models/dtos';
import { AdminServiceService } from '../../services/admin-service.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

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
    this.router.navigate(['/admin/users', id]);
  }

  disableUser(user: UsersDTO): void {

    const id = (user as any).authServiceId ?? user.id;

    if (id == null) {
      alert('User id not found.');
      return;
    }

    if (!confirm(`Disable ${user.username}?`)) {
      return;
    }

    alert("Disable User functionality has not yet been implemented.");
    // this.adminService.disableUser(id).subscribe({
    //   next: () => {
    //     user.enabled = false;
    //     this.applyFilters();
    //     alert('User disabled successfully.');
    //   },
    //   error: err => {
    //     console.error(err);
    //     alert(err?.error?.message ?? 'Failed to disable user.');
    //   }
    // });
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
}

