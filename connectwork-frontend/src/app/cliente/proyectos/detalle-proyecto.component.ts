import { Component, signal, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-detalle-proyecto',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="topbar">
      <div class="topbar-titulo">
        <h2>{{ proyecto()?.titulo }}</h2>
        <p>{{ proyecto()?.nombreCategoria }}</p>
      </div>
      <a routerLink="/cliente/proyectos" class="btn btn-contorno">← Volver</a>
    </div>

    @if (cargando()) {
      <div class="cargando-contenedor"><div class="spinner"></div><p>Cargando...</p></div>
    } @else {
      <div class="grid-2" style="align-items:start">
        <div style="display:flex;flex-direction:column;gap:16px">
          <div class="card">
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
              <span class="badge" [class]="badgeEstado(proyecto()?.estado)">{{ proyecto()?.estado }}</span>
              @if (proyecto()?.estado === 'ABIERTO') {
                <div style="display:flex;gap:8px">
                  <button class="btn btn-contorno btn-sm" (click)="editando.set(!editando())">{{ editando() ? 'Cancelar edicion' : 'Editar' }}</button>
                  <button class="btn btn-peligro btn-sm" (click)="cancelarProyecto()">Cancelar proyecto</button>
                </div>
              }
            </div>
            @if (editando()) {
              <div>
                <div class="campo">
                  <label>Titulo</label>
                  <input type="text" [(ngModel)]="formEdit.titulo" />
                </div>
                <div class="campo">
                  <label>Descripcion</label>
                  <textarea [(ngModel)]="formEdit.descripcion" rows="4"></textarea>
                </div>
                <div class="grid-2">
                  <div class="campo">
                    <label>Presupuesto maximo (Q)</label>
                    <input type="number" [(ngModel)]="formEdit.presupuestoMax" min="0" />
                  </div>
                  <div class="campo">
                    <label>Fecha limite</label>
                    <input type="date" [(ngModel)]="formEdit.fechaLimite" />
                  </div>
                </div>
                <div style="display:flex;gap:8px;justify-content:flex-end">
                  <button class="btn btn-contorno btn-sm" (click)="editando.set(false)">Cancelar</button>
                  <button class="btn btn-primario btn-sm" [disabled]="guardando()" (click)="guardarEdicion()">
                    {{ guardando() ? 'Guardando...' : 'Guardar cambios' }}
                  </button>
                </div>
              </div>
            } @else {
              <p style="color:var(--texto-suave);line-height:1.7;margin-bottom:16px">{{ proyecto()?.descripcion }}</p>
              <div class="proyecto-card-meta" style="margin-bottom:12px">
                <span>💰 Presupuesto: Q {{ proyecto()?.presupuestoMax | number:'1.2-2' }}</span>
                <span>📅 Limite: {{ proyecto()?.fechaLimite | date:'dd/MM/yyyy' }}</span>
                <span>📌 Publicado: {{ proyecto()?.fechaPublicacion | date:'dd/MM/yyyy' }}</span>
              </div>
              @if (proyecto()?.habilidades?.length > 0) {
                <div class="chips-lista">
                  @for (h of proyecto()?.habilidades; track h.idHabilidad) {
                    <span class="chip">{{ h.nombre }}</span>
                  }
                </div>
              }
            }
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <span class="card-titulo">Propuestas recibidas ({{ propuestas().length }})</span>
          </div>
          @if (cargandoPropuestas()) {
            <div class="cargando-contenedor" style="padding:24px"><div class="spinner"></div></div>
          } @else if (propuestas().length === 0) {
            <div class="estado-vacio" style="padding:24px">
              <span class="icono-vacio">📭</span>
              <h3>Sin propuestas aun</h3>
              <p>Los freelancers comenzaran a enviar propuestas pronto</p>
            </div>
          } @else {
            <div style="display:flex;flex-direction:column;gap:12px">
              @for (p of propuestas(); track p.idPropuesta) {
                <div style="border:1px solid var(--borde);border-radius:var(--radio-sm);padding:14px;transition:var(--transicion)"
                     [style.borderColor]="p.estado === 'ACEPTADA' ? 'var(--verde)' : p.estado === 'RECHAZADA' ? '#ef4444' : ''">
                  <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:8px">
                    <div>
                      <strong>{{ p.nombreFreelancer }}</strong>
                      <div style="display:flex;gap:8px;align-items:center;margin-top:4px">
                        <span style="font-size:12px;color:var(--naranja)">★ {{ p.calificacionFreelancer | number:'1.1-1' }}</span>
                        <span class="badge badge-gris" style="font-size:11px">{{ p.nivelExperiencia }}</span>
                      </div>
                    </div>
                    <span class="badge" [class]="badgePropuesta(p.estado)">{{ p.estado }}</span>
                  </div>
                  <div style="display:flex;gap:16px;font-size:13px;margin-bottom:10px">
                    <span style="font-weight:700;color:var(--verde)">Q {{ p.montoOfertado | number:'1.2-2' }}</span>
                    <span style="color:var(--texto-suave)">📅 {{ p.plazoDias }} dias</span>
                  </div>
                  <p style="font-size:13px;color:var(--texto-suave);margin-bottom:10px;line-height:1.5">
                    {{ p.cartaPresentacion | slice:0:150 }}{{ p.cartaPresentacion?.length > 150 ? '...' : '' }}
                  </p>
                  @if (p.estado === 'PENDIENTE' && proyecto()?.estado === 'ABIERTO') {
                    <div style="display:flex;gap:8px">
                      <button class="btn btn-exito btn-sm" [disabled]="procesando()" (click)="aceptarPropuesta(p)">✓ Aceptar</button>
                      <button class="btn btn-peligro btn-sm" [disabled]="procesando()" (click)="rechazarPropuesta(p)">✕ Rechazar</button>
                    </div>
                  }
                </div>
              }
            </div>
          }
        </div>
      </div>
    }
  `
})
export class DetalleProyectoComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private api = inject(ApiService);
  private router = inject(Router);
  private toast = inject(ToastService);

  cargando = signal(true);
  cargandoPropuestas = signal(true);
  guardando = signal(false);
  procesando = signal(false);
  editando = signal(false);
  proyecto = signal<any>(null);
  propuestas = signal<any[]>([]);
  formEdit: any = {};

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.cargarProyecto(id);
    this.cargarPropuestas(id);
  }

  cargarProyecto(id: string): void {
    this.api.get<any>(`/proyectos/${id}`).subscribe({
      next: res => {
        const p = res.datos?.proyecto;
        this.proyecto.set(p);
        this.formEdit = { titulo: p?.titulo, descripcion: p?.descripcion, presupuestoMax: p?.presupuestoMax, fechaLimite: p?.fechaLimite, idCategoria: p?.idCategoria, habilidades: p?.habilidades };
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false)
    });
  }

  cargarPropuestas(id: string): void {
    this.api.get<any>(`/propuestas/proyecto/${id}`).subscribe({
      next: res => { this.propuestas.set(res.datos?.propuestas ?? []); this.cargandoPropuestas.set(false); },
      error: () => this.cargandoPropuestas.set(false)
    });
  }

  guardarEdicion(): void {
    this.guardando.set(true);
    const p = this.proyecto();
    this.api.put<any>(`/proyectos/${p.idProyecto}`, this.formEdit).subscribe({
      next: res => {
        this.guardando.set(false);
        if (res.exito) { this.toast.exito('Proyecto actualizado'); this.editando.set(false); this.cargarProyecto(p.idProyecto); }
        else this.toast.error(res.mensaje);
      },
      error: () => { this.guardando.set(false); this.toast.error('Error al actualizar'); }
    });
  }

  aceptarPropuesta(prop: any): void {
    if (!confirm(`¿Aceptar la propuesta de ${prop.nombreFreelancer} por Q ${prop.montoOfertado}?`)) return;
    this.procesando.set(true);
    this.api.put<any>(`/propuestas/${prop.idPropuesta}/aceptar`).subscribe({
      next: res => {
        this.procesando.set(false);
        if (res.exito) { this.toast.exito(res.mensaje); this.router.navigate(['/cliente/contratos']); }
        else this.toast.error(res.mensaje);
      },
      error: () => { this.procesando.set(false); this.toast.error('Error al aceptar propuesta'); }
    });
  }

  rechazarPropuesta(prop: any): void {
    if (!confirm(`¿Rechazar la propuesta de ${prop.nombreFreelancer}?`)) return;
    this.procesando.set(true);
    this.api.put<any>(`/propuestas/${prop.idPropuesta}/rechazar`).subscribe({
      next: res => {
        this.procesando.set(false);
        if (res.exito) {
          this.toast.exito('Propuesta rechazada');
          this.propuestas.update(l => l.map(p => p.idPropuesta === prop.idPropuesta ? { ...p, estado: 'RECHAZADA' } : p));
        } else this.toast.error(res.mensaje);
      },
      error: () => { this.procesando.set(false); this.toast.error('Error al rechazar propuesta'); }
    });
  }

  cancelarProyecto(): void {
    if (!confirm('¿Estas seguro de cancelar este proyecto?')) return;
    const p = this.proyecto();
    this.api.put<any>(`/proyectos/${p.idProyecto}/cancelar`).subscribe({
      next: res => {
        if (res.exito) { this.toast.exito('Proyecto cancelado'); this.router.navigate(['/cliente/proyectos']); }
        else this.toast.error(res.mensaje);
      }
    });
  }

  badgeEstado(e: string): string {
    const m: Record<string, string> = { ABIERTO:'badge-verde', EN_PROGRESO:'badge-azul', ENTREGA_PENDIENTE:'badge-naranja', COMPLETADO:'badge-gris', CANCELADO:'badge-rojo' };
    return m[e] ?? 'badge-gris';
  }

  badgePropuesta(e: string): string {
    const m: Record<string, string> = { PENDIENTE:'badge-naranja', ACEPTADA:'badge-verde', RECHAZADA:'badge-rojo', RETIRADA:'badge-gris' };
    return m[e] ?? 'badge-gris';
  }
}
