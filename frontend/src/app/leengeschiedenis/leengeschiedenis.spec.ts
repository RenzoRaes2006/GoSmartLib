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

  it('moet aangemaakt kunnen worden', () => {
    expect(component).toBeTruthy();
  });

  it('haalt geschiedenis op voor de gebruiker en filtert geweigerde leningen weg', () => {
    expect(boekSpy.getLeengeschiedenis).toHaveBeenCalledWith(1);
    expect(component.alleLeningen().length).toBe(1);
    expect(component.alleLeningen()[0].status).toBe('GOEDGEKEURD');
  });

  it('berekenSchooljaren voegt huidig schooljaar toe en selecteert het nieuwste', () => {
    expect(component.beschikbareSchooljaren().length).toBeGreaterThan(0);
    expect(component.geselecteerdSchooljaar()).toBe(component.beschikbareSchooljaren()[0]);
  });

  it('dataPerMaand levert 12 maanden, ook bij weinig data', () => {
    expect(component.dataPerMaand().length).toBe(12);
    expect(component.dataPerMaand()[0].naam).toBe('September');
  });
});
