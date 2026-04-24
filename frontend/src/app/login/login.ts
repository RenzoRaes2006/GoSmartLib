import { Component } from '@angular/core';

@Component({
  selector: 'app-login',
  imports: [],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private authBaseUrl = 'http://localhost:8081';

  loginMetSmartschool() {
    window.location.href = `${this.authBaseUrl}/oauth2/authorization/smartschool`;
  }
}
