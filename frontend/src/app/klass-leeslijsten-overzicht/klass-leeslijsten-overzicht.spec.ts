import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { KlassLeeslijstenOverzichtComponent } from './klass-leeslijsten-overzicht';
import { KlassLeeslijstService } from '../services/klassLeeslijstService';

describe('KlassLeeslijstenOverzichtComponent', () => {
  let component: KlassLeeslijstenOverzichtComponent;
  let fixture: ComponentFixture<KlassLeeslijstenOverzichtComponent>;
  let serviceSpy: jasmine.SpyObj<KlassLeeslijstService>;
  let router: Router;

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('KlassLeeslijstService', ['getLeeslijstenByLeerkracht', 'createLeeslijst']);
    serviceSpy.getLeeslijstenByLeerkracht.and.returnValue(of([{ id: 1, naam: 'A', boeken: [] }] as any));
    serviceSpy.createLeeslijst.and.returnValue(of({ id: 99, naam: 'Nieuw' } as any));

    await TestBed.configureTestingModule({
      imports: [KlassLeeslijstenOverzichtComponent],
      providers: [provideRouter([]), { provide: KlassLeeslijstService, useValue: serviceSpy }],
    }).compileComponents();

    fixture = TestBed.createComponent(KlassLeeslijstenOverzichtComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('moet aangemaakt kunnen worden', () => {
    expect(component).toBeTruthy();
  });

  it('laadt leeslijsten van leerkracht via service bij init', () => {
    expect(serviceSpy.getLeeslijstenByLeerkracht).toHaveBeenCalledWith(1);
    expect(component.leeslijsten.length).toBe(1);
  });

  it('openNieuweLijstModal opent modal en wist nieuwe naam', () => {
    component.nieuweLijstNaam = 'oud';

    component.openNieuweLijstModal();

    expect(component.showCreateModal).toBeTrue();
    expect(component.nieuweLijstNaam).toBe('');
  });

  it('bevestigNieuweLijst stuurt POST en navigeert naar nieuwe lijst', () => {
    const navSpy = spyOn(router, 'navigate').and.resolveTo(true);
    component.nieuweLijstNaam = 'Mijn lijst';

    component.bevestigNieuweLijst();

    expect(serviceSpy.createLeeslijst).toHaveBeenCalledWith(1, { naam: 'Mijn lijst' });
    expect(navSpy).toHaveBeenCalledWith(['/leeslijsten', 99]);
    expect(component.showCreateModal).toBeFalse();
  });

  it('bevestigNieuweLijst doet niets bij lege naam', () => {
    component.nieuweLijstNaam = '   ';
    serviceSpy.createLeeslijst.calls.reset();

    component.bevestigNieuweLijst();

    expect(serviceSpy.createLeeslijst).not.toHaveBeenCalled();
  });
});
