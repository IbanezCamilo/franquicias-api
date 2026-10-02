package com.franquicias.api.infrastructure.in.web;

import com.franquicias.api.application.usecase.ActualizarStock;
import com.franquicias.api.application.usecase.AgregarProducto;
import com.franquicias.api.application.usecase.EliminarProducto;
import com.franquicias.api.application.usecase.RenombrarProducto;
import com.franquicias.api.infrastructure.in.web.dto.Peticiones;
import com.franquicias.api.infrastructure.in.web.dto.Respuestas.ProductoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/franquicias/{franquiciaId}/sucursales/{sucursalId}/productos")
@Tag(name = "Productos", description = "Catalogo de productos de una sucursal")
public class ProductoController {

    private final AgregarProducto agregarProducto;
    private final EliminarProducto eliminarProducto;
    private final ActualizarStock actualizarStock;
    private final RenombrarProducto renombrarProducto;

    public ProductoController(AgregarProducto agregarProducto,
                              EliminarProducto eliminarProducto,
                              ActualizarStock actualizarStock,
                              RenombrarProducto renombrarProducto) {
        this.agregarProducto = agregarProducto;
        this.eliminarProducto = eliminarProducto;
        this.actualizarStock = actualizarStock;
        this.renombrarProducto = renombrarProducto;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Agrega un nuevo producto a la sucursal")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado"),
            @ApiResponse(responseCode = "400", description = "Nombre invalido o stock negativo", content = @Content),
            @ApiResponse(responseCode = "404", description = "La franquicia o la sucursal no existen", content = @Content),
            @ApiResponse(responseCode = "409", description = "La sucursal ya tiene un producto con ese nombre", content = @Content)
    })
    public Mono<ProductoResponse> agregar(@PathVariable UUID franquiciaId,
                                          @PathVariable UUID sucursalId,
                                          @Valid @RequestBody Peticiones.CrearProducto peticion) {
        return agregarProducto.ejecutar(franquiciaId, sucursalId, peticion.nombre(), peticion.stock())
                .map(ProductoResponse::desde);
    }

    @DeleteMapping("/{productoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina un producto de la sucursal")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto eliminado"),
            @ApiResponse(responseCode = "404", description = "La franquicia, la sucursal o el producto no existen", content = @Content)
    })
    public Mono<Void> eliminar(@PathVariable UUID franquiciaId,
                               @PathVariable UUID sucursalId,
                               @PathVariable UUID productoId) {
        return eliminarProducto.ejecutar(franquiciaId, sucursalId, productoId);
    }

    @PatchMapping("/{productoId}/stock")
    @Operation(summary = "Modifica el stock de un producto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock actualizado"),
            @ApiResponse(responseCode = "400", description = "Stock negativo o ausente", content = @Content),
            @ApiResponse(responseCode = "404", description = "La franquicia, la sucursal o el producto no existen", content = @Content)
    })
    public Mono<ProductoResponse> actualizarStock(@PathVariable UUID franquiciaId,
                                                  @PathVariable UUID sucursalId,
                                                  @PathVariable UUID productoId,
                                                  @Valid @RequestBody Peticiones.ActualizarStock peticion) {
        return actualizarStock.ejecutar(franquiciaId, sucursalId, productoId, peticion.stock())
                .map(ProductoResponse::desde);
    }

    @PatchMapping("/{productoId}/nombre")
    @Operation(summary = "Actualiza el nombre del producto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nombre actualizado"),
            @ApiResponse(responseCode = "400", description = "Nombre invalido", content = @Content),
            @ApiResponse(responseCode = "404", description = "La franquicia, la sucursal o el producto no existen", content = @Content),
            @ApiResponse(responseCode = "409", description = "El nombre pertenece a otro producto de la sucursal", content = @Content)
    })
    public Mono<ProductoResponse> renombrar(@PathVariable UUID franquiciaId,
                                            @PathVariable UUID sucursalId,
                                            @PathVariable UUID productoId,
                                            @Valid @RequestBody Peticiones.CambiarNombre peticion) {
        return renombrarProducto.ejecutar(franquiciaId, sucursalId, productoId, peticion.nombre())
                .map(ProductoResponse::desde);
    }
}
