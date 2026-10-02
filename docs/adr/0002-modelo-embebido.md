# Un unico documento por franquicia, con sucursales y productos embebidos

La coleccion `franquicias` guarda un documento por franquicia que contiene el arbol
completo: sus sucursales y, dentro de cada una, sus productos. Se descarto el modelo
de tres colecciones referenciadas por `franquiciaId` y `sucursalId`.

## Por que

El enunciado describe una **composicion**, no una asociacion: "una franquicia se
compone por un nombre y una lista de sucursales". Una sucursal no tiene ciclo de vida
fuera de su franquicia, y no existe ninguna operacion que la mueva a otra. Cuando el
ciclo de vida es ese, el agregado es la franquicia entera, y embeber lo refleja.

Consecuencias practicas que lo refuerzan:

- El reporte de mayor stock (criterio 7) se resuelve con **una sola lectura** y un
  calculo en memoria sobre el dominio, sin `$lookup` ni agregaciones, y por tanto se
  puede probar sin base de datos.
- Cada escritura es atomica a nivel de franquicia sin necesidad de transacciones
  multi-documento.
- Desaparece una clase entera de errores: no hay referencias huerfanas ni validaciones
  de "existe el padre" repartidas por el codigo.

El limite de 16 MB por documento no es una preocupacion realista a esta escala: harian
falta decenas de miles de productos en una sola franquicia para acercarse.

## Consecuencias

**No hay bloqueo optimista, y eso puede causar actualizaciones perdidas.** Todas las
escrituras son leer-modificar-escribir sobre el documento completo: si dos peticiones
modifican la *misma* franquicia a la vez, la ultima en guardar pisa a la primera. La
solucion estandar es un campo `@Version` en el documento, que haria fallar la segunda
escritura con `OptimisticLockingFailureException` para reintentarla o devolver 409. Se
dejo fuera porque obligaria a que el modelo de dominio arrastrase un numero de version
que no significa nada para el negocio, o a complicar el mapper para transportarlo
aparte. Para el alcance de esta prueba la ventana es irrelevante; en un sistema con
concurrencia real sobre la misma franquicia, seria lo primero que habria que anadir.

Un caso aparte si esta resuelto: **la unicidad del nombre de franquicia**, que exige
mirar fuera del agregado. La comprobacion previa en el caso de uso deja una ventana de
carrera, de modo que el adaptador se apoya en un indice unico sobre `nombreNormalizado`
y traduce `DuplicateKeyException` a 409. La comprobacion previa da un buen mensaje; el
indice garantiza la regla.
