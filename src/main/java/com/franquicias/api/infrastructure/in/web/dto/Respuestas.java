package com.franquicias.api.infrastructure.in.web.dto;

import com.franquicias.api.domain.model.Franquicia;
import com.franquicias.api.domain.model.Producto;
import com.franquicias.api.domain.model.ProductoDestacado;
import com.franquicias.api.domain.model.Sucursal;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

/**
 * Cuerpos de respuesta de la API.
 *
 * <p>Son tipos distintos de los del dominio a proposito: el contrato publico puede
 * evolucionar sin arrastrar al modelo, y el modelo no queda expuesto por accidente.
 * Cada respuesta sabe construirse desde su equivalente de dominio.
 */
public final class Respuestas {

    private Respuestas() {
    }

    @Schema(name = "Producto")
    public record ProductoResponse(UUID id, String nombre, int stock) {

        public static ProductoResponse desde(Producto producto) {
            return new ProductoResponse(producto.id(), producto.nombre(), producto.stock());
        }
    }

    @Schema(name = "Sucursal")
    public record SucursalResponse(UUID id, String nombre, List<ProductoResponse> productos) {

        public static SucursalResponse desde(Sucursal sucursal) {
            return new SucursalResponse(sucursal.id(), sucursal.nombre(),
                    sucursal.productos().stream().map(ProductoResponse::desde).toList());
        }
    }

    @Schema(name = "Franquicia")
    public record FranquiciaResponse(UUID id, String nombre, List<SucursalResponse> sucursales) {

        public static FranquiciaResponse desde(Franquicia franquicia) {
            return new FranquiciaResponse(franquicia.id(), franquicia.nombre(),
                    franquicia.sucursales().stream().map(SucursalResponse::desde).toList());
        }
    }

    /**
     * Vista reducida para el listado. No incluye el arbol: una respuesta con todas las
     * franquicias y todos sus productos crece sin limite. El cliente usa este resumen
     * para decidir a cual entrar y pide el detalle de una sola.
     *
     * <p>Ojo con la lectura facil: esto recorta la respuesta, no la consulta. El
     * adaptador sigue trayendo los documentos completos de MongoDB. Reducir tambien la
     * lectura exigiria una proyeccion y paginacion.
     */
    @Schema(name = "ResumenFranquicia",
            description = "Franquicia con el recuento de su contenido, sin el detalle")
    public record ResumenFranquiciaResponse(
            UUID id,
            String nombre,
            int sucursales,
            int productos) {

        public static ResumenFranquiciaResponse desde(Franquicia franquicia) {
            return new ResumenFranquiciaResponse(
                    franquicia.id(),
                    franquicia.nombre(),
                    franquicia.sucursales().size(),
                    franquicia.sucursales().stream()
                            .mapToInt(sucursal -> sucursal.productos().size())
                            .sum());
        }
    }

    @Schema(name = "ProductoDestacado",
            description = "Producto con mayor stock de una sucursal, con la sucursal a la que pertenece")
    public record ProductoDestacadoResponse(
            UUID sucursalId,
            String sucursalNombre,
            ProductoResponse producto) {

        public static ProductoDestacadoResponse desde(ProductoDestacado destacado) {
            return new ProductoDestacadoResponse(
                    destacado.sucursalId(),
                    destacado.sucursalNombre(),
                    ProductoResponse.desde(destacado.producto()));
        }
    }
}
