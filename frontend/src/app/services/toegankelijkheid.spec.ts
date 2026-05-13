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

  it('should be created met dyslexie-modus uit', () => {
    expect(service).toBeTruthy();
    expect(service.isActief).toBeFalse();
  });

  it('toggleDyslexieFont voegt body class toe bij eerste toggle', () => {
    service.toggleDyslexieFont();

    expect(service.isActief).toBeTrue();
    expect(document.body.classList.contains('dyslexia-mode')).toBeTrue();
  });

  it('toggleDyslexieFont haalt body class weer weg bij tweede toggle', () => {
    service.toggleDyslexieFont();
    service.toggleDyslexieFont();

    expect(service.isActief).toBeFalse();
    expect(document.body.classList.contains('dyslexia-mode')).toBeFalse();
  });

  it('dyslexieModus$ emit nieuwe waarde na toggle', (done) => {
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
