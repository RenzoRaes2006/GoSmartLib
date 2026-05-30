import { TestBed } from '@angular/core/testing';

import { ToegankelijkheidService } from './toegankelijkheid';

describe('ToegankelijkheidService', () => {
  let service: ToegankelijkheidService;

  beforeEach(() => {
    document.body.classList.remove('dyslexia-mode');
    TestBed.configureTestingModule({});
    service = TestBed.inject(ToegankelijkheidService);
  });

  afterEach(() => document.body.classList.remove('dyslexia-mode'));

  it('givenToegankelijkheidService_whenAangemaakt_thenDyslexieModusUitgeschakeld', () => {
    expect(service).toBeTruthy();
    expect(service.isActief).toBeFalse();
  });

  it('givenDyslexieModusUit_whenToggleDyslexieFont_thenBodyClassToegevoegd', () => {
    service.toggleDyslexieFont();

    expect(service.isActief).toBeTrue();
    expect(document.body.classList.contains('dyslexia-mode')).toBeTrue();
  });

  it('givenDyslexieModusAan_whenToggleDyslexieFontTweedeKeer_thenBodyClassVerwijderd', () => {
    service.toggleDyslexieFont();
    service.toggleDyslexieFont();

    expect(service.isActief).toBeFalse();
    expect(document.body.classList.contains('dyslexia-mode')).toBeFalse();
  });

  it('givenDyslexieModus_whenToggle_thenObservableEmitNieuweWaarde', (done) => {
    let count = 0;
    service.dyslexieModus$.subscribe((v) => {
      if (count === 1) {
        expect(v).toBeTrue();
        done();
      }
      count++;
    });
    service.toggleDyslexieFont();
  });
});
