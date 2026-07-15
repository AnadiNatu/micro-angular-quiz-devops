import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { UsersDTO } from '../../../auth/models/dtos';
import { AdminServiceService } from '../../services/admin-service.service';

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
    private adminService:AdminServiceService
  ){}

  ngOnInit():void{
    this.loadUser();
  }

  loadUser():void{
    const id=Number(this.route.snapshot.paramMap.get('id'));
    if(!id){
      this.hasError=true;
      this.errorMessage='Invalid user id.';
      this.isLoading=false;
      return;
    }

    this.adminService.getUserDetails().subscribe({
      next:user=>{
        this.user=user;
        this.isLoading=false;
      },
      error:err=>{
        console.error(err);
        this.hasError=true;
        this.errorMessage=err?.error?.message??'Unable to load user.';
        this.isLoading=false;
      }
    });
  }

  refresh():void{
    this.isLoading=true;
    this.hasError=false;
    this.loadUser();
  }

  disableUser():void{
    if(!this.user){return;}
    const id=(this.user as any).authServiceId ?? this.user.id;
    if(!confirm(`Disable ${this.user.username}?`)){return;}

    // this.adminService.disableUser(id).subscribe({
    //   next:()=>{
    //     if(this.user){
    //       this.user.enabled=false;
    //     }
    //     alert('User disabled successfully.');
    //   },
    //   error:err=>{
    //     console.error(err);
    //     alert(err?.error?.message??'Failed to disable user.');
    //   }
    // });
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

