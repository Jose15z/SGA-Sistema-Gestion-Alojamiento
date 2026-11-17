import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useLocation, useNavigate } from 'react-router';
import { Campo } from '../componentes/Campo';
import { Icono } from '../componentes/Icono';
import { atributosDeError } from '../utilidades/formularios';
import { inicioDeRol, rutaPermitida } from '../datos/navegacion';
import { esquemaLogin, type FormularioLogin } from '../esquemas/login';
import { iniciarSesion } from '../servicios/authServicio';
import { guardarSesion } from '../servicios/sesion';
import { estadoHttp, mensajeDeError } from '../utilidades/errores';

export default function Login() {
  const navegar = useNavigate();
  const ubicacion = useLocation();
  // Ruta que el usuario intentó abrir sin sesión (la envía DisenoAplicacion).
  const desde = (ubicacion.state as { desde?: string } | null)?.desde;
  const [verContrasena, setVerContrasena] = useState(false);

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormularioLogin>({
    resolver: zodResolver(esquemaLogin),
    mode: 'onTouched',   // valida cada campo al salir de él
    defaultValues: { correo: '', contrasena: '', recordar: true },
  });

  // handleSubmit solo llama a esta función si el esquema aprobó los datos
  async function alEnviar({ recordar, ...credenciales }: FormularioLogin) {
    try {
      const sesion = await iniciarSesion(credenciales);
      guardarSesion(sesion, recordar);
      // El rol lo trae el token: cada usuario llega a las pantallas de su rol.
      const destino = desde && rutaPermitida(sesion.rol, desde) ? desde : inicioDeRol(sesion.rol);
      navegar(destino, { replace: true });
    } catch (error) {
      setError('root.servidor', {
        message: estadoHttp(error) === 401 ? 'Correo o contraseña incorrectos' : mensajeDeError(error),
      });
    }
  }

  return (
    <>
      <div className="auth-heading">
        <span className="eyebrow">BIENVENIDO DE NUEVO</span>
        <h2>Ingresa a Siete Raíces</h2>
        <p>Reserva tu estancia o continúa con la operación del alojamiento.</p>
      </div>

      {desde && (
        <p className="form-message" role="status">
          <Icono nombre="escudo" tamano={14} /> Ingrese para continuar.
        </p>
      )}

      <form className="auth-form" onSubmit={handleSubmit(alEnviar)} noValidate>
        <Campo id="correo" etiqueta="Correo electrónico" icono="usuarios" error={errors.correo?.message}>
          <input
            id="correo"
            type="email"
            autoComplete="username"
            placeholder="nombre@correo.com"
            {...atributosDeError('correo', errors.correo?.message)}
            {...register('correo')}
          />
        </Campo>

        <Campo
          id="contrasena"
          etiqueta="Contraseña"
          icono="llave"
          error={errors.contrasena?.message}
          accion={
            <button
              type="button"
              onClick={() => setVerContrasena(!verContrasena)}
              aria-label={verContrasena ? 'Ocultar contraseña' : 'Mostrar contraseña'}
              aria-pressed={verContrasena}
            >
              {verContrasena ? 'Ocultar' : 'Ver'}
            </button>
          }
        >
          <input
            id="contrasena"
            type={verContrasena ? 'text' : 'password'}
            autoComplete="current-password"
            {...atributosDeError('contrasena', errors.contrasena?.message)}
            {...register('contrasena')}
          />
        </Campo>

        <div className="auth-options">
          <label>
            <input type="checkbox" {...register('recordar')} /> Recordarme
          </label>
          <Link to="/recuperar">¿Olvidaste tu contraseña?</Link>
        </div>

        {errors.root?.servidor && (
          <p className="form-error" role="alert">
            <Icono nombre="alerta" tamano={14} /> {errors.root.servidor.message}
          </p>
        )}

        <button type="submit" className="primary full auth-submit" disabled={isSubmitting}>
          {isSubmitting ? 'Ingresando…' : 'Ingresar'} <Icono nombre="flecha" tamano={18} />
        </button>
      </form>

      <p className="auth-switch">
        ¿No tienes cuenta? <Link to="/registro">Regístrate como huésped</Link>
      </p>
    </>
  );
}
