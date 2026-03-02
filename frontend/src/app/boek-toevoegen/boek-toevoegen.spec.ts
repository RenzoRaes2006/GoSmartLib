import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BoekToevoegen } from './boek-toevoegen';

describe('BoekToevoegen', () => {
  let component: BoekToevoegen;
  let fixture: ComponentFixture<BoekToevoegen>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BoekToevoegen]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BoekToevoegen);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
