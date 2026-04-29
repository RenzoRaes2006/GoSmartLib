import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/AuthService';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Als er een currentUser is, mag je door
  // if (authService.currentUser()) {
  //   return true;
  // }

  // Zo niet? Stuur ze terug naar de homepagina of login
  router.navigate(['/']);
  return false;
};