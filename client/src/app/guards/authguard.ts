import {inject} from '@angular/core';
import {AuthService} from '../services/auth.service';
import {ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot} from '@angular/router';

export const authguard: CanActivateFn =
  (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {

    const authSvc = inject(AuthService);
    const router = inject(Router);
    if (authSvc.isLoggedIn()) {
      return true;
    }
    // If user is not logged in, redirect to the login page
    return router.parseUrl('/login');
  };
