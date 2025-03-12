import { HttpInterceptorFn} from '@angular/common/http';


export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
    // Get the token directly from localStorage (don't rely on the user object)
    console.log("JWT Interceptor running for URL:", req.url);
    const token = localStorage.getItem('jwtToken');
    console.log("Token from localStorage:", token);

    if (token) {
      // Clone the request with the token
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
      console.log('JWT Authorization header added:', `Bearer ${token}`);
    }
    return next(req);

}
