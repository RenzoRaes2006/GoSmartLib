import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { KlassenleeslijstenLeerlingComponent } from './klassenleeslijsten-leerling';
import { KlassLeeslijstService } from '../services/klassLeeslijstService';

describe('KlassenleeslijstenLeerlingComponent', () => {
  let component: KlassenleeslijstenLeerlingComponent;
  let fixture: ComponentFixture<KlassenleeslijstenLeerlingComponent>;
  let serviceSpy: jasmine.SpyObj<KlassLeeslijstService>;

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('KlassLeeslijstService', ['getLeeslijstenVoorMij']);
    serviceSpy.getLeeslijstenVoorMij.and.returnValue(
      of([
        { id: 1, naam: 'Klas A', klas: '5INF1', boeken: [], zichtbaar: true },
        { id: 2, naam: 'Persoonlijk', leerlingId: 42, leerlingNaam: 'l42', boeken: [], zichtbaar: true },
      ] as any),
    );

    await TestBed.configureTestingModule({
      imports: [KlassenleeslijstenLeerlingComponent],
      providers: [provideRouter([]), { provide: KlassLeeslijstService, useValue: serviceSpy }],
    }).compileComponents();

    fixture = TestBed.createComponent(KlassenleeslijstenLeerlingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('givenKlassenleeslijstenLeerling_whenAangemaakt_thenComponentBestaat', () => {
    expect(component).toBeTruthy();
  });

  it('givenKlassLeeslijstService_whenInit_thenLeeslijstenVoorMijOpgehaald', () => {
    expect(serviceSpy.getLeeslijstenVoorMij).toHaveBeenCalledTimes(1);
    expect(component.leeslijsten().length).toBe(2);
  });

  it('givenGemengdeLeeslijsten_whenInit_thenKlasEnPersoonlijkeLijstenCorrectGesplitst', () => {
    expect(component.klasLijsten().length).toBe(1);
    expect(component.klasLijsten()[0].klas).toBe('5INF1');
    expect(component.persoonlijkeLijsten().length).toBe(1);
    expect(component.persoonlijkeLijsten()[0].leerlingId).toBe(42);
  });

  it('givenLeeslijsten_whenGerenderd_thenNamenVanKlasEnPersoonlijkeLijstenZichtbaar', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Klas A');
    expect(text).toContain('Persoonlijk');
    expect(text).toContain('5INF1');
  });
});
