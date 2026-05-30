import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { AdminDashboard } from './admin-dashboard';
import { SchoolService } from '../services/School.Service';

describe('AdminDashboard', () => {
  let component: AdminDashboard;
  let fixture: ComponentFixture<AdminDashboard>;
  let schoolSpy: jasmine.SpyObj<SchoolService>;

  beforeEach(async () => {
    schoolSpy = jasmine.createSpyObj('SchoolService', ['getAlleScholen', 'maakSchoolAan', 'verwijderSchool', 'getLiveSmartschoolData']);
    schoolSpy.getAlleScholen.and.returnValue(of([{ id: 1, naam: 'GO! Atheneum Antwerpen' }]));

    await TestBed.configureTestingModule({
      imports: [AdminDashboard],
      providers: [provideRouter([]), { provide: SchoolService, useValue: schoolSpy }],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminDashboard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('givenAdminDashboard_whenAangemaakt_thenComponentBestaat', () => {
    expect(component).toBeTruthy();
  });

  it('givenSchoolService_whenInit_thenGeregistreerdeScholenGeladen', () => {
    expect(schoolSpy.getAlleScholen).toHaveBeenCalled();
    expect(component.geregistreerdeScholen().length).toBe(1);
  });

  it('givenGeregistreerdeScholen_whenBeschikbareScholenOpgevraagd_thenGeregistreerdeNamenWeggefilterd', () => {
    const namen = component.beschikbareScholen().map((s) => s.naam);
    expect(namen).not.toContain('GO! Atheneum Antwerpen');
  });

  it('givenGeselecteerdeSchool_whenWisselTab_thenActieveTabGewisseldEnSchoolGesloten', () => {
    component.geselecteerdeSchool.set({ id: 1 });

    component.wisselTab('wizard');

    expect(component.activeTab()).toBe('wizard');
    expect(component.geselecteerdeSchool()).toBeNull();
  });
});
