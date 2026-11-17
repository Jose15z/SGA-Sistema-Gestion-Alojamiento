import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { EncabezadoPagina } from '../componentes/EncabezadoPagina';
import { Icono } from '../componentes/Icono';
import { esquemaCrearUsuario, type FormularioCrearUsuario } from '../esquemas/usuario';
import type { UsuarioResponse } from '../modelos/auth';
import { nombreRol, type Rol } from '../modelos/rol';
import { crearUsuario } from '../servicios/usuarioServicio';
import { hoyISO } from '../utilidades/fechas';
import { mostrarErrorDelServidor } from '../utilidades/formularios';

const CAMPOS = ['nombreCompleto', 'correo', 'documento', 'telefono', 'fechaNacimiento', 'contrasena', 'rol'] as const;

// Primero los roles del personal: los huéspedes normalmente se registran solos.
const ROLES: Rol[] = ['RECEPCIONISTA', 'PERSONAL_SERVICIO', 'ADMINISTRADOR', 'HUESPED'];

const VALORES_INICIALES: FormularioCrearUsuario = {
  nombreCompleto: '',
  correo: '',
  documento: '',
  telefono: '',
  fechaNacimiento: '',
  contrasena: '',
  confirmacion: '',
  rol: 'RECEPCIONISTA',
};

// Solo el ADMINISTRADOR llega aquí (navegación por rol) y solo él puede usar POST /api/usuarios (backend).
export default function UsuariosInternos() {
  const [creado, setCreado] = useState<UsuarioResponse | null>(null);
  const {
    register,
    handleSubmit,
    setError,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<FormularioCrearUsuario>({
    resolver: zodResolver(esquemaCrearUsuario),
    mode: 'onTouched',
    defaultValues: VALORES_INICIALES,
  });

  async function alEnviar({ confirmacion: _confirmacion, ...datos }: FormularioCrearUsuario) {
    setCreado(null);
    try {
      const usuario = await crearUsuario(datos);
      setCreado(usuario);
      reset(VALORES_INICIALES);
    } catch (error) {
      mostrarErrorDelServidor(error, setError, CAMPOS);
    }
  }

  // Error de un campo del formulario: borde rojo y mensaje enlazado al input.
  function error(campo: keyof FormularioCrearUsuario) {
    const mensaje = errors[campo]?.message;
    return {
      mensaje: mensaje ? (
        <span className="error" id={`${campo}-error`} role="alert">
          {mensaje}
        </span>
      ) : null,
      atributos: {
        'aria-invalid': mensaje ? true : undefined,
        'aria-describedby': mensaje ? `${campo}-error` : undefined,
      },
    };
  }

  return (
    <section>
      <EncabezadoPagina
        antetitulo="ADMINISTRACIÓN"
        titulo="Usuarios del alojamiento"
        descripcion="Solo el administrador crea cuentas del personal y les asigna su rol. Los huéspedes se registran desde el portal."
      />

      <form className="form-section" onSubmit={handleSubmit(alEnviar)} noValidate>
        <div className="form-section-title">
          <span>1</span>
          <div>
            <h3>Nuevo usuario</h3>
            <p>La persona ingresará con este correo y la contraseña inicial.</p>
          </div>
        </div>

        <div className="form-grid">
          <label htmlFor="nombreCompleto" className="wide">
            NOMBRE COMPLETO
            <input id="nombreCompleto" autoComplete="off" {...error('nombreCompleto').atributos} {...register('nombreCompleto')} />
            {error('nombreCompleto').mensaje}
          </label>
          <label htmlFor="correo">
            CORREO ELECTRÓNICO
            <input id="correo" type="email" autoComplete="off" {...error('correo').atributos} {...register('correo')} />
            {error('correo').mensaje}
          </label>
          <label htmlFor="rol">
            ROL
            <select id="rol" {...error('rol').atributos} {...register('rol')}>
              {ROLES.map((rol) => (
                <option key={rol} value={rol}>
                  {nombreRol[rol]}
                </option>
              ))}
            </select>
            {error('rol').mensaje}
          </label>
          <label htmlFor="documento">
            DOCUMENTO DE IDENTIDAD
            <input id="documento" {...error('documento').atributos} {...register('documento')} />
            {error('documento').mensaje}
          </label>
          <label htmlFor="telefono">
            TELÉFONO
            <input id="telefono" type="tel" {...error('telefono').atributos} {...register('telefono')} />
            {error('telefono').mensaje}
          </label>
          <label htmlFor="fechaNacimiento">
            FECHA DE NACIMIENTO
            <input id="fechaNacimiento" type="date" max={hoyISO()} {...error('fechaNacimiento').atributos} {...register('fechaNacimiento')} />
            {error('fechaNacimiento').mensaje}
          </label>
          <span />
          <label htmlFor="contrasena">
            CONTRASEÑA INICIAL
            <input id="contrasena" type="password" autoComplete="new-password" {...error('contrasena').atributos} {...register('contrasena')} />
            {error('contrasena').mensaje}
          </label>
          <label htmlFor="confirmacion">
            CONFIRMAR CONTRASEÑA
            <input id="confirmacion" type="password" autoComplete="new-password" {...error('confirmacion').atributos} {...register('confirmacion')} />
            {error('confirmacion').mensaje}
          </label>
          <p className="password-rule wide">
            <Icono nombre="escudo" tamano={14} /> Mínimo 8 caracteres, con una mayúscula y un número.
          </p>
        </div>

        {errors.root?.servidor && (
          <p className="form-error" role="alert">
            <Icono nombre="alerta" tamano={14} /> {errors.root.servidor.message}
          </p>
        )}
        {creado && (
          <p className="form-message" role="status">
            <Icono nombre="chequeo" tamano={14} /> Se creó la cuenta de {creado.nombreCompleto} ({creado.correo}) con el rol{' '}
            {nombreRol[creado.rol]}.
          </p>
        )}

        <div className="button-row">
          <button type="submit" className="primary" disabled={isSubmitting}>
            <Icono nombre="mas" /> {isSubmitting ? 'Creando…' : 'Crear usuario'}
          </button>
        </div>
      </form>
    </section>
  );
}
