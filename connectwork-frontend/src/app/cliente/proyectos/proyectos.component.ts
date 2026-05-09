import { Component, signal, inject, OnInit } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-proyectos',
  standalone: true,
  imports: [RouterLink, CommonModule],
  template: `
    <div class="topbar">
      <div class="topbar-titulo">
        <h2>Mis proyectos</h2>
        <p>Todos los proyectos que has publicado</p>
      </div>
      <a routerLink="/cliente/proyectos/nuevo" class="btn btn-primario">+ Nuevo proyecto</a>
    </div>

    @if (cargando()) {
      <div class="cargando-contenedor"><div class="spinner"></div><p>Cargando proyectos...</p></div>
    } @else if (proyectos().length === 0) {
      <div class="card estado-vacio">
        <span class="icono-vacio">📭</span>
        <h3>Aun no tienes proyectos</h3>
        <p>Publica tu primer proyecto y empieza a recibir propuestas de freelancers</p>
        <a routerLink="/cliente/proyectos/nuevo" class="btn btn-primario" style="margin-top:16px">Publicar proyecto</a>
      </div>
    } @else {
      <div style="display:flex;flex-direction:column;gap:16px">
        @for (p of proyectos(); track p.idProyecto) {
          <a routerLink="/cliente/proyectos/{{ p.idProyecto }}" style="display:block">
            <div class="proyecto-card">
              <div class="proyecto-card-header">
                <div>
                  <div class="proyecto-card-titulo">{{ p.titulo }}</div>
                  <span style="font-size:12px;color:var(--texto-suave)">{{ p.nombreCategoria }}</span>
                </div>
                <span class="badge" [class]="badgeEstado(p.estado)">{{ p.estado }}</span>
              </div>
              <p class="proyecto-card-desc">{{ p.descripcion | slice:0:140 }}{{ p.descripcion?.length > 140 ? '...' : '' }}</p>
              <div class="proyecto-card-meta">
                <span>💰 Q {{ p.presupuestoMax | number:'1.2-2' }}</span>
                <span>📅 {{ p.fechaLimite | date:'dd/MM/yyyy' }}</span>
                <span>📌 {{ p.fechaPublicacion | date:'dd/MM/yyyy' }}</span>
              </div>
              @if (p.habilidades?.length > 0) {
                <div class="chips-lista" style="margin-top:10px">
                  @for (h of p.habilidades.slice(0,4); track h.idHabilidad) {
                    <span class="chip">{{ h.nombre }}</span>
                  }
                  @if (p.habilidades.length > 4) {
                    <span class="chip" style="background:#f1f5f9;color:var(--texto-suave)">+{{ p.habilidades.length - 4 }}</span>
                  }
                </div>
              }
            </div>
          </a>
        }
      </div>
    }
  `
})
export class ProyectosComponent implements OnInit {
  private api = inject(ApiService);
  cargando = signal(true);
  proyectos = signal<any[]>([]);

  ngOnInit(): void {
    this.api.get<any>('/proyectos').subscribe({
      next: res => { this.proyectos.set(res.datos?.proyectos ?? []); this.cargando.set(false); },
      error: () => this.cargando.set(false)
    });
  }

  badgeEstado(e: string): string {
    const m: Record<string, string> = { ABIERTO:'badge-verde', EN_PROGRESO:'badge-azul', ENTREGA_PENDIENTE:'badge-naranja', COMPLETADO:'badge-gris', CANCELADO:'badge-rojo', EN_REVISION:'badge-purpura' };
    return m[e] ?? 'badge-gris';
  }
}
