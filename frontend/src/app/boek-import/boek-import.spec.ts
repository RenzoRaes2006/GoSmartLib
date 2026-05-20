import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { BoekImport } from './boek-import';
import { BoekService } from '../services/boek.service';

describe('BoekImport', () => {
  let component: BoekImport;
  let fixture: ComponentFixture<BoekImport>;
  let boekSpy: jasmine.SpyObj<BoekService>;

  beforeEach(async () => {
    boekSpy = jasmine.createSpyObj('BoekService', ['uploadVoorPreview', 'bevestigImport', 'uploadAfbeelding']);
    boekSpy.bevestigImport.and.returnValue(of('Succesvol geïmporteerd'));

    await TestBed.configureTestingModule({
      imports: [BoekImport],
      providers: [provideRouter([]), { provide: BoekService, useValue: boekSpy }],
    }).compileComponents();

    fixture = TestBed.createComponent(BoekImport);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('moet aangemaakt kunnen worden', () => {
    expect(component).toBeTruthy();
  });

  it('verwijderUitPreview haalt boek uit lijst en toont succesmelding', () => {
    component.previewLijst.set([{ titel: 'A' }, { titel: 'B' }] as any);

    component.verwijderUitPreview(0, 'A');

    expect(component.previewLijst().length).toBe(1);
    expect(component.successMessage()).toContain('A');
  });

  it('bevestigImport stuurt previewLijst door naar BoekService.bevestigImport', () => {
    component.previewLijst.set([{ titel: 'X' }] as any);

    component.bevestigImport();

    expect(boekSpy.bevestigImport).toHaveBeenCalledWith([{ titel: 'X' }] as any);
    expect(component.previewLijst().length).toBe(0);
  });
});
