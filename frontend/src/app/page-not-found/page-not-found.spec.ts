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

  it('givenPageNotFound_whenAangemaakt_thenComponentBestaat', () => {
    expect(component).toBeTruthy();
  });

  it('givenPageNotFound_whenGerenderd_then404BoodschapZichtbaar', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('404');
    expect(text).toContain('niet meer bestaat');
  });

  it('givenPageNotFound_whenGerenderd_thenLinksNaarHomepageEnCatalogusAanwezig', () => {
    const hrefs = Array.from(
      (fixture.nativeElement as HTMLElement).querySelectorAll('a'),
    ).map((a) => a.getAttribute('href') ?? '');
    expect(hrefs).toContain('/');
    expect(hrefs).toContain('/catalogus');
  });
});
