import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/AuthService';

// Beschermt routes die enkel voor de bibliotheekbeheerder ('beheerder') zijn.
// Bibliotheekbeheerders loggen in via Smartschool en krijgen in hun JWT-token
// de rol 'beheerder'.
export const beheerderGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const user = authService.currentUser();

  if (user && user.role === 'beheerder') {
    return true;
  }

  router.navigate(['/']);
  return false;
};
