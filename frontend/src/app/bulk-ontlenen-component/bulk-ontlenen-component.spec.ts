import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BulkOntlenenComponent } from './bulk-ontlenen-component';

describe('BulkOntlenenComponent', () => {
  let component: BulkOntlenenComponent;
  let fixture: ComponentFixture<BulkOntlenenComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BulkOntlenenComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BulkOntlenenComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
