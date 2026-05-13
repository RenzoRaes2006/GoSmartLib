import { TestBed } from '@angular/core/testing';

import { AuthService } from './AuthService';

describe('AuthService', () => {
  let service: AuthService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuthService);
  });

  afterEach(() => localStorage.clear());

  it('should be created met currentUser null als er geen token in localStorage staat', () => {
    expect(service).toBeTruthy();
    expect(service.currentUser()).toBeNull();
  });

  it('setUserFromToken decodeert JWT en zet currentUser', () => {
    // payload: {"sub":"123","name":"Test","role":"student","klas":"2SOF"} (base64url)
    const fakeJwt =
      'eyJhbGciOiJIUzI1NiJ9.' +
      'eyJzdWIiOiIxMjMiLCJuYW1lIjoiVGVzdCIsInJvbGUiOiJzdHVkZW50Iiwia2xhcyI6IjJTT0YifQ.' +
      'sig';

    service.setUserFromToken(fakeJwt);

    const user = service.currentUser();
    expect(user).not.toBeNull();
    expect(user!.id).toBe('123');
    expect(user!.name).toBe('Test');
    expect(user!.role).toBe('student');
    expect(user!.klas).toBe('2SOF');
  });

  it('logout verwijdert token uit localStorage en zet currentUser op null', () => {
    localStorage.setItem('jwtToken', 'iets');
    service.currentUser.set({ id: '1', name: 'X', role: 'student', klas: '1A' });

    service.logout();

    expect(service.currentUser()).toBeNull();
    expect(localStorage.getItem('jwtToken')).toBeNull();
  });
});
