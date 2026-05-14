import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/AuthService';

// Beschermt routes die enkel voor de applicatiebeheerder ('admin') zijn.
// Admins loggen in via het aparte loginscherm (niet via Smartschool) en krijgen
// in hun JWT-token de rol 'admin'.
export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const user = authService.currentUser();

  if (user && user.role === 'admin') {
    return true;
  }

  router.navigate(['/']);
  return false;
};
