# Franquicias

Gestion del catalogo comercial de una cadena de franquicias: que sucursales tiene
cada franquicia y que productos oferta cada sucursal, con su disponibilidad.

## Lenguaje

**Franquicia**:
Marca comercial que agrupa un conjunto de sucursales. Es la unidad completa que se
crea, se consulta y se modifica.
_Evitar_: cadena, empresa, negocio, compania

**Sucursal**:
Local fisico de una franquicia, con su propio catalogo de productos. No existe fuera
de la franquicia a la que pertenece.
_Evitar_: tienda, local, punto de venta, filial, branch

**Producto**:
Articulo ofertado en una sucursal concreta. El mismo articulo vendido en dos
sucursales son dos productos distintos, cada uno con su disponibilidad.
_Evitar_: articulo, item, mercancia, SKU

**Stock**:
Numero de unidades disponibles de un producto en su sucursal. Nunca es negativo;
cero significa agotado, no inexistente.
_Evitar_: inventario, existencias, cantidad, disponibilidad

**Producto destacado**:
Producto con mayor stock de una sucursal. Hay como mucho uno por sucursal: una
sucursal sin productos no tiene producto destacado.
_Evitar_: top, mejor producto, producto estrella

**Nombre**:
Etiqueta legible de una franquicia, sucursal o producto. Es modificable y por tanto
nunca sirve como identificador. Dos nombres que solo difieren en mayusculas o en
espacios de los extremos son el mismo nombre.
_Evitar_: titulo, descripcion, etiqueta

## Reglas del lenguaje

**Unicidad de nombres.** Una franquicia es unica por nombre en todo el sistema. Una
sucursal es unica por nombre dentro de su franquicia. Un producto es unico por nombre
dentro de su sucursal. Dos sucursales de franquicias distintas pueden llamarse igual,
y dos productos de sucursales distintas tambien.

**Composicion, no asociacion.** Una sucursal pertenece a una sola franquicia y una
franquicia es la unidad completa de consulta y modificacion. Lo mismo entre producto y
sucursal. No se habla nunca de "mover" una sucursal a otra franquicia ni un producto a
otra sucursal: esas operaciones no existen en este dominio.

**Identidad separada del nombre.** Renombrar una franquicia, una sucursal o un producto
no la convierte en otra: conserva su identidad, su contenido y sus referencias.
