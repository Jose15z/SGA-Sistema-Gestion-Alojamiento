import { Icono } from './Icono';

interface MarcaProps {
  tamanoIcono?: number;
}

export function Marca({ tamanoIcono = 25 }: MarcaProps) {
  return (
    <>
      <span className="brand-mark">
        <Icono nombre="hoja" tamano={tamanoIcono} />
      </span>
      <span>
        <strong>Siete Raíces</strong>
        <small>Apartamentos turísticos</small>
      </span>
    </>
  );
}
