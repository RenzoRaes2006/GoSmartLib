import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { BeheerderDashboard } from './beheerder-dashboard';
import { SchoolService } from '../services/School.Service';

describe('BeheerderDashboard', () => {
  let component: BeheerderDashboard;
  let fixture: ComponentFixture<BeheerderDashboard>;
  let schoolSpy: jasmine.SpyObj<SchoolService>;

  beforeEach(async () => {
    schoolSpy = jasmine.createSpyObj('SchoolService', ['getAlleScholen', 'maakSchoolAan', 'verwijderSchool', 'getLiveSmartschoolData']);
    schoolSpy.getAlleScholen.and.returnValue(of([{ id: 1, naam: 'GO! Atheneum Antwerpen' }]));

    await TestBed.configureTestingModule({
      imports: [BeheerderDashboard],
      providers: [{ provide: SchoolService, useValue: schoolSpy }],
    }).compileComponents();

    fixture = TestBed.createComponent(BeheerderDashboard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('moet aangemaakt kunnen worden', () => {
    expect(component).toBeTruthy();
  });

  it('laadt geregistreerde scholen via de service bij init', () => {
    expect(schoolSpy.getAlleScholen).toHaveBeenCalled();
    expect(component.geregistreerdeScholen().length).toBe(1);
  });

  it('beschikbareScholen filtert reeds-geregistreerde namen weg', () => {
    const namen = component.beschikbareScholen().map((s) => s.naam);
    expect(namen).not.toContain('GO! Atheneum Antwerpen');
  });

  it('wisselTab wisselt actieve tab en sluit school-details', () => {
    component.geselecteerdeSchool.set({ id: 1 });

    component.wisselTab('wizard');

    expect(component.activeTab()).toBe('wizard');
    expect(component.geselecteerdeSchool()).toBeNull();
  });
});
