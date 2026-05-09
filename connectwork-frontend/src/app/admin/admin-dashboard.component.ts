import { Component, signal, inject, OnInit } from '@angular/core';
import { ApiService } from '../core/services/api.service';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [RouterLink, CommonModule],
  template: `
    <div class="topbar">
      <div class="topbar-titulo">
        <h2>Dashboard</h2>
        <p>Resumen general de la plataforma</p>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icono" style="background:#eff6ff">👥</div>
        <div class="stat-info">
          <strong>{{ totalUsuarios() }}</strong>
          <span>Usuarios registrados</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icono" style="background:#f0fdf4">🏷️</div>
        <div class="stat-info">
          <strong>{{ totalCategorias() }}</strong>
          <span>Categorias activas</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icono" style="background:#fff7ed">📬</div>
        <div class="stat-info">
          <strong>{{ solicitudesPendientes() }}</strong>
          <span>Solicitudes pendientes</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icono" style="background:#fdf4ff">💰</div>
        <div class="stat-info">
          <strong>{{ comisionActual() }}%</strong>
          <span>Comision vigente</span>
        </div>
      </div>
    </div>

    <div class="grid-2">
      <div class="card">
        <div class="card-header">
          <span class="card-titulo">Acciones rapidas</span>
        </div>
        <div style="display:flex;flex-direction:column;gap:12px">
          <a routerLink="/admin/usuarios" class="btn btn-secundario">👥 Gestionar usuarios</a>
          <a routerLink="/admin/categorias" class="btn btn-secundario">🏷️ Gestionar categorias</a>
          <a routerLink="/admin/solicitudes" class="btn btn-alerta">📬 Revisar solicitudes ({{ solicitudesPendientes() }})</a>
          <a routerLink="/admin/comision" class="btn btn-exito">💰 Cambiar comision</a>
          <a routerLink="/admin/reportes" class="btn btn-primario">📈 Ver reportes</a>
        </div>
      </div>
      <div class="card">
        <div class="card-header">
          <span class="card-titulo">Ultimas solicitudes pendientes</span>
        </div>
        @if (solicitudes().length === 0) {
          <div class="estado-vacio" style="padding:32px">
            <span class="icono-vacio">📭</span>
            <p>No hay solicitudes pendientes</p>
          </div>
        } @else {
          <div style="display:flex;flex-direction:column;gap:12px">
            @for (s of solicitudes().slice(0,5); track s.idSolicitud) {
              <div style="padding:12px;border:1px solid var(--borde);border-radius:var(--radio-sm)">
                <div style="display:flex;justify-content:space-between;align-items:center">
                  <strong style="font-size:14px">{{ s.nombre }}</strong>
                  <span class="badge badge-naranja">{{ s.tipo }}</span>
                </div>
                <p style="font-size:12px;color:var(--texto-suave);margin-top:4px">Por: {{ s.nombreUsuario }}</p>
              </div>
            }
            <a routerLink="/admin/solicitudes" class="btn btn-contorno btn-sm" style="text-align:center;justify-content:center">Ver todas</a>
          </div>
        }
      </div>
    </div>
  `
})
export class AdminDashboardComponent implements OnInit {
  private api = inject(ApiService);

  totalUsuarios = signal(0);
  totalCategorias = signal(0);
  solicitudesPendientes = signal(0);
  comisionActual = signal(0);
  solicitudes = signal<any[]>([]);

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.api.get<any>('/admin/usuarios').subscribe(res => {
      this.totalUsuarios.set(res.datos?.usuarios?.length ?? 0);
    });
    this.api.get<any>('/catalogo/categorias').subscribe(res => {
      this.totalCategorias.set(res.datos?.categorias?.length ?? 0);
    });
    this.api.get<any>('/admin/solicitudes').subscribe(res => {
      const lista = res.datos?.solicitudes ?? [];
      this.solicitudes.set(lista);
      this.solicitudesPendientes.set(lista.length);
    });
    this.api.get<any>('/admin/comision').subscribe(res => {
      this.comisionActual.set(res.datos?.vigente?.porcentaje ?? 0);
    });
  }
}
