import { Routes } from '@angular/router';
import { Detail } from './detail/detail';
import { Catalogus } from './catalogus/catalogus';
import { Leeslijst } from './leeslijst/leeslijst';
import { BoekBeheer } from './boek-beheer/boek-beheer';
import { HomePage } from './home-page/home-page';
import { ReserverenComponent } from './reserveren/reserveren';
import { PageNotFound } from './page-not-found/page-not-found';
import { Login } from './login/login';
import { AanvragenBeheer } from './aanvragen-beheer/aanvragen-beheer';
import { AdminDashboard } from './admin-dashboard/admin-dashboard';
import { BoekImport } from './boek-import/boek-import';
import { GerapporteerdeReviews } from './gerapporteerde-reviews/gerapporteerde-reviews';
import { Leengeschiedenis } from './leengeschiedenis/leengeschiedenis';
import { LoginSuccessComponent } from './login-success/login-success';
import { adminGuard } from './guards/admin-guard';
import { authGuard } from './guards/auth.guard';

// Guards voor later
// import { AdminGuard } from './guards/admin-guard';
// import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  // { path: '', component: HomePage },
  // { path: 'catalogus', component: Catalogus },
  // { path: 'login-success', component: LoginSuccessComponent },
  // { path: 'details/:id', component: Detail },
  // { path: 'leeslijst/:id', component: Leeslijst },
  // { path: 'boek-beheer', component: BoekBeheer },
  // { path: 'reserveren', component: ReserverenComponent },
  // { path: 'not-found', component: PageNotFound },
  // { path: 'login', component: Login },
  // { path: 'beheer/aanvragen', component: AanvragenBeheer },
  // { path: 'gerapporteerde-beoordelingen', component: GerapporteerdeReviews },
  // { path: 'leengeschiedenis', component: Leengeschiedenis },
  // {
  //   path: 'admin',
  //   component: AdminDashboard,
  //   //canActivate: [AdminGuard]
  // },
  // { path: 'boek-import', component: BoekImport },

  // // the wildcard (**) must ALWAYS be the absolute last item in the array
  // { path: '**', redirectTo: 'not-found' },

  { path: '', component: HomePage },
  { path: 'catalogus', component: Catalogus },
  { path: 'details/:id', component: Detail },
  { path: 'login', component: Login },
  { path: 'login-success', component: LoginSuccessComponent },
  { path: 'not-found', component: PageNotFound },

  { path: 'leeslijst/:id', component: Leeslijst, canActivate: [authGuard] },
  { path: 'reserveren', component: ReserverenComponent, canActivate: [authGuard] },
  { path: 'leengeschiedenis', component: Leengeschiedenis, canActivate: [authGuard] },

  { path: 'boek-beheer', component: BoekBeheer, canActivate: [adminGuard] },
  { path: 'beheer/aanvragen', component: AanvragenBeheer, canActivate: [adminGuard] },
  { path: 'gerapporteerde-beoordelingen', component: GerapporteerdeReviews, canActivate: [adminGuard] },
  { path: 'boek-import', component: BoekImport, canActivate: [adminGuard] },
  { path: 'admin', component: AdminDashboard, canActivate: [adminGuard] },

  { path: '**', redirectTo: 'not-found' },
];
