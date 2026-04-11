import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BoekImport } from './boek-import';

describe('BoekImport', () => {
  let component: BoekImport;
  let fixture: ComponentFixture<BoekImport>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BoekImport]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BoekImport);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
