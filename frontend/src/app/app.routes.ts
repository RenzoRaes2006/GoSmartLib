import { Routes } from '@angular/router';
import { Detail } from './detail/detail';
import { Catalogus } from './catalogus/catalogus';
import { Leeslijst } from './leeslijst/leeslijst';
import { HoofdPagina } from './hoofd-pagina/hoofd-pagina';

export const routes: Routes = [
    { path: 'catalogus', component: Catalogus },
    //aangekomen van Mohamed, ik heb de path aangepast van isbn naar id omdat dat logischer is voor een RESTful API en ook omdat we in de backend werken met een id veld als primary key
    { path: 'details/:id', component: Detail },
    { path: 'leeslijst', component: Leeslijst },
    { path: '', component: HoofdPagina },
    {path: '**', redirectTo: ''}
];
