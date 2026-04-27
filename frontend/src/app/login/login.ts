import { Component } from '@angular/core';

@Component({
  selector: 'app-login',
  imports: [],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private authBaseUrl = 'https://api-sp09.cloud-ap.be';

  loginMetSmartschool() {
    window.location.href = `${this.authBaseUrl}/oauth2/authorization/smartschool`;
  }
}
