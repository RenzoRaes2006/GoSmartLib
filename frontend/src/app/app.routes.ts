import { Routes } from '@angular/router';
import { Detail } from './detail/detail';
import { Catalogus } from './catalogus/catalogus';
import { Leeslijst } from './leeslijst/leeslijst';
import { HoofdPagina } from './hoofd-pagina/hoofd-pagina';
import { BoekToevoegen } from './boek-toevoegen/boek-toevoegen';

export const routes: Routes = [
  { path: 'catalogus', component: Catalogus },
  { path: 'details/:id', component: Detail },
  { path: 'leeslijst/:id', component: Leeslijst },
  { path: 'boek-toevoegen', component: BoekToevoegen },
  { path: '', component: HoofdPagina },
  { path: '**', redirectTo: '' },
];
