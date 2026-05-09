import { Component, signal, inject, OnInit } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-solicitudes',
  standalone: true,
  imports: [FormsModule, CommonModule],
  template: `
    <div class="topbar">
      <div class="topbar-titulo">
        <h2>Solicitudes pendientes</h2>
        <p>Revisa y resuelve solicitudes de nuevas categorias y habilidades</p>
      </div>
    </div>

    @if (cargando()) {
      <div class="cargando-contenedor"><div class="spinner"></div></div>
    } @else if (solicitudes().length === 0) {
      <div class="card estado-vacio">
        <span class="icono-vacio">📭</span>
        <h3>No hay solicitudes pendientes</h3>
        <p>Cuando los usuarios soliciten nuevas categorias o habilidades apareceran aqui</p>
      </div>
    } @else {
      <div style="display:flex;flex-direction:column;gap:16px">
        @for (s of solicitudes(); track s.idSolicitud) {
          <div class="card">
            <div style="display:flex;justify-content:space-between;align-items:flex-start;gap:16px">
              <div style="flex:1">
                <div style="display:flex;align-items:center;gap:10px;margin-bottom:8px">
                  <span class="badge" [class]="s.tipo === 'CATEGORIA' ? 'badge-azul' : 'badge-purpura'">{{ s.tipo }}</span>
                  <strong style="font-size:16px">{{ s.nombre }}</strong>
                </div>
                @if (s.descripcion) {
                  <p style="color:var(--texto-suave);font-size:14px;margin-bottom:8px">{{ s.descripcion }}</p>
                }
                <p style="font-size:12px;color:var(--texto-suave)">Solicitado por: <strong>{{ s.nombreUsuario }}</strong> — {{ s.fecha | date:'dd/MM/yyyy HH:mm' }}</p>
              </div>
              <div style="display:flex;gap:8px;flex-shrink:0">
                <button class="btn btn-exito btn-sm" (click)="abrirModal(s, 'ACEPTADA')">✓ Aceptar</button>
                <button class="btn btn-peligro btn-sm" (click)="abrirModal(s, 'RECHAZADA')">✕ Rechazar</button>
              </div>
            </div>
          </div>
        }
      </div>
    }

    @if (modalVisible()) {
      <div class="overlay-modal" (click)="cerrarModal()">
        <div class="modal" (click)="$event.stopPropagation()">
          <div class="modal-cabecera">
            <h3>{{ accionModal() === 'ACEPTADA' ? 'Aceptar solicitud' : 'Rechazar solicitud' }}</h3>
            <button class="btn-cerrar" (click)="cerrarModal()">✕</button>
          </div>
          <p style="margin-bottom:16px;color:var(--texto-suave)">
            Solicitud: <strong>{{ solicitudModal()?.nombre }}</strong> ({{ solicitudModal()?.tipo }})
          </p>
          @if (accionModal() === 'ACEPTADA' && solicitudModal()?.tipo === 'HABILIDAD') {
            <div class="campo">
              <label>Categoria a la que pertenecera</label>
              <select [(ngModel)]="idCategoriaPadre">
                <option [value]="0">Seleccionar categoria...</option>
                @for (c of categorias(); track c.idCategoria) {
                  <option [value]="c.idCategoria">{{ c.nombre }}</option>
                }
              </select>
            </div>
          }
          <div class="campo">
            <label>{{ accionModal() === 'ACEPTADA' ? 'Nota (opcional)' : 'Motivo del rechazo (obligatorio)' }}</label>
            <textarea [(ngModel)]="respuesta" rows="3" [placeholder]="accionModal() === 'ACEPTADA' ? 'Comentario adicional...' : 'Indica por que se rechaza la solicitud...'"></textarea>
          </div>
          <div class="modal-acciones">
            <button class="btn btn-contorno" (click)="cerrarModal()">Cancelar</button>
            <button class="btn" [class]="accionModal() === 'ACEPTADA' ? 'btn-exito' : 'btn-peligro'" [disabled]="guardando()" (click)="resolver()">
              {{ guardando() ? 'Procesando...' : (accionModal() === 'ACEPTADA' ? 'Aceptar' : 'Rechazar') }}
            </button>
          </div>
        </div>
      </div>
    }
  `
})
export class SolicitudesComponent implements OnInit {
  private api = inject(ApiService);
  private toast = inject(ToastService);

  cargando = signal(true);
  guardando = signal(false);
  modalVisible = signal(false);
  solicitudes = signal<any[]>([]);
  categorias = signal<any[]>([]);
  solicitudModal = signal<any>(null);
  accionModal = signal<'ACEPTADA' | 'RECHAZADA'>('ACEPTADA');
  respuesta = '';
  idCategoriaPadre = 0;

  ngOnInit(): void {
    this.cargar();
    this.api.get<any>('/admin/categorias').subscribe(res => this.categorias.set(res.datos?.categorias ?? []));
  }

  cargar(): void {
    this.cargando.set(true);
    this.api.get<any>('/admin/solicitudes').subscribe({
      next: res => { this.solicitudes.set(res.datos?.solicitudes ?? []); this.cargando.set(false); },
      error: () => this.cargando.set(false)
    });
  }

  abrirModal(s: any, accion: 'ACEPTADA' | 'RECHAZADA'): void {
    this.solicitudModal.set(s);
    this.accionModal.set(accion);
    this.respuesta = '';
    this.idCategoriaPadre = 0;
    this.modalVisible.set(true);
  }

  cerrarModal(): void { this.modalVisible.set(false); }

  resolver(): void {
    const s = this.solicitudModal();
    if (this.accionModal() === 'RECHAZADA' && !this.respuesta.trim()) {
      this.toast.error('El motivo del rechazo es obligatorio');
      return;
    }
    if (this.accionModal() === 'ACEPTADA' && s?.tipo === 'HABILIDAD' && !this.idCategoriaPadre) {
      this.toast.error('Debes seleccionar la categoria de la habilidad');
      return;
    }
    this.guardando.set(true);
    const payload = { decision: this.accionModal(), respuesta: this.respuesta, idCategoria: this.idCategoriaPadre || null };
    this.api.put<any>(`/admin/solicitudes/${s.idSolicitud}`, payload).subscribe({
      next: res => {
        this.guardando.set(false);
        if (res.exito) { this.toast.exito(res.mensaje); this.cerrarModal(); this.cargar(); }
        else this.toast.error(res.mensaje);
      },
      error: () => { this.guardando.set(false); this.toast.error('Error al procesar'); }
    });
  }
}
