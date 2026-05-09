import { Component, signal, inject, OnInit, computed } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [FormsModule, CommonModule],
  template: `
    <div class="topbar">
      <div class="topbar-titulo">
        <h2>Usuarios</h2>
        <p>Gestiona los clientes y freelancers registrados</p>
      </div>
    </div>

    <div class="card">
      <div class="barra-filtros">
        <input type="text" [(ngModel)]="busqueda" (ngModelChange)="filtrar()" placeholder="Buscar por nombre o usuario..." style="flex:1;min-width:200px" />
        <select [(ngModel)]="filtroRol" (ngModelChange)="filtrar()">
          <option value="">Todos los roles</option>
          <option value="CLIENTE">Clientes</option>
          <option value="FREELANCER">Freelancers</option>
        </select>
        <select [(ngModel)]="filtroEstado" (ngModelChange)="filtrar()">
          <option value="">Todos</option>
          <option value="1">Activos</option>
          <option value="0">Desactivados</option>
        </select>
      </div>

      @if (cargando()) {
        <div class="cargando-contenedor"><div class="spinner"></div><p>Cargando usuarios...</p></div>
      } @else if (usuariosFiltrados().length === 0) {
        <div class="estado-vacio">
          <span class="icono-vacio">👤</span>
          <h3>No se encontraron usuarios</h3>
          <p>Intenta cambiar los filtros de busqueda</p>
        </div>
      } @else {
        <div class="tabla-contenedor">
          <table class="tabla">
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Username</th>
                <th>Correo</th>
                <th>Rol</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              @for (u of usuariosFiltrados(); track u.idUsuario) {
                <tr>
                  <td><strong>{{ u.nombreCompleto }}</strong></td>
                  <td style="color:var(--texto-suave)">{{ u.username }}</td>
                  <td style="color:var(--texto-suave)">{{ u.correo }}</td>
                  <td>
                    <span class="badge" [class]="u.tipoRol === 'CLIENTE' ? 'badge-azul' : 'badge-purpura'">
                      {{ u.tipoRol }}
                    </span>
                  </td>
                  <td>
                    <span class="badge" [class]="u.activo ? 'badge-verde' : 'badge-rojo'">
                      {{ u.activo ? 'Activo' : 'Desactivado' }}
                    </span>
                  </td>
                  <td>
                    <button class="btn btn-sm" [class]="u.activo ? 'btn-peligro' : 'btn-exito'" (click)="cambiarEstado(u)">
                      {{ u.activo ? 'Desactivar' : 'Activar' }}
                    </button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>
  `
})
export class UsuariosComponent implements OnInit {
  private api = inject(ApiService);
  private toast = inject(ToastService);

  cargando = signal(true);
  usuarios = signal<any[]>([]);
  usuariosFiltrados = signal<any[]>([]);
  busqueda = '';
  filtroRol = '';
  filtroEstado = '';

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.cargando.set(true);
    this.api.get<any>('/admin/usuarios').subscribe({
      next: res => {
        this.usuarios.set(res.datos?.usuarios ?? []);
        this.filtrar();
        this.cargando.set(false);
      },
      error: () => { this.toast.error('Error al cargar usuarios'); this.cargando.set(false); }
    });
  }

  filtrar(): void {
    let lista = this.usuarios();
    if (this.busqueda.trim()) {
      const b = this.busqueda.toLowerCase();
      lista = lista.filter(u => u.nombreCompleto?.toLowerCase().includes(b) || u.username?.toLowerCase().includes(b));
    }
    if (this.filtroRol) lista = lista.filter(u => u.tipoRol === this.filtroRol);
    if (this.filtroEstado !== '') lista = lista.filter(u => String(u.activo ? 1 : 0) === this.filtroEstado);
    this.usuariosFiltrados.set(lista);
  }

  cambiarEstado(u: any): void {
    const accion = u.activo ? 'desactivar' : 'activar';
    this.api.put<any>(`/admin/usuarios/${u.idUsuario}/${accion}`).subscribe({
      next: res => {
        if (res.exito) {
          this.toast.exito(res.mensaje);
          this.usuarios.update(lista => lista.map(x => x.idUsuario === u.idUsuario ? { ...x, activo: !x.activo } : x));
          this.filtrar();
        } else { this.toast.error(res.mensaje); }
      },
      error: () => this.toast.error('Error al actualizar el usuario')
    });
  }
}
