import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TagService } from './tag.service';
import { Tag } from '../models/Tag';

describe('TagService', () => {
  let service: TagService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TagService],
    });
    service = TestBed.inject(TagService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('list met type bouwt query-param correct', () => {
    const verwacht: Tag[] = [{ id: 1, naam: 'Fantasy', type: 'GENRE' }];
    service.list('GENRE').subscribe((tags) => expect(tags).toEqual(verwacht));
    const req = httpMock.expectOne((r) => r.url.endsWith('/api/tags') && r.params.get('type') === 'GENRE');
    expect(req.request.method).toBe('GET');
    req.flush(verwacht);
  });

  it('list zonder type roept basis-url aan', () => {
    service.list().subscribe();
    const req = httpMock.expectOne((r) => r.url.endsWith('/api/tags') && !r.params.has('type'));
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('create POST naam+type', () => {
    const nieuw: Tag = { id: 5, naam: 'Sci-Fi', type: 'GENRE' };
    service.create('Sci-Fi', 'GENRE').subscribe((t) => expect(t).toEqual(nieuw));
    const req = httpMock.expectOne((r) => r.url.endsWith('/api/tags') && r.method === 'POST');
    expect(req.request.body).toEqual({ naam: 'Sci-Fi', type: 'GENRE' });
    req.flush(nieuw);
  });

  it('rename PUT op id met nieuwe naam', () => {
    const gewijzigd: Tag = { id: 5, naam: 'Sci-Fi', type: 'GENRE' };
    service.rename(5, 'Sci-Fi').subscribe((t) => expect(t).toEqual(gewijzigd));
    const req = httpMock.expectOne((r) => r.url.endsWith('/api/tags/5') && r.method === 'PUT');
    expect(req.request.body).toEqual({ naam: 'Sci-Fi' });
    req.flush(gewijzigd);
  });

  it('delete DELETE op id', () => {
    service.delete(7).subscribe();
    const req = httpMock.expectOne((r) => r.url.endsWith('/api/tags/7') && r.method === 'DELETE');
    req.flush(null);
  });
});
