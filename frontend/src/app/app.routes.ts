import { Routes } from '@angular/router';
import { Detail } from './detail/detail';
import { Catalogus } from './catalogus/catalogus';
import { Leeslijst } from './leeslijst/leeslijst';
import { BoekBeheer } from './boek-beheer/boek-beheer';
import { HomePageLeerling } from './home-page-leerling/home-page-leerling';
import { ReserverenComponent } from './reserveren/reserveren';
import { PageNotFound } from './page-not-found/page-not-found';
import { Login } from './login/login';
import { AanvragenBeheer } from './aanvragen-beheer/aanvragen-beheer';
import { AdminGuard } from './services/admin-guard';
import { AdminDashboard } from './admin-dashboard/admin-dashboard';
import { BoekImport } from './boek-import/boek-import';
import { GerapporteerdeReviews } from './gerapporteerde-reviews/gerapporteerde-reviews';
import { Leengeschiedenis } from './leengeschiedenis/leengeschiedenis';

export const routes: Routes = [
  { path: '', component: HomePageLeerling },
  { path: 'catalogus', component: Catalogus },
  { path: 'details/:id', component: Detail },
  { path: 'leeslijst/:id', component: Leeslijst },
  { path: 'boek-beheer', component: BoekBeheer },
  { path: 'reserveren', component: ReserverenComponent },
  { path: 'not-found', component: PageNotFound },
  { path: 'login/smartschool', component: Login },
  { path: 'beheer/aanvragen', component: AanvragenBeheer },
  { path: 'gerapporteerde-beoordelingen', component: GerapporteerdeReviews },
  { path: 'leengeschiedenis', component: Leengeschiedenis },
  {
    path: 'admin',
    component: AdminDashboard,
    //canActivate: [AdminGuard]
  },
  { path: 'boek-import', component: BoekImport },

  // the wildcard (**) must ALWAYS be the absolute last item in the array
  { path: '**', redirectTo: 'not-found' },
];
