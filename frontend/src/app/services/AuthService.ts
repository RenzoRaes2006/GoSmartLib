import { Injectable, isDevMode, signal } from '@angular/core';
import { jwtDecode } from 'jwt-decode';

@Injectable({ providedIn: 'root' })
export class AuthService {
  public currentUser = signal<{
    id: string;
    name: string;
    role: string;
    klas: string;
    isBeheerder: boolean;
  } | null>(null);

  constructor() {
    if (isDevMode()) {
      const role = (localStorage.getItem('devRole') || 'BEHEERDER').toLowerCase();
      const isBeheerder = localStorage.getItem('devBeheerder') === 'true';
      this.currentUser.set({
        id: 'dev-user-001',
        name: 'Dev User',
        role,
        klas: 'DEV1',
        isBeheerder,
      });
      return;
    }

    const token = localStorage.getItem('jwtToken');
    if (token) {
      this.setUserFromToken(token);
    }
  }

  setUserFromToken(token: string) {
    const decoded: any = jwtDecode(token);
    this.currentUser.set({
      id: decoded.sub,
      name: decoded.name,
      role: decoded.role,
      klas: decoded.klas ?? 'onbekend',
      isBeheerder: decoded.isBeheerder === true,
    });
  }

  setDevRole(role: 'STUDENT' | 'TEACHER' | 'BEHEERDER' | 'ADMIN') {
    if (!isDevMode()) return;
    localStorage.setItem('devRole', role);
    window.location.reload();
  }

  setDevBeheerder(isBeheerder: boolean) {
    if (!isDevMode()) return;
    localStorage.setItem('devBeheerder', String(isBeheerder));
    window.location.reload();
  }

  logout() {
    localStorage.removeItem('jwtToken');
    this.currentUser.set(null);
  }
}
