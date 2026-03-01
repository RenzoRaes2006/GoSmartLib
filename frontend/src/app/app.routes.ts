import { Routes } from '@angular/router';
import { Detail } from './detail/detail';
import { Catalogus } from './catalogus/catalogus';
import { Leeslijst } from './leeslijst/leeslijst';
import { HoofdPagina } from './hoofd-pagina/hoofd-pagina';

export const routes: Routes = [
    { path: 'catalogus', component: Catalogus },
    { path: 'details/:title', component: Detail },
    { path: 'leeslijst', component: Leeslijst },
    { path: '', component: HoofdPagina },
    {path: '**', redirectTo: ''}
];
