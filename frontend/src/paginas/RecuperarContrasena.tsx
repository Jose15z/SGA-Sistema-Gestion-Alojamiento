import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link } from 'react-router';
import { Campo } from '../componentes/Campo';
import { Icono } from '../componentes/Icono';
import { atributosDeError } from '../utilidades/formularios';
import {
  esquemaRestablecer,
  esquemaSolicitud,
  type FormularioRestablecer,
  type FormularioSolicitud,
} from '../esquemas/recuperacion';
import { restablecerContrasena, solicitarRecuperacion } from '../servicios/authServicio';
import { mensajeDeError } from '../utilidades/errores';

// Dos pasos: 1) pedir el código al correo; 2) escribir el código y la nueva contraseña.
export default function RecuperarContrasena() {
  const [correo, setCorreo] = useState<string | null>(null);
  const [restablecida, setRestablecida] = useState(false);

  if (restablecida) {
    return (
      <>
        <div className="auth-heading">
          <span className="eyebrow">CONTRASEÑA ACTUALIZADA</span>
          <h2>Listo, ya puedes ingresar</h2>
          <p>Usa tu nueva contraseña para entrar a Siete Raíces.</p>
        </div>
        <Link to="/login" className="primary full auth-submit">
          Ir a ingresar <Icono nombre="flecha" tamano={18} />
        </Link>
      </>
    );
  }

  return correo ? (
    <PasoRestablecer correo={correo} onVolver={() => setCorreo(null)} onListo={() => setRestablecida(true)} />
  ) : (
    <PasoSolicitar onEnviado={setCorreo} />
  );
}

function PasoSolicitar({ onEnviado }: { onEnviado: (correo: string) => void }) {
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormularioSolicitud>({
    resolver: zodResolver(esquemaSolicitud),
    mode: 'onTouched',
    defaultValues: { correo: '' },
  });

  async function alEnviar(datos: FormularioSolicitud) {
    try {
      // El backend responde igual exista o no la cuenta, para no revelar qué correos están registrados.
      await solicitarRecuperacion(datos);
      onEnviado(datos.correo);
    } catch (error) {
      setError('root.servidor', { message: mensajeDeError(error) });
    }
  }

  return (
    <>
      <div className="auth-heading">
        <span className="eyebrow">RECUPERAR CONTRASEÑA</span>
        <h2>¿Olvidaste tu contraseña?</h2>
        <p>Te enviaremos un código de 6 dígitos, válido por 15 minutos.</p>
      </div>
      <form className="auth-form" onSubmit={handleSubmit(alEnviar)} noValidate>
        <Campo id="correo" etiqueta="Correo electrónico" icono="usuarios" error={errors.correo?.message}>
          <input
            id="correo"
            type="email"
            autoComplete="email"
            placeholder="nombre@correo.com"
            {...atributosDeError('correo', errors.correo?.message)}
            {...register('correo')}
          />
        </Campo>
        {errors.root?.servidor && (
          <p className="form-error" role="alert">
            <Icono nombre="alerta" tamano={14} /> {errors.root.servidor.message}
          </p>
        )}
        <button type="submit" className="primary full auth-submit" disabled={isSubmitting}>
          {isSubmitting ? 'Enviando…' : 'Enviar código'} <Icono nombre="flecha" tamano={18} />
        </button>
      </form>
      <p className="auth-switch">
        ¿La recordaste? <Link to="/login">Ingresa</Link>
      </p>
    </>
  );
}

interface PasoRestablecerProps {
  correo: string;
  onVolver: () => void;
  onListo: () => void;
}

function PasoRestablecer({ correo, onVolver, onListo }: PasoRestablecerProps) {
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormularioRestablecer>({
    resolver: zodResolver(esquemaRestablecer),
    mode: 'onTouched',
    defaultValues: { codigo: '', contrasenaNueva: '', confirmacion: '' },
  });

  async function alEnviar({ codigo, contrasenaNueva }: FormularioRestablecer) {
    try {
      await restablecerContrasena({ correo, codigo, contrasenaNueva });
      onListo();
    } catch (error) {
      setError('root.servidor', { message: mensajeDeError(error) });
    }
  }

  return (
    <>
      <div className="auth-heading">
        <span className="eyebrow">RECUPERAR CONTRASEÑA</span>
        <h2>Escribe el código</h2>
        <p>Si {correo} tiene una cuenta, le enviamos un código de 6 dígitos.</p>
      </div>
      <form className="auth-form" onSubmit={handleSubmit(alEnviar)} noValidate>
        <Campo id="codigo" etiqueta="Código de verificación" icono="llave" error={errors.codigo?.message}>
          <input
            id="codigo"
            inputMode="numeric"
            autoComplete="one-time-code"
            maxLength={6}
            placeholder="000000"
            {...atributosDeError('codigo', errors.codigo?.message)}
            {...register('codigo')}
          />
        </Campo>
        <Campo id="contrasenaNueva" etiqueta="Nueva contraseña" error={errors.contrasenaNueva?.message}>
          <input
            id="contrasenaNueva"
            type="password"
            autoComplete="new-password"
            placeholder="Mínimo 8 caracteres"
            {...atributosDeError('contrasenaNueva', errors.contrasenaNueva?.message)}
            {...register('contrasenaNueva')}
          />
        </Campo>
        <Campo id="confirmacion" etiqueta="Confirmar contraseña" error={errors.confirmacion?.message}>
          <input
            id="confirmacion"
            type="password"
            autoComplete="new-password"
            placeholder="Repite la contraseña"
            {...atributosDeError('confirmacion', errors.confirmacion?.message)}
            {...register('confirmacion')}
          />
        </Campo>
        <p className="password-rule">
          <Icono nombre="escudo" tamano={14} /> Mínimo 8 caracteres, con una mayúscula y un número.
        </p>
        {errors.root?.servidor && (
          <p className="form-error" role="alert">
            <Icono nombre="alerta" tamano={14} /> {errors.root.servidor.message}
          </p>
        )}
        <button type="submit" className="primary full auth-submit" disabled={isSubmitting}>
          {isSubmitting ? 'Guardando…' : 'Cambiar contraseña'} <Icono nombre="flecha" tamano={18} />
        </button>
      </form>
      <p className="auth-switch">
        <button type="button" onClick={onVolver}>
          Usar otro correo
        </button>
      </p>
    </>
  );
}
