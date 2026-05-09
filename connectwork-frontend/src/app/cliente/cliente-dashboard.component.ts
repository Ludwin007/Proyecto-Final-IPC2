import { Component, signal, inject, OnInit, computed } from '@angular/core';
import { ApiService } from '../core/services/api.service';
import { AuthService } from '../core/services/auth.service';
import { ToastService } from '../core/services/toast.service';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-cliente-dashboard',
  standalone: true,
  imports: [RouterLink, CommonModule, FormsModule],
  template: `
    <div class="topbar">
      <div class="topbar-titulo">
        <h2>Hola, {{ nombreCorto() }}</h2>
        <p>Aqui puedes gestionar todos tus proyectos y contratos</p>
      </div>
      <a routerLink="/cliente/proyectos/nuevo" class="btn btn-primario">+ Publicar proyecto</a>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icono" style="background:#eff6ff">📋</div>
        <div class="stat-info">
          <strong>{{ stats().totalProyectos }}</strong>
          <span>Proyectos publicados</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icono" style="background:#f0fdf4">✅</div>
        <div class="stat-info">
          <strong>{{ stats().proyectosActivos }}</strong>
          <span>En progreso</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icono" style="background:#fdf4ff">📝</div>
        <div class="stat-info">
          <strong>{{ stats().contratosActivos }}</strong>
          <span>Contratos activos</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icono" style="background:#fff7ed">💰</div>
        <div class="stat-info">
          <strong>Q {{ (auth.usuario()?.saldo || 0) | number:'1.2-2' }}</strong>
          <span>Saldo disponible</span>
        </div>
      </div>
    </div>

    <div class="grid-2">
      <div class="card">
        <div class="card-header">
          <span class="card-titulo">Proyectos recientes</span>
          <a routerLink="/cliente/proyectos" class="btn btn-contorno btn-sm">Ver todos</a>
        </div>
        @if (cargando()) {
          <div class="cargando-contenedor"><div class="spinner"></div></div>
        } @else if (proyectos().length === 0) {
          <div class="estado-vacio" style="padding:24px">
            <span class="icono-vacio">📭</span>
            <h3>Sin proyectos aun</h3>
            <a routerLink="/cliente/proyectos/nuevo" class="btn btn-primario btn-sm" style="margin-top:12px">Publicar mi primer proyecto</a>
          </div>
        } @else {
          <div style="display:flex;flex-direction:column;gap:12px">
            @for (p of proyectos().slice(0,4); track p.idProyecto) {
              <a routerLink="/cliente/proyectos/{{ p.idProyecto }}" style="display:block">
                <div class="proyecto-card" style="padding:14px">
                  <div class="proyecto-card-header">
                    <strong style="font-size:14px">{{ p.titulo }}</strong>
                    <span class="badge" [class]="badgeEstado(p.estado)">{{ p.estado }}</span>
                  </div>
                  <div class="proyecto-card-meta">
                    <span>Q {{ p.presupuestoMax | number:'1.2-2' }}</span>
                    <span>{{ p.nombreCategoria }}</span>
                  </div>
                </div>
              </a>
            }
          </div>
        }
      </div>

      <div class="card">
        <div class="card-header">
          <span class="card-titulo">Acciones rapidas</span>
        </div>
        <div style="display:flex;flex-direction:column;gap:12px">
          <a routerLink="/cliente/proyectos/nuevo" class="btn btn-primario">📋 Publicar nuevo proyecto</a>
          <a routerLink="/cliente/contratos" class="btn btn-secundario">📝 Ver mis contratos</a>
          <a routerLink="/cliente/reportes" class="btn btn-contorno">📊 Ver reportes</a>
          <button class="btn btn-exito" (click)="mostrarRecarga.set(!mostrarRecarga())">💰 Recargar saldo</button>
        </div>

        @if (mostrarRecarga()) {
          <div style="margin-top:16px;padding:16px;background:var(--fondo);border-radius:var(--radio-sm)">
            @if (mensajeRecarga()) {
              <div class="alerta" [class]="mensajeRecarga()!.startsWith('Error') ? 'alerta-error' : 'alerta-exito'" style="margin-bottom:10px">
                {{ mensajeRecarga() }}
              </div>
            }
            <div class="campo" style="margin-bottom:10px">
              <label>Monto a recargar (Q)</label>
              <input type="number" [(ngModel)]="montoRecarga" min="1" step="0.01" placeholder="0.00" />
            </div>
            <div style="display:flex;gap:8px">
              <button class="btn btn-exito btn-sm" [disabled]="recargando()" (click)="recargar()">
                {{ recargando() ? 'Procesando...' : 'Recargar' }}
              </button>
              <button class="btn btn-contorno btn-sm" (click)="mostrarRecarga.set(false)">Cancelar</button>
            </div>
          </div>
        }
      </div>
    </div>
  `
})
export class ClienteDashboardComponent implements OnInit {
  auth = inject(AuthService);
  private api = inject(ApiService);
  private toast = inject(ToastService);

  nombreCorto = computed(() => {
    const nombre = this.auth.usuario()?.nombreCompleto;
    return nombre ? nombre.split(' ')[0] : 'Usuario';
  });

  cargando = signal(true);
  recargando = signal(false);
  mostrarRecarga = signal(false);
  proyectos = signal<any[]>([]);
  montoRecarga = 0;
  mensajeRecarga = signal<string | null>(null);
  stats = signal({ totalProyectos: 0, proyectosActivos: 0, contratosActivos: 0 });

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.api.get<any>('/proyectos').subscribe({
      next: res => {
        const lista = res.datos?.proyectos ?? [];
        this.proyectos.set(lista);
        this.stats.set({
          totalProyectos: lista.length,
          proyectosActivos: lista.filter((p: any) => ['ABIERTO','EN_PROGRESO','ENTREGA_PENDIENTE'].includes(p.estado)).length,
          contratosActivos: lista.filter((p: any) => p.estado === 'EN_PROGRESO').length
        });
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false)
    });
  }

  recargar(): void {
    this.mensajeRecarga.set(null);
    if (!this.montoRecarga || this.montoRecarga <= 0) {
      this.mensajeRecarga.set('El monto debe ser mayor a cero');
      return;
    }
    this.recargando.set(true);
    this.api.post<any>('/cliente/recargar', { monto: this.montoRecarga }).subscribe({
      next: res => {
        this.recargando.set(false);
        if (res.exito) {
          this.auth.actualizarSaldo(res.datos?.nuevoSaldo);
          this.montoRecarga = 0;
          this.mensajeRecarga.set('Recarga realizada exitosamente');
          setTimeout(() => { this.mostrarRecarga.set(false); this.mensajeRecarga.set(null); }, 2000);
        } else {
          this.mensajeRecarga.set(res.mensaje);
        }
      },
      error: () => { this.recargando.set(false); this.mensajeRecarga.set('Error al conectar'); }
    });
  }

  badgeEstado(e: string): string {
    const m: Record<string, string> = {
      ABIERTO: 'badge-verde', EN_PROGRESO: 'badge-azul',
      ENTREGA_PENDIENTE: 'badge-naranja', COMPLETADO: 'badge-gris',
      CANCELADO: 'badge-rojo', EN_REVISION: 'badge-purpura'
    };
    return m[e] ?? 'badge-gris';
  }
}
