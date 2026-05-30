import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { KlassLeeslijstenOverzichtComponent } from './klass-leeslijsten-overzicht';
import { KlassLeeslijstService } from '../services/klassLeeslijstService';
import { LeerlingService } from '../services/Leerling.service';

describe('KlassLeeslijstenOverzichtComponent', () => {
  let component: KlassLeeslijstenOverzichtComponent;
  let fixture: ComponentFixture<KlassLeeslijstenOverzichtComponent>;
  let serviceSpy: jasmine.SpyObj<KlassLeeslijstService>;
  let leerlingSpy: jasmine.SpyObj<LeerlingService>;
  let router: Router;

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('KlassLeeslijstService', [
      'getMijnLeerkrachtLijsten',
      'createLeeslijstVoorMij',
    ]);
    serviceSpy.getMijnLeerkrachtLijsten.and.returnValue(
      of([{ id: 1, naam: 'A', boeken: [], zichtbaar: false }] as any),
    );
    serviceSpy.createLeeslijstVoorMij.and.returnValue(
      of({ id: 99, naam: 'Nieuw', boeken: [], zichtbaar: false } as any),
    );

    leerlingSpy = jasmine.createSpyObj('LeerlingService', ['getLeerlingen', 'getKlassen']);
    leerlingSpy.getKlassen.and.returnValue(of(['1a', '2a', '5INF1']));
    leerlingSpy.getLeerlingen.and.returnValue(of({ content: [{ id: 10, smartschoolId: 'jan.peeters' }] }));

    await TestBed.configureTestingModule({
      imports: [KlassLeeslijstenOverzichtComponent],
      providers: [
        provideRouter([]),
        { provide: KlassLeeslijstService, useValue: serviceSpy },
        { provide: LeerlingService, useValue: leerlingSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(KlassLeeslijstenOverzichtComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('givenKlassLeeslijstenOverzicht_whenAangemaakt_thenComponentBestaat', () => {
    expect(component).toBeTruthy();
  });

  it('givenServices_whenInit_thenLeeslijstenEnKlassenGeladen', () => {
    expect(serviceSpy.getMijnLeerkrachtLijsten).toHaveBeenCalled();
    expect(leerlingSpy.getKlassen).toHaveBeenCalled();
    expect(component.leeslijsten.length).toBe(1);
    expect(component.klassen).toEqual(['1a', '2a', '5INF1']);
  });

  it('givenModal_whenOpenNieuweLijstModal_thenModalGeopendEnVeldenGereset', () => {
    component.nieuweLijstNaam = 'oud';
    component.koppelType = 'LEERLING';

    component.openNieuweLijstModal();

    expect(component.showCreateModal).toBeTrue();
    expect(component.nieuweLijstNaam).toBe('');
    expect(component.koppelType).toBe('KLAS');
  });

  it('givenKlasType_whenBevestigNieuweLijst_thenKlasVeldMeegestuurd', () => {
    const navSpy = spyOn(router, 'navigate').and.resolveTo(true);
    component.nieuweLijstNaam = 'Klas-lijst';
    component.koppelType = 'KLAS';
    component.geselecteerdeKlas = '5INF1';
    component.nieuweLijstZichtbaar = true;

    component.bevestigNieuweLijst();

    const call = serviceSpy.createLeeslijstVoorMij.calls.mostRecent().args[0];
    expect(call.klas).toBe('5INF1');
    expect(call.leerlingId).toBeNull();
    expect(call.zichtbaar).toBeTrue();
    expect(navSpy).toHaveBeenCalledWith(['/leeslijsten', 99]);
  });

  it('givenGekozenklas_whenOnLeerlingKlasChange_thenLeerlingenVoorKlasOpgehaald', () => {
    component.leerlingKlas = '2a';
    component.onLeerlingKlasChange();

    expect(leerlingSpy.getLeerlingen).toHaveBeenCalledWith(0, 50, '2a', '');
    expect(component.leerlingResultaten.length).toBe(1);
  });

  it('givenKlasTypeZonderKlasSelectie_whenKanBevestigen_thenFalse', () => {
    component.nieuweLijstNaam = 'X';
    component.koppelType = 'KLAS';
    component.geselecteerdeKlas = '';

    expect(component.kanBevestigen).toBeFalse();
  });

  it('givenLeerlingTypeZonderLeerlingSelectie_whenKanBevestigen_thenFalse', () => {
    component.nieuweLijstNaam = 'X';
    component.koppelType = 'LEERLING';
    component.geselecteerdeLeerlingId = null;

    expect(component.kanBevestigen).toBeFalse();
  });

  it('givenLegeNaam_whenBevestigNieuweLijst_thenGeenServiceCall', () => {
    component.nieuweLijstNaam = '   ';
    component.koppelType = 'KLAS';
    component.geselecteerdeKlas = '1a';
    serviceSpy.createLeeslijstVoorMij.calls.reset();

    component.bevestigNieuweLijst();

    expect(serviceSpy.createLeeslijstVoorMij).not.toHaveBeenCalled();
  });
});
