import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';

import { BeheerderLogin } from './beheerder-login';
import { AuthService } from '../services/AuthService';

describe('BeheerderLogin', () => {
  let component: BeheerderLogin;
  let fixture: ComponentFixture<BeheerderLogin>;
  let httpMock: HttpTestingController;
  let authSpy: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    authSpy = jasmine.createSpyObj('AuthService', ['setUserFromToken', 'currentUser', 'logout']);

    await TestBed.configureTestingModule({
      imports: [BeheerderLogin, HttpClientTestingModule],
      providers: [provideRouter([]), { provide: AuthService, useValue: authSpy }],
    }).compileComponents();

    fixture = TestBed.createComponent(BeheerderLogin);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  afterEach(() => {
    localStorage.removeItem('jwtToken');
    httpMock.verify();
  });

  it('moet aangemaakt kunnen worden', () => {
    expect(component).toBeTruthy();
  });

  it('login() POST naar /api/auth/admin/login en navigeert bij succes naar /admin', () => {
    const navSpy = spyOn(router, 'navigate').and.resolveTo(true);
    component.gebruikersnaam = 'admin';
    component.wachtwoord = 'geheim';

    component.login();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/auth/admin/login'));
    expect(req.request.method).toBe('POST');
    expect((req.request.body as any).gebruikersnaam).toBe('admin');
    req.flush({ token: 'jwt' });

    expect(authSpy.setUserFromToken).toHaveBeenCalledWith('jwt');
    expect(navSpy).toHaveBeenCalledWith(['/admin']);
  });

  it('login() zet foutmelding bij HTTP-fout', () => {
    component.login();
    const req = httpMock.expectOne((r) => r.url.endsWith('/api/auth/admin/login'));
    req.flush('nope', { status: 401, statusText: 'Unauthorized' });

    expect(component.foutmelding).toContain('Ongeldig');
    expect(component.bezig).toBeFalse();
  });
});
