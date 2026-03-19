import { TestBed } from '@angular/core/testing';

import { Toegankelijkheid } from './toegankelijkheid';

describe('Toegankelijkheid', () => {
  let service: Toegankelijkheid;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Toegankelijkheid);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
