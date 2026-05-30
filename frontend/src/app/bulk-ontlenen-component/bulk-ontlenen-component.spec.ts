import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { of } from 'rxjs';

import { BulkOntlenenComponent } from './bulk-ontlenen-component';
import { BoekService } from '../services/boek.service';

describe('BulkOntlenenComponent', () => {
  let component: BulkOntlenenComponent;
  let fixture: ComponentFixture<BulkOntlenenComponent>;
  let boekSpy: jasmine.SpyObj<BoekService>;

  beforeEach(async () => {
    boekSpy = jasmine.createSpyObj('BoekService', ['getBoekenGefilterd', 'leenBoekenBulk']);
    boekSpy.getBoekenGefilterd.and.returnValue(of({ content: [] } as any));
    boekSpy.leenBoekenBulk.and.returnValue(of('5 boeken geleend'));

    await TestBed.configureTestingModule({
      imports: [BulkOntlenenComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: BoekService, useValue: boekSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BulkOntlenenComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const http = TestBed.inject(HttpTestingController);
    http.match((r) => r.url.includes('/api/tags')).forEach((r) => r.flush([]));
  });

  it('givenBulkOntlenen_whenAangemaakt_thenComponentBestaat', () => {
    expect(component).toBeTruthy();
  });

  it('givenBoekService_whenZoekBoeken_thenAangeropenMetBeschikbaarheidTrue', () => {
    expect(boekSpy.getBoekenGefilterd).toHaveBeenCalled();
    const args = boekSpy.getBoekenGefilterd.calls.mostRecent().args[0] as any;
    expect(args.beschikbaarheid).toBe('true');
  });

  it('givenBoek_whenVoegToeAanMandje_thenBoekMetAantalEenToegevoegd', () => {
    component.voegToeAanMandje({ id: 1, aantalExemplaren: 5 } as any);

    expect(component.mandje().length).toBe(1);
    expect(component.mandje()[0].aantal).toBe(1);
  });

  it('givenBoekId_whenVerwijderUitMandje_thenBoekMetJuisteIdVerwijderd', () => {
    component.mandje.set([
      { boek: { id: 1, aantalExemplaren: 5 } as any, aantal: 1 },
      { boek: { id: 2, aantalExemplaren: 5 } as any, aantal: 1 },
    ]);

    component.verwijderUitMandje(1);

    expect(component.mandje().map((i) => i.boek.id)).toEqual([2]);
  });

  it('givenMandje_whenBevestigBulkOntlening_thenExpandedIdLijstNaarServiceGestuurd', () => {
    component.mandje.set([{ boek: { id: 7, aantalExemplaren: 5 } as any, aantal: 3 }]);

    component.bevestigBulkOntlening();

    expect(boekSpy.leenBoekenBulk).toHaveBeenCalledWith(1, [7, 7, 7]);
  });
});
