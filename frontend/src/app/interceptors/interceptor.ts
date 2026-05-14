import { HttpInterceptorFn } from '@angular/common/http';
import { isDevMode } from '@angular/core';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    // Dev-modus: stuur X-Dev-Role mee zodat de backend DevAuthFilter
    // weet welke rol hij moet simuleren. De rol kan tijdens het testen
    // overruled worden via localStorage.setItem('devRole', 'STUDENT').
    if (isDevMode()) {
        const devRole = (localStorage.getItem('devRole') || 'BEHEERDER').toUpperCase();
        const devReq = req.clone({ setHeaders: { 'X-Dev-Role': devRole } });
        return next(devReq);
    }

    // Productie: standaard JWT-flow via localStorage.
    const token = localStorage.getItem('jwtToken');
    if (token) {
        const clonedRequest = req.clone({
            setHeaders: {
                Authorization: `Bearer ${token}`
            }
        });
        return next(clonedRequest);
    }

    return next(req);
};
