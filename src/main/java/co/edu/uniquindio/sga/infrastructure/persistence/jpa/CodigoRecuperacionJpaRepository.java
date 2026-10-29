package co.edu.uniquindio.sga.infrastructure.persistence.jpa;

import co.edu.uniquindio.sga.application.port.CodigoRecuperacionRepository;
import co.edu.uniquindio.sga.application.seguridad.CodigoRecuperacion;
import co.edu.uniquindio.sga.application.seguridad.CorreoElectronico;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.CodigoRecuperacionEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.CodigoRecuperacionJpaDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Adaptador JPA del puerto CodigoRecuperacionRepository. */
@Repository
@Transactional
public class CodigoRecuperacionJpaRepository implements CodigoRecuperacionRepository {

    private final CodigoRecuperacionJpaDataRepository dataRepository;

    public CodigoRecuperacionJpaRepository(CodigoRecuperacionJpaDataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodigoRecuperacion> buscarNoUsadosPorCorreo(CorreoElectronico correo) {
        return dataRepository.findByCorreoAndUsadoFalse(correo.valor()).stream()
                .map(e -> new CodigoRecuperacion(e.getIdentificacion(), new CorreoElectronico(e.getCorreo()),
                        e.getCodigoCifrado(), e.getExpiraEn(), e.isUsado()))
                .toList();
    }

    @Override
    public void guardar(CodigoRecuperacion codigo) {
        CodigoRecuperacionEntity entity = new CodigoRecuperacionEntity();
        entity.setIdentificacion(codigo.getIdentificacion());
        entity.setCorreo(codigo.getCorreo().valor());
        entity.setCodigoCifrado(codigo.getCodigoCifrado());
        entity.setExpiraEn(codigo.getExpiraEn());
        entity.setUsado(codigo.estaUsado());
        dataRepository.save(entity);
    }
}
