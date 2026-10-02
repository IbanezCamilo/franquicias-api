package com.franquicias.api.infrastructure.out.mongo;

import com.franquicias.api.domain.model.Franquicia;
import com.franquicias.api.domain.model.Producto;
import com.franquicias.api.domain.model.Sucursal;
import com.franquicias.api.domain.validation.Nombres;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

/**
 * Representacion en MongoDB del agregado: un unico documento por franquicia, con
 * las sucursales y sus productos embebidos.
 *
 * <p>Es un tipo distinto del modelo de dominio a proposito. El dominio no lleva
 * anotaciones de persistencia y puede cambiar de forma sin reescribir lo guardado,
 * y el esquema puede cambiar sin tocar las reglas de negocio.
 *
 * @param nombreNormalizado copia en minusculas y sin espacios sobrantes del nombre.
 *                          Existe solo para soportar el indice unico: la unicidad del
 *                          dominio ignora mayusculas, y un indice unico sobre el
 *                          nombre original no lo haria.
 */
@Document(collection = "franquicias")
public record FranquiciaDocument(
        @Id UUID id,
        String nombre,
        @Indexed(unique = true, name = "idx_franquicia_nombre_unico") String nombreNormalizado,
        List<SucursalDocument> sucursales) {

    public record SucursalDocument(UUID id, String nombre, List<ProductoDocument> productos) {
    }

    public record ProductoDocument(UUID id, String nombre, int stock) {
    }

    public static FranquiciaDocument desde(Franquicia franquicia) {
        return new FranquiciaDocument(
                franquicia.id(),
                franquicia.nombre(),
                Nombres.claveDeComparacion(franquicia.nombre()),
                franquicia.sucursales().stream().map(FranquiciaDocument::desde).toList());
    }

    public Franquicia aDominio() {
        return new Franquicia(id, nombre,
                sucursales == null ? List.of()
                        : sucursales.stream().map(FranquiciaDocument::aDominio).toList());
    }

    private static SucursalDocument desde(Sucursal sucursal) {
        return new SucursalDocument(sucursal.id(), sucursal.nombre(),
                sucursal.productos().stream().map(FranquiciaDocument::desde).toList());
    }

    private static Sucursal aDominio(SucursalDocument documento) {
        return new Sucursal(documento.id(), documento.nombre(),
                documento.productos() == null ? List.of()
                        : documento.productos().stream().map(FranquiciaDocument::aDominio).toList());
    }

    private static ProductoDocument desde(Producto producto) {
        return new ProductoDocument(producto.id(), producto.nombre(), producto.stock());
    }

    private static Producto aDominio(ProductoDocument documento) {
        return new Producto(documento.id(), documento.nombre(), documento.stock());
    }
}
