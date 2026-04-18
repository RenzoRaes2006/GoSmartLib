import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReserverenComponent } from './reserveren';

describe('Reserveren', () => {
  let component: ReserverenComponent;
  let fixture: ComponentFixture<ReserverenComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReserverenComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReserverenComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
