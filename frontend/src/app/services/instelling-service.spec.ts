import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { InstellingService } from './instelling-service';

describe('InstellingService', () => {
  let service: InstellingService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [InstellingService],
    });
    service = TestBed.inject(InstellingService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getLeenLimiet stuurt GET en geeft het aantal terug', () => {
    service.getLeenLimiet().subscribe((limiet) => expect(limiet).toBe(3));

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/instellingen/leenlimiet'));
    expect(req.request.method).toBe('GET');
    req.flush(3);
  });

  it('updateLeenLimiet stuurt PUT met nieuwe waarde als body', () => {
    service.updateLeenLimiet(5).subscribe((res) => expect(res).toBe('ok'));

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/instellingen/leenlimiet'));
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toBe(5);
    req.flush('ok');
  });
});
