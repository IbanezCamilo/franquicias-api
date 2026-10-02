# Arquitectura hexagonal con dominio libre de framework

Para una API con seis operaciones CRUD, lo habitual seria el esquema
`controller → service → repository`. Aqui se eligio **puertos y adaptadores**: el
dominio y los casos de uso no importan nada de Spring ni de MongoDB, y la
infraestructura depende de ellos y no al reves.

## Por que

La logica que distingue esta prueba de un CRUD generado no esta en los endpoints,
sino en las reglas: unicidad de nombres por ambito, stock no negativo y, sobre todo,
el reporte de mayor stock con sus casos borde (sucursal sin catalogo, empate de
stock). Aislar eso del framework permite probarlo en milisegundos, sin levantar
contexto de Spring ni base de datos, que es donde esta el grueso de los 113 tests.

El efecto practico se ve en dos sitios:

- `Franquicia`, `Sucursal` y `Producto` son records sin anotaciones. El documento de
  MongoDB es un tipo **distinto** (`FranquiciaDocument`) con su mapper, de modo que el
  esquema de persistencia puede cambiar sin tocar las reglas de negocio.
- Los casos de uso son POJOs que reciben el puerto por constructor. El cableado vive
  en `CasosDeUsoConfiguration`, no en anotaciones `@Service`, precisamente para que la
  capa de aplicacion no tenga que conocer Spring.

## Consecuencias

El coste es real: mas archivos y un mapeo explicito entre dominio y documento que un
CRUD con `@Document` sobre la entidad se ahorraria. Se acepta porque el beneficio
(tests rapidos y reglas aisladas) se cobra en cada una de las nueve operaciones.

Hay una concesion deliberada a la pureza: el puerto `FranquiciaRepositoryPort` devuelve
`Mono`/`Flux`. Reactor es una libreria, no un framework de aplicacion, y toda la
aplicacion es reactiva de punta a punta; inventar una abstraccion propia para ocultarlo
seria ceremonia sin beneficio.
