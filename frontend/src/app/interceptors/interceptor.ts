import { HttpInterceptorFn } from '@angular/common/http';
import { isDevMode } from '@angular/core';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    // Externe API's (zoals Google Books) niet aanraken: een custom header
    // triggert een CORS preflight die de externe server afwijst.
    // Eigen backend op localhost (andere poort) telt NIET als extern.
    const isOwnBackend =
        req.url.startsWith('/') ||
        req.url.startsWith(window.location.origin) ||
        /^https?:\/\/localhost(:\d+)?\//i.test(req.url);
    if (!isOwnBackend) {
        return next(req);
    }

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
