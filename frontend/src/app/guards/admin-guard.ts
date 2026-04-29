import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/AuthService';

export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // // Haal de huidige ingelogde gebruiker op uit je signal
  // const user = authService.currentUser();

  // Controleer of er iemand is ingelogd EN of diegene de rol 'beheerder' heeft
  // later nog aapassen op basis van hoe je rollen precies hebt opgezet in je JWT en AuthService
  // if (user && user.role === 'beheerder') {
  //   return true; // Toegang verleend!
  // }

  // Niet ingelogd of geen beheerder? Stuur ze weg (bijv. naar de homepagina)
  router.navigate(['/']);
  return false; // Toegang geweigerd!
};