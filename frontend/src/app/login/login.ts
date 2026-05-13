import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private authBaseUrl = 'http://api-sp09.cloud-ap.be';

  loginMetSmartschool() {
    window.location.href = `${this.authBaseUrl}/oauth2/authorization/smartschool`;
  }
}