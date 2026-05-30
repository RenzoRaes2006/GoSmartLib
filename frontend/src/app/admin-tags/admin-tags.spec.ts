import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AdminTags } from './admin-tags';
import { Tag } from '../models/Tag';

describe('AdminTags', () => {
  let component: AdminTags;
  let fixture: ComponentFixture<AdminTags>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminTags, HttpClientTestingModule],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminTags);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  function antwoordOpListGenre(tags: Tag[]) {
    const req = httpMock.expectOne((r) => r.url.endsWith('/api/tags') && r.params.get('type') === 'GENRE');
    req.flush(tags);
  }

  it('givenAdminTags_whenInit_thenTagsVoorActieveTabGeladen', () => {
    fixture.detectChanges();
    antwoordOpListGenre([{ id: 1, naam: 'Fantasy', type: 'GENRE' }]);
    expect(component.tags().length).toBe(1);
    expect(component.tags()[0].naam).toBe('Fantasy');
  });

  it('givenNieuwType_whenWisselTab_thenTagsVoorNieuwTypeHerladen', () => {
    fixture.detectChanges();
    antwoordOpListGenre([]);

    component.wisselTab('THEMATISCH');
    const req = httpMock.expectOne((r) => r.params.get('type') === 'THEMATISCH');
    req.flush([{ id: 2, naam: 'Vriendschap', type: 'THEMATISCH' }]);
    expect(component.activeTab()).toBe('THEMATISCH');
    expect(component.tags()[0].naam).toBe('Vriendschap');
  });

  it('givenNieuweTagNaam_whenToevoegen_thenPostTagEnHerladen', () => {
    fixture.detectChanges();
    antwoordOpListGenre([]);

    component.nieuweNaam = 'Sci-Fi';
    component.toevoegen();

    const post = httpMock.expectOne((r) => r.method === 'POST' && r.url.endsWith('/api/tags'));
    expect(post.request.body).toEqual({ naam: 'Sci-Fi', type: 'GENRE' });
    post.flush({ id: 9, naam: 'Sci-Fi', type: 'GENRE' });

    antwoordOpListGenre([{ id: 9, naam: 'Sci-Fi', type: 'GENRE' }]);
    expect(component.nieuweNaam).toBe('');
    expect(component.tags()[0].naam).toBe('Sci-Fi');
  });

  it('givenTagInGebruik_whenVerwijderen_then409FoutmeldingGetoond', () => {
    fixture.detectChanges();
    antwoordOpListGenre([{ id: 3, naam: 'Horror', type: 'GENRE' }]);

    spyOn(window, 'confirm').and.returnValue(true);
    component.verwijderen({ id: 3, naam: 'Horror', type: 'GENRE' });

    const del = httpMock.expectOne((r) => r.method === 'DELETE' && r.url.endsWith('/api/tags/3'));
    del.flush("Tag 'Horror' kan niet verwijderd worden: gebruikt door 2 boek(en).",
      { status: 409, statusText: 'Conflict' });

    expect(component.fout()).toContain('gebruikt door 2');
  });

  it('givenGewijzigdeNaam_whenBewaarBewerken_thenPutVerstuurdEnHerladen', () => {
    fixture.detectChanges();
    antwoordOpListGenre([{ id: 4, naam: 'Avontuur', type: 'GENRE' }]);

    const tag: Tag = { id: 4, naam: 'Avontuur', type: 'GENRE' };
    component.startBewerken(tag);
    component.bewerktNaam = 'Avontuur & ontdekking';
    component.bewaarBewerken(tag);

    const put = httpMock.expectOne((r) => r.method === 'PUT' && r.url.endsWith('/api/tags/4'));
    expect(put.request.body).toEqual({ naam: 'Avontuur & ontdekking' });
    put.flush({ id: 4, naam: 'Avontuur & ontdekking', type: 'GENRE' });

    antwoordOpListGenre([{ id: 4, naam: 'Avontuur & ontdekking', type: 'GENRE' }]);
    expect(component.bewerktId).toBeNull();
  });
});
