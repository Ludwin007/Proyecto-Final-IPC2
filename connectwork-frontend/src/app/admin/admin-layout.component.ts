import { Component, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="pagina-layout">
      <aside class="sidebar">
        <div class="sidebar-logo">
          <h1>ConnectWork</h1>
          <p>Panel Administrador</p>
        </div>
        <nav class="sidebar-nav">
          <div class="sidebar-seccion">General</div>
          <a routerLink="/admin/dashboard" routerLinkActive="activo">
            <span class="icono">📊</span> Dashboard
          </a>
          <div class="sidebar-seccion">Gestion</div>
          <a routerLink="/admin/usuarios" routerLinkActive="activo">
            <span class="icono">👥</span> Usuarios
          </a>
          <a routerLink="/admin/categorias" routerLinkActive="activo">
            <span class="icono">🏷️</span> Categorias y Habilidades
          </a>
          <a routerLink="/admin/solicitudes" routerLinkActive="activo">
            <span class="icono">📬</span> Solicitudes
          </a>
          <a routerLink="/admin/comision" routerLinkActive="activo">
            <span class="icono">💰</span> Comision
          </a>
          <div class="sidebar-seccion">Reportes</div>
          <a routerLink="/admin/reportes" routerLinkActive="activo">
            <span class="icono">📈</span> Reportes
          </a>
        </nav>
        <div class="sidebar-usuario">
          <div class="sidebar-avatar">{{ inicial() }}</div>
          <div class="sidebar-info">
            <strong>{{ auth.usuario()?.nombreCompleto }}</strong>
            <span>Administrador</span>
          </div>
          <button (click)="auth.logout()" style="background:none;border:none;color:rgba(255,255,255,.6);cursor:pointer;font-size:18px;padding:4px" title="Cerrar sesion">⏻</button>
        </div>
      </aside>
      <main class="contenido-principal">
        <router-outlet />
      </main>
    </div>
  `
})
export class AdminLayoutComponent {
  auth = inject(AuthService);
  inicial() { return this.auth.usuario()?.nombreCompleto?.charAt(0)?.toUpperCase() ?? 'A'; }
}
