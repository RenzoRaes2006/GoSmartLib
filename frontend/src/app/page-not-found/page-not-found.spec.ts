import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PageNotFound } from './page-not-found';

describe('PageNotFound', () => {
  let component: PageNotFound;
  let fixture: ComponentFixture<PageNotFound>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [PageNotFound] }).compileComponents();
    fixture = TestBed.createComponent(PageNotFound);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('moet aangemaakt kunnen worden', () => {
    expect(component).toBeTruthy();
  });

  it('toont de 404 boodschap', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('404');
    expect(text).toContain('niet meer bestaat');
  });

  it('biedt acties terug naar homepage en catalogus', () => {
    const hrefs = Array.from(
      (fixture.nativeElement as HTMLElement).querySelectorAll('a'),
    ).map((a) => a.getAttribute('href') ?? '');
    expect(hrefs).toContain('/');
    expect(hrefs).toContain('/catalogus');
  });
});
