package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Modelo de persistencia del agregado Folio; los movimientos nunca se borran (RN-16). */
@Entity
@Table(name = "folios")
@Getter @Setter @NoArgsConstructor
public class FolioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_reserva", nullable = false, unique = true, length = 20)
    private String codigoReserva;

    @Column(name = "cerrado", nullable = false)
    private boolean cerrado;

    @Column(name = "alojamiento_liquidado", nullable = false)
    private boolean alojamientoLiquidado;

    @Column(name = "autorizacion_cierre", length = 200)
    private String autorizacionCierre;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "folio_cargos", joinColumns = @JoinColumn(name = "folio_id"))
    @OrderColumn(name = "posicion")
    private List<CargoEmbeddable> cargos = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "folio_pagos", joinColumns = @JoinColumn(name = "folio_id"))
    @OrderColumn(name = "posicion")
    private List<PagoEmbeddable> pagos = new ArrayList<>();
}
