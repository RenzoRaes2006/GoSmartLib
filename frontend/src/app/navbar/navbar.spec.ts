import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { Navbar } from './navbar';
import { ToegankelijkheidService } from '../services/toegankelijkheid';
import { AuthService } from '../services/AuthService';

describe('Navbar', () => {
  let component: Navbar;
  let fixture: ComponentFixture<Navbar>;
  let toegankelijkheidSpy: jasmine.SpyObj<ToegankelijkheidService>;
  let authSpy: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    toegankelijkheidSpy = jasmine.createSpyObj('ToegankelijkheidService', ['toggleDyslexieFont'], {
      dyslexieModus$: { subscribe: (cb: (v: boolean) => void) => cb(false) } as any,
    });
    authSpy = jasmine.createSpyObj('AuthService', ['logout', 'currentUser']);
    authSpy.currentUser.and.returnValue(null);

    await TestBed.configureTestingModule({
      imports: [Navbar],
      providers: [
        provideRouter([]),
        { provide: ToegankelijkheidService, useValue: toegankelijkheidSpy },
        { provide: AuthService, useValue: authSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Navbar);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('moet aangemaakt kunnen worden', () => {
    expect(component).toBeTruthy();
  });

  it('toggleDyslexie roept ToegankelijkheidService.toggleDyslexieFont aan', () => {
    component.toggleDyslexie();

    expect(toegankelijkheidSpy.toggleDyslexieFont).toHaveBeenCalledTimes(1);
  });

  it('logout roept AuthService.logout aan en navigeert naar /login', () => {
    const navSpy = spyOn(router, 'navigate').and.resolveTo(true);

    component.logout();

    expect(authSpy.logout).toHaveBeenCalledTimes(1);
    expect(navSpy).toHaveBeenCalledWith(['/login']);
  });

  it('homeRoute geeft / terug voor niet-leerkracht', () => {
    authSpy.currentUser.and.returnValue({ id: '1', name: 'X', role: 'student', klas: '1A' });
    expect(component.homeRoute).toBe('/');
  });
});
