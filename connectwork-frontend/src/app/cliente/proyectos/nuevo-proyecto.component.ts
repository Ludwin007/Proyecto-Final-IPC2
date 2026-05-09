import { Component, signal, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-nuevo-proyecto',
  standalone: true,
  imports: [FormsModule, RouterLink, CommonModule],
  template: `
    <div class="topbar">
      <div class="topbar-titulo">
        <h2>Publicar nuevo proyecto</h2>
        <p>Describe el trabajo que necesitas y recibe propuestas de freelancers</p>
      </div>
      <a routerLink="/cliente/proyectos" class="btn btn-contorno">← Volver</a>
    </div>

    <div class="card" style="max-width:780px">
      @if (error()) {
        <div class="alerta alerta-error">{{ error() }}</div>
      }
      <div class="campo">
        <label>Titulo del proyecto</label>
        <input type="text" [(ngModel)]="form.titulo" placeholder="Ej: Desarrollo de landing page para empresa..." />
      </div>
      <div class="campo">
        <label>Descripcion detallada</label>
        <textarea [(ngModel)]="form.descripcion" rows="5" placeholder="Describe con detalle el trabajo que necesitas, los entregables esperados, referencias visuales, etc."></textarea>
      </div>
      <div class="grid-2">
        <div class="campo">
          <label>Categoria</label>
          <select [(ngModel)]="form.idCategoria" (ngModelChange)="cargarHabilidades($event)">
            <option [value]="0">Seleccionar categoria...</option>
            @for (c of categorias(); track c.idCategoria) {
              <option [value]="c.idCategoria">{{ c.nombre }}</option>
            }
          </select>
        </div>
        <div class="campo">
          <label>Presupuesto maximo (Q)</label>
          <input type="number" [(ngModel)]="form.presupuestoMax" min="0" step="0.01" placeholder="0.00" />
        </div>
      </div>
      <div class="campo">
        <label>Fecha limite de entrega</label>
        <input type="date" [(ngModel)]="form.fechaLimite" [min]="manana()" />
      </div>
      <div class="campo">
        <label>Habilidades requeridas</label>
        @if (form.idCategoria === 0) {
          <p style="color:var(--texto-suave);font-size:13px;margin-top:6px">Selecciona una categoria primero</p>
        } @else if (habilidades().length === 0) {
          <p style="color:var(--texto-suave);font-size:13px;margin-top:6px">No hay habilidades en esta categoria</p>
        } @else {
          <div class="habilidades-grid" style="display:flex;flex-wrap:wrap;gap:8px;margin-top:8px">
            @for (h of habilidades(); track h.idHabilidad) {
              <label class="hab-check" [class.seleccionada]="habSeleccionada(h.idHabilidad)"
                style="display:flex;align-items:center;gap:6px;padding:6px 12px;border:2px solid var(--borde);border-radius:20px;cursor:pointer;font-size:13px;font-weight:500;transition:var(--transicion)"
                [style.borderColor]="habSeleccionada(h.idHabilidad) ? 'var(--acento)' : ''"
                [style.background]="habSeleccionada(h.idHabilidad) ? '#eff6ff' : ''"
                [style.color]="habSeleccionada(h.idHabilidad) ? 'var(--acento)' : ''">
                <input type="checkbox" [checked]="habSeleccionada(h.idHabilidad)" (change)="toggleHab(h)" style="display:none" />
                {{ h.nombre }}
              </label>
            }
          </div>
        }
        @if (habsSeleccionadas().length > 0) {
          <div class="chips-lista" style="margin-top:12px">
            @for (h of habsSeleccionadas(); track h.idHabilidad) {
              <span class="chip">{{ h.nombre }}</span>
            }
          </div>
        }
      </div>
      <div style="display:flex;justify-content:flex-end;gap:12px;margin-top:8px">
        <a routerLink="/cliente/proyectos" class="btn btn-contorno">Cancelar</a>
        <button class="btn btn-primario" [disabled]="publicando()" (click)="publicar()">
          {{ publicando() ? 'Publicando...' : 'Publicar proyecto' }}
        </button>
      </div>
    </div>
  `
})
export class NuevoProyectoComponent implements OnInit {
  private api = inject(ApiService);
  private router = inject(Router);
  private toast = inject(ToastService);

  publicando = signal(false);
  error = signal('');
  categorias = signal<any[]>([]);
  habilidades = signal<any[]>([]);
  habsSeleccionadas = signal<any[]>([]);

  form = { titulo: '', descripcion: '', idCategoria: 0, presupuestoMax: 0, fechaLimite: '' };

  ngOnInit(): void {
    this.api.get<any>('/catalogo/categorias').subscribe(res => this.categorias.set(res.datos?.categorias ?? []));
  }

  manana(): string {
    const d = new Date(); d.setDate(d.getDate() + 1);
    return d.toISOString().split('T')[0];
  }

  cargarHabilidades(idCat: number): void {
    this.habsSeleccionadas.set([]);
    if (!idCat) { this.habilidades.set([]); return; }
    this.api.get<any>('/catalogo/habilidades', { categoria: String(idCat) }).subscribe(res => this.habilidades.set(res.datos?.habilidades ?? []));
  }

  habSeleccionada(id: number): boolean { return this.habsSeleccionadas().some(h => h.idHabilidad === id); }

  toggleHab(h: any): void {
    if (this.habSeleccionada(h.idHabilidad)) {
      this.habsSeleccionadas.update(l => l.filter(x => x.idHabilidad !== h.idHabilidad));
    } else {
      this.habsSeleccionadas.update(l => [...l, h]);
    }
  }

  publicar(): void {
    if (!this.form.titulo || this.form.titulo.length < 5) { this.error.set('El titulo debe tener al menos 5 caracteres'); return; }
    if (!this.form.descripcion.trim()) { this.error.set('La descripcion es obligatoria'); return; }
    if (!this.form.idCategoria) { this.error.set('Selecciona una categoria'); return; }
    if (!this.form.presupuestoMax || this.form.presupuestoMax <= 0) { this.error.set('El presupuesto debe ser mayor a cero'); return; }
    if (!this.form.fechaLimite) { this.error.set('La fecha limite es obligatoria'); return; }
    if (this.habsSeleccionadas().length === 0) { this.error.set('Selecciona al menos una habilidad requerida'); return; }

    this.publicando.set(true);
    this.error.set('');
    const payload = { ...this.form, habilidades: this.habsSeleccionadas() };
    this.api.post<any>('/proyectos', payload).subscribe({
      next: res => {
        this.publicando.set(false);
        if (res.exito) { this.toast.exito('Proyecto publicado exitosamente'); this.router.navigate(['/cliente/proyectos']); }
        else this.error.set(res.mensaje);
      },
      error: () => { this.publicando.set(false); this.error.set('Error al publicar el proyecto'); }
    });
  }
}
