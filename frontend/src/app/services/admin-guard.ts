import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class AdminGuard implements CanActivate {
  constructor(private router: Router) {}

  canActivate(): boolean {
    //TODO: Smartschool logica gebruiken
    const isBeheerder = true;
    if (!isBeheerder) {
      return true;
    } else {
      this.router.navigate(['/catalogus']);
      return false;
    }
  }
}
