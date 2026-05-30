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

  it('givenLogin_whenAangemaakt_thenComponentBestaat', () => {
    expect(component).toBeTruthy();
  });

  it('givenLogin_whenGerenderd_thenSmartschoolAanmeldknopZichtbaar', () => {
    const btn = (fixture.nativeElement as HTMLElement).querySelector('button.smartschool-btn');
    expect(btn).not.toBeNull();
    expect(btn?.textContent).toContain('Smartschool');
  });

  it('givenAanmeldknop_whenGeklikt_thenLoginMetSmartschoolAangeroepen', () => {
    const spy = spyOn(component, 'loginMetSmartschool');
    const btn = (fixture.nativeElement as HTMLElement).querySelector('button.smartschool-btn') as HTMLButtonElement;

    btn.click();

    expect(spy).toHaveBeenCalledTimes(1);
  });
});
