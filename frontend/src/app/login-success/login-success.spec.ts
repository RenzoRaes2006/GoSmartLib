import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { LoginSuccessComponent } from './login-success';
import { AuthService } from '../services/AuthService';

describe('LoginSuccessComponent', () => {
  let component: LoginSuccessComponent;
  let fixture: ComponentFixture<LoginSuccessComponent>;
  let authSpy: jasmine.SpyObj<AuthService>;
  let router: Router;

  function setup(queryParams: Record<string, string>) {
    authSpy = jasmine.createSpyObj('AuthService', ['setUserFromToken', 'currentUser', 'logout']);
    authSpy.currentUser.and.returnValue(null);

    TestBed.configureTestingModule({
      imports: [LoginSuccessComponent],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { queryParams: of(queryParams) } },
        { provide: AuthService, useValue: authSpy },
      ],
    });

    fixture = TestBed.createComponent(LoginSuccessComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
  }

  it('givenGeenToken_whenInit_thenNavigeertNaarLogin', () => {
    setup({});
    const navSpy = spyOn(router, 'navigate').and.resolveTo(true);

    fixture.detectChanges();

    expect(navSpy).toHaveBeenCalledWith(['/login']);
    expect(authSpy.setUserFromToken).not.toHaveBeenCalled();
  });

  it('givenToken_whenInit_thenTokenOpsgeslagenEnSetUserFromTokenAangeroepen', () => {
    localStorage.removeItem('jwtToken');
    setup({ token: 'abc.def.ghi' });
    spyOn(router, 'navigate').and.resolveTo(true);

    fixture.detectChanges();

    expect(localStorage.getItem('jwtToken')).toBe('abc.def.ghi');
    expect(authSpy.setUserFromToken).toHaveBeenCalledWith('abc.def.ghi');
    localStorage.removeItem('jwtToken');
  });

  it('givenTeacherRol_whenInit_thenNavigeertNaarLeerkrachtDashboard', () => {
    setup({ token: 'tok' });
    authSpy.currentUser.and.returnValue({ id: '1', name: 'X', role: 'teacher', klas: '5A' });
    const navSpy = spyOn(router, 'navigate').and.resolveTo(true);

    fixture.detectChanges();

    expect(navSpy).toHaveBeenCalledWith(['/leerkracht-dashboard']);
    localStorage.removeItem('jwtToken');
  });
});
