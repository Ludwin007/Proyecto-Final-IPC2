import { Injectable, signal, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from './api.service';
import { Observable, tap } from 'rxjs';

export interface UsuarioSesion {
  idUsuario: number;
  username: string;
  nombreCompleto: string;
  tipoRol: 'CLIENTE' | 'FREELANCER' | 'ADMIN';
  perfilCompleto: boolean;
  saldo: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private api = inject(ApiService);
  private router = inject(Router);
  private _usuario = signal<UsuarioSesion | null>(null);
  private _token = signal<string | null>(null);

  usuario = this._usuario.asReadonly();
  token = this._token.asReadonly();
  autenticado = computed(() => this._token() !== null);
  esAdmin = computed(() => this._usuario()?.tipoRol === 'ADMIN');
  esCliente = computed(() => this._usuario()?.tipoRol === 'CLIENTE');
  esFreelancer = computed(() => this._usuario()?.tipoRol === 'FREELANCER');

  constructor() {
    const tok = localStorage.getItem('cw_token');
    const usr = localStorage.getItem('cw_usuario');

    if (tok && usr) {
      try {
        this._token.set(tok);
        this._usuario.set(JSON.parse(usr));
      } catch {
        this.logout();
      }
    }

    window.addEventListener('storage', (event) => {
      if (event.key === 'cw_usuario' && event.newValue) {
        this._usuario.set(JSON.parse(event.newValue));
      }
      if (event.key === 'cw_token' && !event.newValue) {
        this.logout();
      }
    });
  }

  login(username: string, contrasena: string): Observable<any> {
    return this.api.post<any>('/auth/login', { 
      username: username, 
      contrasena: contrasena,
      password: contrasena 
    }).pipe(
      tap(res => {
        if (res && res.exito) {
          this._token.set(res.datos.token);
          this._usuario.set(res.datos.usuario);
          localStorage.setItem('cw_token', res.datos.token);
          localStorage.setItem('cw_usuario', JSON.stringify(res.datos.usuario));
        }
      })
    );
  }

  logout(): void {
    this._token.set(null);
    this._usuario.set(null);
    localStorage.removeItem('cw_token');
    localStorage.removeItem('cw_usuario');
    this.router.navigate(['/login']);
  }

  actualizarPerfilCompleto(): void {
    const u = this._usuario();
    if (u) {
      const actualizado = { ...u, perfilCompleto: true };
      this._usuario.set(actualizado);
      localStorage.setItem('cw_usuario', JSON.stringify(actualizado));
    }
  }

  actualizarSaldo(nuevoSaldo: number): void {
    const u = this._usuario();
    if (u) {
      const actualizado = { ...u, saldo: nuevoSaldo };
      this._usuario.set(actualizado);
      localStorage.setItem('cw_usuario', JSON.stringify(actualizado));
    }
  }

  getToken(): string | null {
    return this._token();
  }
}