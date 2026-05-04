import { TestBed } from '@angular/core/testing';

import { Leeslijst } from './leeslijst';

describe('Leeslijst', () => {
  let service: Leeslijst;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Leeslijst);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
