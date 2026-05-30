import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { Leeslijst } from './leeslijst';
import { LeeslijstService } from '../services/leeslijstService';

describe('Leeslijst', () => {
  let component: Leeslijst;
  let fixture: ComponentFixture<Leeslijst>;
  let serviceSpy: jasmine.SpyObj<LeeslijstService>;

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('LeeslijstService', ['getMappenVanGebruiker', 'verwijderBoekUitMap']);
    serviceSpy.getMappenVanGebruiker.and.returnValue(
      of([
        { id: 1, naam: 'School', boeken: [{ id: 10 }, { id: 11 }] },
        { id: 2, naam: 'Leeg', boeken: [] },
      ]),
    );
    serviceSpy.verwijderBoekUitMap.and.returnValue(of('OK'));

    await TestBed.configureTestingModule({
      imports: [Leeslijst],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { paramMap: of(convertToParamMap({})) } },
        { provide: LeeslijstService, useValue: serviceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Leeslijst);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('givenLeeslijst_whenAangemaakt_thenComponentBestaat', () => {
    expect(component).toBeTruthy();
  });

  it('givenLeeslijstService_whenInit_thenMappenOpgehaaldEnFavorietenToegevoegd', () => {
    expect(serviceSpy.getMappenVanGebruiker).toHaveBeenCalledWith(1);
    const namen = component.mappen.map((m) => m.naam);
    expect(namen).toContain('Favorieten');
    expect(namen).toContain('School');
    expect(namen).not.toContain('Leeg');
  });

  it('givenMapObject_whenOpenMap_thenGeselecteerdeMapGezet', () => {
    const map = { id: 1, naam: 'School' };

    component.openMap(map);

    expect(component.geselecteerdeMap).toBe(map);
  });

  it('givenBoekId_whenVerwijderBoek_thenServiceAangeropenEnBoekUitLokaleMapVerwijderd', () => {
    component.geselecteerdeMap = { id: 1, naam: 'School', boeken: [{ id: 10 }, { id: 11 }] };

    component.verwijderBoek(10, { stopPropagation: () => {} } as any);

    expect(serviceSpy.verwijderBoekUitMap).toHaveBeenCalledWith(1, 'School', 10);
    expect(component.geselecteerdeMap.boeken.find((b: any) => b.id === 10)).toBeUndefined();
  });
});
