import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Login } from './login';

describe('Login', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('moet aangemaakt kunnen worden', () => {
    expect(component).toBeTruthy();
  });

  it('rendert de Smartschool-aanmeldknop', () => {
    const btn = (fixture.nativeElement as HTMLElement).querySelector('button.smartschool-btn');
    expect(btn).not.toBeNull();
    expect(btn?.textContent).toContain('Smartschool');
  });

  it('klik op aanmeldknop roept loginMetSmartschool aan', () => {
    const spy = spyOn(component, 'loginMetSmartschool');
    const btn = (fixture.nativeElement as HTMLElement).querySelector('button.smartschool-btn') as HTMLButtonElement;

    btn.click();

    expect(spy).toHaveBeenCalledTimes(1);
  });
});
