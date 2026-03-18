import { Routes } from '@angular/router';
import { Detail } from './detail/detail';
import { Catalogus } from './catalogus/catalogus';
import { Leeslijst } from './leeslijst/leeslijst';
import { HoofdPagina } from './hoofd-pagina/hoofd-pagina';
import { BoekBeheer } from './boek-beheer/boek-beheer';
import { ReserverenComponent } from './reserveren/reserveren';

export const routes: Routes = [
  { path: 'catalogus', component: Catalogus },
  { path: 'details/:id', component: Detail },
  { path: 'leeslijst/:id', component: Leeslijst },
  { path: 'boek-beheer', component: BoekBeheer },
  { path: 'reserveren', component: ReserverenComponent },
  { path: '', component: HoofdPagina },
  { path: '**', redirectTo: '' },
];
