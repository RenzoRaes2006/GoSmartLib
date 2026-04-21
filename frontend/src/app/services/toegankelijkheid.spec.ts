import { TestBed } from '@angular/core/testing';

import { ToegankelijkheidService } from './toegankelijkheid';

describe('Toegankelijkheid', () => {
  let service: ToegankelijkheidService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ToegankelijkheidService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
