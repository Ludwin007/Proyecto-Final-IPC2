import { Component, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../core/services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-cliente-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  template: `
    <div class="pagina-layout">
      <aside class="sidebar">
        <div class="sidebar-logo">
          <h1>ConnectWork</h1>
          <p>Panel Cliente</p>
        </div>
        <nav class="sidebar-nav">
          <div class="sidebar-seccion">General</div>
          <a routerLink="/cliente/dashboard" routerLinkActive="activo" [routerLinkActiveOptions]="{exact:true}">
            <span class="icono">🏠</span> Dashboard
          </a>
          <div class="sidebar-seccion">Proyectos</div>
          <a routerLink="/cliente/proyectos" routerLinkActive="activo">
            <span class="icono">📋</span> Mis proyectos
          </a>
          <a routerLink="/cliente/proyectos/nuevo" routerLinkActive="activo">
            <span class="icono">➕</span> Publicar proyecto
          </a>
          <div class="sidebar-seccion">Contratos</div>
          <a routerLink="/cliente/contratos" routerLinkActive="activo">
            <span class="icono">📝</span> Mis contratos
          </a>
          <div class="sidebar-seccion">Finanzas</div>
          <a routerLink="/cliente/reportes" routerLinkActive="activo">
            <span class="icono">📊</span> Reportes
          </a>
        </nav>
        <div class="sidebar-usuario">
          <div class="sidebar-avatar">{{ inicial() }}</div>
          <div class="sidebar-info">
            <strong>{{ auth.usuario()?.nombreCompleto }}</strong>
            <span style="color:var(--verde)">Q {{ auth.usuario()?.saldo | number:'1.2-2' }}</span>
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
export class ClienteLayoutComponent {
  auth = inject(AuthService);
  inicial() { return this.auth.usuario()?.nombreCompleto?.charAt(0)?.toUpperCase() ?? 'C'; }
}
