import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class LeerlingService {

  // mock data to simulate the smartschool api response
  private mockStudents = [
    { 
      id: 1, 
      firstName: 'Amira', 
      lastName: 'El Idrissi', 
      className: '2a', 
      activeLoans: 4, 
      history: [
        { titel: 'Oorlogswinter', datum: '2026-04-20', status: 'ingeleverd' }, 
        { titel: 'Spijt', datum: '2026-05-01', status: 'actief' }
      ] 
    },
    { 
      id: 2, 
      firstName: 'Amira', 
      lastName: 'Sels', 
      className: '2b', 
      activeLoans: 1, 
      history: [
        { titel: 'Het gouden ei', datum: '2026-04-25', status: 'te laat' }
      ] 
    },
    { 
      id: 3, 
      firstName: 'Bram', 
      lastName: 'Peeters', 
      className: '2a', 
      activeLoans: 0, 
      history: [] 
    },
    { 
      id: 4, 
      firstName: 'Céline', 
      lastName: 'Dupont', 
      className: '1a', 
      activeLoans: 2, 
      history: [
        { titel: 'Kruistocht in spijkerbroek', datum: '2026-04-10', status: 'ingeleverd' }
      ] 
    },
    { id: 5, firstName: 'Daan', lastName: 'Janssens', className: '2a', activeLoans: 1, history: [{ titel: 'Matilda', datum: '2026-04-15', status: 'ingeleverd' }] },
    { id: 6, firstName: 'Eva', lastName: 'De Smet', className: '2a', activeLoans: 3, history: [] },
    { id: 7, firstName: 'Fatih', lastName: 'Yilmaz', className: '2a', activeLoans: 0, history: [] },
    { id: 8, firstName: 'Gitte', lastName: 'Vermeulen', className: '2a', activeLoans: 2, history: [] },
    
    // extra mock data to test pagination
    { id: 9, firstName: 'Lotte', lastName: 'De Vos', className: '1a', activeLoans: 0, history: [] },
    { id: 10, firstName: 'Noah', lastName: 'Maes', className: '1b', activeLoans: 2, history: [{ titel: 'Koning van Katoren', datum: '2026-04-12', status: 'ingeleverd' }] },
    { id: 11, firstName: 'Lars', lastName: 'Peeters', className: '3a', activeLoans: 1, history: [{ titel: 'De Hobbit', datum: '2026-04-20', status: 'actief' }] },
    { id: 12, firstName: 'Mila', lastName: 'Claes', className: '2b', activeLoans: 0, history: [] },
    { id: 13, firstName: 'Finn', lastName: 'Willems', className: '1a', activeLoans: 3, history: [{ titel: 'Dolfje Weerwolfje', datum: '2026-03-15', status: 'ingeleverd' }, { titel: 'Harry Potter 1', datum: '2026-04-26', status: 'actief' }] },
    { id: 14, firstName: 'Elena', lastName: 'Desmet', className: '3a', activeLoans: 0, history: [{ titel: 'Twilight', datum: '2026-02-10', status: 'ingeleverd' }] },
    { id: 15, firstName: 'Arthur', lastName: 'Goossens', className: '2a', activeLoans: 1, history: [{ titel: 'Percy Jackson', datum: '2026-04-22', status: 'actief' }] },
    { id: 16, firstName: 'Emma', lastName: 'Wouters', className: '1b', activeLoans: 0, history: [] },
    { id: 17, firstName: 'Lucas', lastName: 'Mertens', className: '2b', activeLoans: 2, history: [{ titel: 'De Grijze Jager', datum: '2026-04-05', status: 'ingeleverd' }] },
    { id: 18, firstName: 'Olivia', lastName: 'Jacobs', className: '3a', activeLoans: 0, history: [] }
  ];

  // fetch students as an observable, ready to be replaced by http.get later
  getLeerlingen(): Observable<any[]> {
    // using delay to simulate network loading time
    return of(this.mockStudents).pipe(delay(400));
  }
}