import { Component, signal, inject, OnInit } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-categorias',
  standalone: true,
  imports: [FormsModule, CommonModule],
  template: `
    <div class="topbar">
      <div class="topbar-titulo">
        <h2>Categorias y Habilidades</h2>
        <p>Gestiona el catalogo de servicios de la plataforma</p>
      </div>
      <button class="btn btn-primario" (click)="abrirModalCategoria()">+ Nueva categoria</button>
    </div>

    @if (cargando()) {
      <div class="cargando-contenedor"><div class="spinner"></div></div>
    } @else {
      <div style="display:flex;flex-direction:column;gap:16px">
        @for (cat of categorias(); track cat.idCategoria) {
          <div class="card">
            <div class="card-header">
              <div style="display:flex;align-items:center;gap:12px">
                <strong style="font-size:16px">{{ cat.nombre }}</strong>
                <span class="badge" [class]="cat.activo ? 'badge-verde' : 'badge-rojo'">
                  {{ cat.activo ? 'Activa' : 'Inactiva' }}
                </span>
              </div>
              <div style="display:flex;gap:8px">
                <button class="btn btn-contorno btn-sm" (click)="abrirEditarCategoria(cat)">Editar</button>
                <button class="btn btn-sm" [class]="cat.activo ? 'btn-peligro' : 'btn-exito'" (click)="toggleCategoria(cat)">
                  {{ cat.activo ? 'Desactivar' : 'Activar' }}
                </button>
                <button class="btn btn-secundario btn-sm" (click)="abrirModalHabilidad(cat)">+ Habilidad</button>
              </div>
            </div>
            @if (cat.descripcion) {
              <p style="color:var(--texto-suave);font-size:13px;margin-bottom:12px">{{ cat.descripcion }}</p>
            }
            <div class="chips-lista">
              @for (h of habilidadesDe(cat.idCategoria); track h.idHabilidad) {
                <div class="chip" style="gap:8px">
                  {{ h.nombre }}
                  <span class="badge" [class]="h.activo ? 'badge-verde' : 'badge-rojo'" style="padding:1px 6px;font-size:10px">
                    {{ h.activo ? 'A' : 'I' }}
                  </span>
                  <button style="background:none;border:none;cursor:pointer;color:var(--texto-suave);font-size:12px;padding:0" (click)="toggleHabilidad(h)" title="{{ h.activo ? 'Desactivar' : 'Activar' }}">⚙</button>
                </div>
              }
              @if (habilidadesDe(cat.idCategoria).length === 0) {
                <span style="color:var(--texto-suave);font-size:13px">Sin habilidades registradas</span>
              }
            </div>
          </div>
        }
      </div>
    }

    @if (modalCategoriaVisible()) {
      <div class="overlay-modal" (click)="cerrarModales()">
        <div class="modal" (click)="$event.stopPropagation()">
          <div class="modal-cabecera">
            <h3>{{ editando() ? 'Editar categoria' : 'Nueva categoria' }}</h3>
            <button class="btn-cerrar" (click)="cerrarModales()">✕</button>
          </div>
          <div class="campo">
            <label>Nombre</label>
            <input type="text" [(ngModel)]="formCat.nombre" placeholder="Nombre de la categoria" />
          </div>
          <div class="campo">
            <label>Descripcion</label>
            <textarea [(ngModel)]="formCat.descripcion" rows="3" placeholder="Descripcion opcional..."></textarea>
          </div>
          <div class="modal-acciones">
            <button class="btn btn-contorno" (click)="cerrarModales()">Cancelar</button>
            <button class="btn btn-primario" [disabled]="guardando()" (click)="guardarCategoria()">
              {{ guardando() ? 'Guardando...' : 'Guardar' }}
            </button>
          </div>
        </div>
      </div>
    }

    @if (modalHabilidadVisible()) {
      <div class="overlay-modal" (click)="cerrarModales()">
        <div class="modal" (click)="$event.stopPropagation()">
          <div class="modal-cabecera">
            <h3>Nueva habilidad en "{{ catSeleccionada()?.nombre }}"</h3>
            <button class="btn-cerrar" (click)="cerrarModales()">✕</button>
          </div>
          <div class="campo">
            <label>Nombre</label>
            <input type="text" [(ngModel)]="formHab.nombre" placeholder="Nombre de la habilidad" />
          </div>
          <div class="campo">
            <label>Descripcion</label>
            <textarea [(ngModel)]="formHab.descripcion" rows="2" placeholder="Descripcion opcional..."></textarea>
          </div>
          <div class="modal-acciones">
            <button class="btn btn-contorno" (click)="cerrarModales()">Cancelar</button>
            <button class="btn btn-primario" [disabled]="guardando()" (click)="guardarHabilidad()">
              {{ guardando() ? 'Guardando...' : 'Agregar' }}
            </button>
          </div>
        </div>
      </div>
    }
  `
})
export class CategoriasComponent implements OnInit {
  private api = inject(ApiService);
  private toast = inject(ToastService);

  cargando = signal(true);
  guardando = signal(false);
  categorias = signal<any[]>([]);
  habilidades = signal<any[]>([]);
  modalCategoriaVisible = signal(false);
  modalHabilidadVisible = signal(false);
  editando = signal(false);
  catSeleccionada = signal<any>(null);

  formCat = { idCategoria: 0, nombre: '', descripcion: '' };
  formHab = { nombre: '', descripcion: '' };

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.cargando.set(true);
    this.api.get<any>('/admin/categorias').subscribe(res => {
      this.categorias.set(res.datos?.categorias ?? []);
    });
    this.api.get<any>('/catalogo/habilidades').subscribe(res => {
      this.habilidades.set(res.datos?.habilidades ?? []);
      this.cargando.set(false);
    });
  }

  habilidadesDe(idCategoria: number): any[] {
    return this.habilidades().filter(h => h.idCategoria === idCategoria);
  }

  abrirModalCategoria(): void {
    this.editando.set(false);
    this.formCat = { idCategoria: 0, nombre: '', descripcion: '' };
    this.modalCategoriaVisible.set(true);
  }

  abrirEditarCategoria(cat: any): void {
    this.editando.set(true);
    this.formCat = { idCategoria: cat.idCategoria, nombre: cat.nombre, descripcion: cat.descripcion ?? '' };
    this.modalCategoriaVisible.set(true);
  }

  abrirModalHabilidad(cat: any): void {
    this.catSeleccionada.set(cat);
    this.formHab = { nombre: '', descripcion: '' };
    this.modalHabilidadVisible.set(true);
  }

  cerrarModales(): void {
    this.modalCategoriaVisible.set(false);
    this.modalHabilidadVisible.set(false);
  }

  guardarCategoria(): void {
    if (!this.formCat.nombre.trim()) { this.toast.error('El nombre es obligatorio'); return; }
    this.guardando.set(true);
    const obs = this.editando()
      ? this.api.put<any>(`/admin/categorias/${this.formCat.idCategoria}`, this.formCat)
      : this.api.post<any>('/admin/categorias', this.formCat);
    obs.subscribe({
      next: res => {
        this.guardando.set(false);
        if (res.exito) { this.toast.exito(res.mensaje); this.cerrarModales(); this.cargar(); }
        else this.toast.error(res.mensaje);
      },
      error: () => { this.guardando.set(false); this.toast.error('Error al guardar'); }
    });
  }

  guardarHabilidad(): void {
    if (!this.formHab.nombre.trim()) { this.toast.error('El nombre es obligatorio'); return; }
    this.guardando.set(true);
    const payload = { ...this.formHab, idCategoria: this.catSeleccionada()?.idCategoria };
    this.api.post<any>('/admin/habilidades', payload).subscribe({
      next: res => {
        this.guardando.set(false);
        if (res.exito) { this.toast.exito(res.mensaje); this.cerrarModales(); this.cargar(); }
        else this.toast.error(res.mensaje);
      },
      error: () => { this.guardando.set(false); this.toast.error('Error al guardar'); }
    });
  }

  toggleCategoria(cat: any): void {
    const accion = cat.activo ? 'desactivar' : 'activar';
    this.api.put<any>(`/admin/categorias/${cat.idCategoria}/${accion}`).subscribe({
      next: res => {
        if (res.exito) { this.toast.exito(res.mensaje); this.cargar(); }
        else this.toast.error(res.mensaje);
      }
    });
  }

  toggleHabilidad(h: any): void {
    const accion = h.activo ? 'desactivar' : 'activar';
    this.api.put<any>(`/admin/habilidades/${h.idHabilidad}/${accion}`).subscribe({
      next: res => {
        if (res.exito) { this.toast.exito(res.mensaje); this.cargar(); }
        else this.toast.error(res.mensaje);
      }
    });
  }
}
