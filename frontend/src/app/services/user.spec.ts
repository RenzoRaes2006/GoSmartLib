import { TestBed } from '@angular/core/testing';

import { GebruikerService } from './GebruikerService';

describe('User', () => {
  let service: GebruikerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(GebruikerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
