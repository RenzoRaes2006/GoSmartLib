import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/AuthService';

// Beschermt routes die enkel voor ingelogde gebruikers zijn.
// Als de gebruiker niet ingelogd is, wordt hij doorgestuurd naar de loginpagina.
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const user = authService.currentUser();

  if (!user) {
    router.navigate(['/login']);
    return false;
  }

  if (user.role === 'teacher' && state.url === '/') {
    router.navigate(['/leerkracht-dashboard']);
    return false;
  }

  return true;
};
