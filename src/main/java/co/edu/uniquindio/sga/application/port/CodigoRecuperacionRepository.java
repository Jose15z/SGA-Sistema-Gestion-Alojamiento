package co.edu.uniquindio.sga.application.port;

import co.edu.uniquindio.sga.application.seguridad.CodigoRecuperacion;
import co.edu.uniquindio.sga.application.seguridad.CorreoElectronico;

import java.util.List;

/** Puerto de los códigos de recuperación de contraseña (7.2). */
public interface CodigoRecuperacionRepository {

    List<CodigoRecuperacion> buscarNoUsadosPorCorreo(CorreoElectronico correo);

    void guardar(CodigoRecuperacion codigo);
}
