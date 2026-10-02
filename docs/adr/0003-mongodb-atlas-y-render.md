# MongoDB Atlas sobre DynamoDB o PostgreSQL, y Render como runtime

El enunciado deja libre el motor de persistencia (Redis, MySQL, MongoDB, DynamoDB)
siempre que este en la nube. Se eligio **MongoDB Atlas M0** con el driver reactivo, y
**Render** como runtime de la aplicacion. Ambas decisiones estuvieron condicionadas
por una restriccion dura: **presupuesto cero y dos dias de plazo**.

## Por que Atlas

| Opcion | Por que se descarto |
|---|---|
| DynamoDB | El SDK async funciona bien, pero modelar el arbol y montar IAM se come medio dia, y la cuenta de AWS exige tarjeta |
| PostgreSQL / MySQL con R2DBC | Relacional es razonable, pero R2DBC obliga a escribir a mano el mapeo de las relaciones anidadas y el reporte, con menos ergonomia que Spring Data |
| Redis | Es un almacen clave-valor: forzar en el un arbol consultable seria modelar contra la herramienta |

Atlas gana por tres razones concretas: el modelo documental calza con la decision de
embeber el agregado (ver [ADR 0002](./0002-modelo-embebido.md)); el driver reactivo
esta soportado de primera mano por Spring Data, lo que encaja con WebFlux; y la capa
M0 es gratuita de forma permanente y **sin tarjeta de credito**, con un provider de
Terraform oficial que permite cumplir el extra de infraestructura como codigo.

## Por que Render

Sin tarjeta de credito las opciones se estrechan mucho: Fly.io y Railway ya la exigen,
y ECS o App Runner ademas costarian medio dia de IAM y redes. Render despliega desde el
`Dockerfile` del repositorio, redespliega en cada push a `main` y acepta un health check.

**Contrapartida conocida:** en el plan gratuito el servicio se duerme tras 15 minutos
de inactividad, y el primer request posterior tarda del orden de un minuto en
responder mientras el contenedor y la JVM arrancan. Se asume y se avisa en el README,
porque ocultarlo seria peor que explicarlo.

**Segunda contrapartida, de seguridad:** Render free no asigna IP de salida estatica,
asi que la lista de acceso de Atlas tiene que abrirse a `0.0.0.0/0`. Lo que protege es
el usuario de base de datos, acotado a `readWrite` sobre una sola base, y TLS, que
Atlas impone siempre. La solucion correcta seria VPC peering o PrivateLink, que Atlas
ofrece desde el nivel M10 de pago. Queda documentado en el `main.tf` y en el README.

## Nota sobre versiones

Se uso **Spring Boot 4.1.1**, no la rama 3.5 inicialmente prevista, simplemente porque
Spring Initializr ya no ofrece la 3.5 entre sus versiones estables. Obligo a dos
ajustes: los starters de test modulares (`spring-boot-starter-webflux-test` en lugar de
`spring-boot-starter-test`) y el espacio de nombres `spring.mongodb.*`, que en Boot 4
sustituye a `spring.data.mongodb.*` para la configuracion de conexion.
