import { Component, OnInit } from '@angular/core';
import { AuthService } from '../_services/auth.service';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.css']
})
export class UserListComponent implements OnInit {
  usersList: any;
  

  constructor(private authService:AuthService) {
    this.getUsersList();
   }

  ngOnInit() {
  }

  getUsersList() {
    this.authService.getUsersList().subscribe(
      data => {
        console.log("data is",data);
        this.usersList=data;
      },
      err => {
      }
    );
  }

  deleteUser(id){
    this.authService.deleteUser(id).subscribe(
      data => {
        this.getUsersList();
      },
      err => {
      }
    );
  }
}
