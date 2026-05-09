import { Routes } from '@angular/router';
import { authGuard, adminGuard, clienteGuard, freelancerGuard, perfilIncompleto } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login',
    loadComponent: () => import('./auth/login.component').then(m => m.LoginComponent) },
  { path: 'registro',
    loadComponent: () => import('./auth/registro.component').then(m => m.RegistroComponent) },
  { path: 'completar-perfil/cliente',
    canActivate: [perfilIncompleto],
    loadComponent: () => import('./auth/perfil-cliente.component').then(m => m.PerfilClienteComponent) },
  { path: 'completar-perfil/freelancer',
    canActivate: [perfilIncompleto],
    loadComponent: () => import('./auth/perfil-freelancer.component').then(m => m.PerfilFreelancerComponent) },
  {
    path: 'admin',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./admin/admin-layout.component').then(m => m.AdminLayoutComponent),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard',
        loadComponent: () => import('./admin/admin-dashboard.component').then(m => m.AdminDashboardComponent) },
      { path: 'usuarios',
        loadComponent: () => import('./admin/usuarios/usuarios.component').then(m => m.UsuariosComponent) },
      { path: 'categorias',
        loadComponent: () => import('./admin/categorias/categorias.component').then(m => m.CategoriasComponent) },
      { path: 'comision',
        loadComponent: () => import('./admin/comision/comision.component').then(m => m.ComisionComponent) },
      { path: 'solicitudes',
        loadComponent: () => import('./admin/solicitudes/solicitudes.component').then(m => m.SolicitudesComponent) },
      { path: 'reportes',
        loadComponent: () => import('./admin/reportes/reportes-admin.component').then(m => m.ReportesAdminComponent) }
    ]
  },
  {
    path: 'cliente',
    canActivate: [authGuard, clienteGuard],
    loadComponent: () => import('./cliente/cliente-layout.component').then(m => m.ClienteLayoutComponent),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard',
        loadComponent: () => import('./cliente/cliente-dashboard.component').then(m => m.ClienteDashboardComponent) },
      { path: 'proyectos',
        loadComponent: () => import('./cliente/proyectos/proyectos.component').then(m => m.ProyectosComponent) },
      { path: 'proyectos/nuevo',
        loadComponent: () => import('./cliente/proyectos/nuevo-proyecto.component').then(m => m.NuevoProyectoComponent) },
      { path: 'proyectos/:id',
        loadComponent: () => import('./cliente/proyectos/detalle-proyecto.component').then(m => m.DetalleProyectoComponent) },
      { path: 'contratos',
        loadComponent: () => import('./cliente/contratos/contratos.component').then(m => m.ContratosClienteComponent) },
      { path: 'contratos/:id',
        loadComponent: () => import('./cliente/contratos/detalle-contrato-cliente.component').then(m => m.DetalleContratoClienteComponent) },
      { path: 'reportes',
        loadComponent: () => import('./cliente/reportes/reportes-cliente.component').then(m => m.ReportesClienteComponent) }
    ]
  },
  {
    path: 'freelancer',
    canActivate: [authGuard, freelancerGuard],
    loadComponent: () => import('./freelancer/freelancer-layout.component').then(m => m.FreelancerLayoutComponent),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard',
        loadComponent: () => import('./freelancer/freelancer-dashboard.component').then(m => m.FreelancerDashboardComponent) },
      { path: 'explorar',
        loadComponent: () => import('./freelancer/explorar/explorar-proyectos.component').then(m => m.ExplorarProyectosComponent) },
      { path: 'explorar/:id',
        loadComponent: () => import('./freelancer/explorar/detalle-proyecto-fl.component').then(m => m.DetalleProyectoFlComponent) },
      { path: 'propuestas',
        loadComponent: () => import('./freelancer/mis-propuestas/mis-propuestas.component').then(m => m.MisPropuestasComponent) },
      { path: 'contratos',
        loadComponent: () => import('./freelancer/contratos/contratos-fl.component').then(m => m.ContratosFlComponent) },
      { path: 'contratos/:id',
        loadComponent: () => import('./freelancer/contratos/detalle-contrato-fl.component').then(m => m.DetalleContratoFlComponent) },
      { path: 'reportes',
        loadComponent: () => import('./freelancer/reportes/reportes-fl.component').then(m => m.ReportesFlComponent) }
    ]
  },
  { path: '**', redirectTo: '/login' }
];
