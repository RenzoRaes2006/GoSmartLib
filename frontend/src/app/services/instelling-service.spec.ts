import { TestBed } from '@angular/core/testing';

import { InstellingService } from './instelling-service';

describe('InstellingService', () => {
  let service: InstellingService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(InstellingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
