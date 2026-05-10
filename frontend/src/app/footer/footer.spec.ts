import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Footer } from './footer';

describe('Footer', () => {
  let component: Footer;
  let fixture: ComponentFixture<Footer>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Footer],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Footer);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('moet aangemaakt kunnen worden', () => {
    expect(component).toBeTruthy();
  });

  it('rendert contact-info in de footer', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('GO! Scholengroep Antwerpen');
    expect(text).toContain('Contact');
  });

  it('toont een link naar /boek-beheer', () => {
    const links = (fixture.nativeElement as HTMLElement).querySelectorAll('a');
    const hrefs = Array.from(links).map((a) => a.getAttribute('href') ?? '');
    expect(hrefs.some((h) => h.includes('boek-beheer'))).toBeTrue();
  });
});
