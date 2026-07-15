import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { UsersDTO } from '../../../auth/models/dtos';

@Component({
  selector: 'app-disable-user-dialog',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './disable-user-dialog.component.html',
  styleUrl: './disable-user-dialog.component.css'
})
export class DisableUserDialogComponent {

  /**
   * Controls whether the dialog is visible.
   */
  @Input()
  visible = false;

  /**
   * User to be displayed inside the confirmation dialog.
   */
  @Input()
  user: UsersDTO | null = null;

  /**
   * Prevents multiple clicks while disabling.
   */
  @Input()
  loading = false;

  /**
   * Fired when the dialog should close.
   */
  @Output()
  cancel = new EventEmitter<void>();

  /**
   * Fired when the admin confirms disabling the user.
   */
  @Output()
  confirm = new EventEmitter<UsersDTO>();

  constructor() {}

  close(): void {
    this.cancel.emit();
  }

  confirmDisable(): void {

    if (!this.user || this.loading) {
      return;
    }

    this.confirm.emit(this.user);

  }

  onBackdropClick(event: MouseEvent): void {

    if (event.target === event.currentTarget) {
      this.close();
    }

  }

  getRoleClass(): string {

    switch ((this.user?.role ?? '').toUpperCase()) {

      case 'ADMIN':
        return 'role-admin';

      case 'CURATOR':
        return 'role-curator';

      case 'PARTICIPANT':
        return 'role-participant';

      default:
        return '';

    }

  }

  getStatusText(): string {

    if (!this.user) {
      return '-';
    }

    return this.user.enabled
      ? 'Active'
      : 'Already Disabled';

  }

  getStatusClass(): string {

    if (!this.user) {
      return '';
    }

    return this.user.enabled
      ? 'status-active'
      : 'status-disabled';

  }

  getDisplayName(): string {
    return this.user?.username ?? 'Unknown User';
  }

  getEmail(): string {
    return this.user?.email ?? '-';
  }

  canDisable(): boolean {
    return !!this.user && this.user.enabled && !this.loading;
  }

}

// Add these properties to manage-users.component.ts
// showDisableDialog = false;
// selectedUser: UsersDTO | null = null;
// isDisabling = false;
// Replace the current disableUser() method with
// openDisableDialog(user: UsersDTO): void {
//   this.selectedUser = user;
//   this.showDisableDialog = true;
// }
// closeDisableDialog(): void {
//   this.showDisableDialog = false;
//   this.selectedUser = null;
// }
// confirmDisableUser(user: UsersDTO): void {

//   const id = (user as any).authServiceId ?? user.id;

//   this.isDisabling = true;

//   this.adminService.disableUser(id).subscribe({

//     next: () => {

//       user.enabled = false;

//       this.isDisabling = false;

//       this.closeDisableDialog();

//       this.applyFilters();

//     },

//     error: err => {

//       console.error(err);

//       this.isDisabling = false;

//       alert(err?.error?.message ?? 'Failed to disable user.');

//     }

//   });

// }
