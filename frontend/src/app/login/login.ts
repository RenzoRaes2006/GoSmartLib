import { Component } from '@angular/core';

@Component({
  selector: 'app-login',
  imports: [],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private authBaseUrl =
    window.location.hostname === 'localhost' ? 'http://localhost:8081' : window.location.origin;

  loginMetSmartschool() {
    window.location.href = `${this.authBaseUrl}/oauth2/authorization/smartschool`;
  }
}
