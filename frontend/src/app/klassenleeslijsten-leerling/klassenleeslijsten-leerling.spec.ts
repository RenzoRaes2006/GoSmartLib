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
    serviceSpy = jasmine.createSpyObj('KlassLeeslijstService', ['getAllKlassenLeeslijsten']);
    serviceSpy.getAllKlassenLeeslijsten.and.returnValue(
      of([
        { id: 1, naam: 'A', boeken: [] },
        { id: 2, naam: 'B', boeken: [] },
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

  it('moet aangemaakt kunnen worden', () => {
    expect(component).toBeTruthy();
  });

  it('haalt alle klassenleeslijsten op via de service bij init', () => {
    expect(serviceSpy.getAllKlassenLeeslijsten).toHaveBeenCalledTimes(1);
    expect(component.leeslijsten.length).toBe(2);
  });

  it('rendert de namen van de leeslijsten in de template', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('A');
    expect(text).toContain('B');
  });
});
