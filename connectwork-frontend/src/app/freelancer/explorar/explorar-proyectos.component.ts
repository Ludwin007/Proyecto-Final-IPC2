import { Component, signal, inject, OnInit } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-explorar-proyectos',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="topbar">
      <div class="topbar-titulo">
        <h2>Explorar proyectos</h2>
        <p>Encuentra proyectos que se adapten a tus habilidades</p>
      </div>
    </div>

    <div class="card" style="margin-bottom:20px">
      <div class="barra-filtros" style="margin-bottom:0">
        <select [(ngModel)]="filtroCategoria" (ngModelChange)="filtrar()" style="flex:1;min-width:160px">
          <option value="">Todas las categorias</option>
          @for (c of categorias(); track c.idCategoria) {
            <option [value]="c.idCategoria">{{ c.nombre }}</option>
          }
        </select>
        <input type="number" [(ngModel)]="presMin" (ngModelChange)="filtrar()" placeholder="Presup. min (Q)" style="width:140px" />
        <input type="number" [(ngModel)]="presMax" (ngModelChange)="filtrar()" placeholder="Presup. max (Q)" style="width:140px" />
        <button class="btn btn-contorno btn-sm" (click)="limpiar()">Limpiar filtros</button>
      </div>
    </div>

    @if (cargando()) {
      <div class="cargando-contenedor"><div class="spinner"></div><p>Cargando proyectos...</p></div>
    } @else if (proyectos().length === 0) {
      <div class="card estado-vacio">
        <span class="icono-vacio">🔍</span>
        <h3>No se encontraron proyectos</h3>
        <p>Intenta cambiar los filtros o espera nuevas publicaciones</p>
      </div>
    } @else {
      <div style="display:flex;flex-direction:column;gap:16px">
        @for (p of proyectos(); track p.idProyecto) {
          <a routerLink="/freelancer/explorar/{{ p.idProyecto }}" style="display:block">
            <div class="proyecto-card">
              <div class="proyecto-card-header">
                <div>
                  <div class="proyecto-card-titulo">{{ p.titulo }}</div>
                  <span style="font-size:12px;color:var(--texto-suave)">{{ p.nombreCategoria }}</span>
                </div>
                <strong style="font-size:18px;color:var(--verde);flex-shrink:0">Q {{ p.presupuestoMax | number:'1.2-2' }}</strong>
              </div>
              <p class="proyecto-card-desc">{{ p.descripcion | slice:0:160 }}{{ p.descripcion?.length > 160 ? '...' : '' }}</p>
              <div class="proyecto-card-meta">
                <span>👤 {{ p.nombreCliente }}</span>
                <span>📅 Limite: {{ p.fechaLimite | date:'dd/MM/yyyy' }}</span>
                <span>📌 {{ p.fechaPublicacion | date:'dd/MM/yyyy' }}</span>
              </div>
              @if (p.habilidades?.length > 0) {
                <div class="chips-lista" style="margin-top:10px">
                  @for (h of p.habilidades.slice(0,5); track h.idHabilidad) {
                    <span class="chip">{{ h.nombre }}</span>
                  }
                  @if (p.habilidades.length > 5) {
                    <span class="chip" style="background:#f1f5f9;color:var(--texto-suave)">+{{ p.habilidades.length - 5 }}</span>
                  }
                </div>
              }
              <div class="proyecto-card-footer">
                <span style="font-size:12px;color:var(--verde);font-weight:600">ABIERTO</span>
                <span class="btn btn-primario btn-sm">Ver detalle →</span>
              </div>
            </div>
          </a>
        }
      </div>
    }
  `
})
export class ExplorarProyectosComponent implements OnInit {
  private api = inject(ApiService);

  cargando = signal(true);
  proyectos = signal<any[]>([]);
  categorias = signal<any[]>([]);
  filtroCategoria = '';
  presMin = '';
  presMax = '';

  ngOnInit(): void {
    this.api.get<any>('/catalogo/categorias').subscribe(res => this.categorias.set(res.datos?.categorias ?? []));
    this.filtrar();
  }

  filtrar(): void {
    this.cargando.set(true);
    const params: Record<string, string> = {};
    if (this.filtroCategoria) params['categoria'] = this.filtroCategoria;
    if (this.presMin) params['presMin'] = String(this.presMin);
    if (this.presMax) params['presMax'] = String(this.presMax);
    this.api.get<any>('/proyectos', params).subscribe({
      next: res => { this.proyectos.set(res.datos?.proyectos ?? []); this.cargando.set(false); },
      error: () => this.cargando.set(false)
    });
  }

  limpiar(): void { this.filtroCategoria = ''; this.presMin = ''; this.presMax = ''; this.filtrar(); }
}
