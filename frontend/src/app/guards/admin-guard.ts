import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/AuthService';

// Beschermt routes die enkel voor beheerders zijn.
// TODO MORGEN: rol komt via OneRoster -> controleer hier op de juiste rolnaam
// (bv. 'administrator', 'teacher', ...) zodra je weet welke waarden OneRoster teruggeeft
export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const user = authService.currentUser();

  if (user && user.role === 'beheerder') {
    return true;
  }

  router.navigate(['/']);
  return false;
};
