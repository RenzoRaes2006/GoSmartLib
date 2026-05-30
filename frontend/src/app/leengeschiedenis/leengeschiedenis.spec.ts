import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { Leengeschiedenis } from './leengeschiedenis';
import { BoekService } from '../services/boek.service';

describe('Leengeschiedenis', () => {
  let component: Leengeschiedenis;
  let fixture: ComponentFixture<Leengeschiedenis>;
  let boekSpy: jasmine.SpyObj<BoekService>;

  beforeEach(async () => {
    boekSpy = jasmine.createSpyObj('BoekService', ['getLeengeschiedenis']);
    boekSpy.getLeengeschiedenis.and.returnValue(
      of([
        { id: 1, status: 'GOEDGEKEURD', uitleenDatum: '2024-09-15' },
        { id: 2, status: 'GEWEIGERD', uitleenDatum: '2024-10-01' },
      ]),
    );

    await TestBed.configureTestingModule({
      imports: [Leengeschiedenis],
      providers: [provideRouter([]), { provide: BoekService, useValue: boekSpy }],
    }).compileComponents();

    fixture = TestBed.createComponent(Leengeschiedenis);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('givenLeengeschiedenis_whenAangemaakt_thenComponentBestaat', () => {
    expect(component).toBeTruthy();
  });

  it('givenBoekService_whenInit_thenGeschiedenisOpgehaaldEnGeweigerdGefilterd', () => {
    expect(boekSpy.getLeengeschiedenis).toHaveBeenCalledWith(1);
    expect(component.alleLeningen().length).toBe(1);
    expect(component.alleLeningen()[0].status).toBe('GOEDGEKEURD');
  });

  it('givenHuidigDatum_whenBerekenSchooljaren_thenHuidigSchooljaarToegevoegdEnGeselecteerd', () => {
    expect(component.beschikbareSchooljaren().length).toBeGreaterThan(0);
    expect(component.geselecteerdSchooljaar()).toBe(component.beschikbareSchooljaren()[0]);
  });

  it('givenLeendata_whenDataPerMaand_thenTwaalfMaandenTeruggegeven', () => {
    expect(component.dataPerMaand().length).toBe(12);
    expect(component.dataPerMaand()[0].naam).toBe('September');
  });
});
