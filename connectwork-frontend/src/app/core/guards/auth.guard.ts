import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const auth   = inject(AuthService);
  const router = inject(Router);
  if (!auth.autenticado()) { router.navigate(['/login']); return false; }
  const u = auth.usuario();
  if (u && !u.perfilCompleto && u.tipoRol !== 'ADMIN') {
    const ruta = u.tipoRol === 'CLIENTE' ? '/completar-perfil/cliente' : '/completar-perfil/freelancer';
    router.navigate([ruta]);
    return false;
  }
  return true;
};

export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService); const router = inject(Router);
  if (!auth.esAdmin()) { router.navigate(['/login']); return false; }
  return true;
};

export const clienteGuard: CanActivateFn = () => {
  const auth = inject(AuthService); const router = inject(Router);
  if (!auth.esCliente()) { router.navigate(['/login']); return false; }
  return true;
};

export const freelancerGuard: CanActivateFn = () => {
  const auth = inject(AuthService); const router = inject(Router);
  if (!auth.esFreelancer()) { router.navigate(['/login']); return false; }
  return true;
};

export const perfilIncompleto: CanActivateFn = () => {
  const auth = inject(AuthService); const router = inject(Router);
  if (!auth.autenticado()) { router.navigate(['/login']); return false; }
  const u = auth.usuario();
  if (u?.perfilCompleto) {
    const rutas: Record<string, string> = { ADMIN: '/admin', CLIENTE: '/cliente', FREELANCER: '/freelancer' };
    router.navigate([rutas[u.tipoRol] ?? '/login']);
    return false;
  }
  return true;
};
