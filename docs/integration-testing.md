# Pruebas de integración MySQL

## Estado y límites de esta entrega

Solo se prepararon los archivos. No se crearon bases ni usuarios y no se ejecutaron
el inicializador ni las pruebas que escriben en MySQL. Las pruebas unitarias de
aislamiento usan dobles JDBC sin red. Los DAO y el SQL oficial no cambian.

## Separación de ejecuciones

- `mvn clean test`: compila todas las pruebas y ejecuta solamente las unitarias.
  Surefire excluye `*IT.java` y la etiqueta `integration`.
- Failsafe 3.2.5 existe únicamente dentro del perfil `integration`, utiliza una JVM
  separada por clase y trabaja en `target/`, sin leer el `db.properties` raíz.
- Activar el perfil no basta para escribir: el soporte también exige
  `integration.write.enabled=true`. Si falta, falla antes de abrir conexiones.

No ejecutar todavía los comandos de inicialización o integración de las secciones
siguientes: requieren aprobación posterior del propietario del proyecto.

## Cuentas y archivos privados

Un administrador deberá crear posteriormente dos cuentas no root, sin roles,
permisos globales, permisos sobre otras bases ni `GRANT OPTION`:

| Cuenta sugerida | Permisos exclusivamente sobre `erp_little_horizons_test.*` |
| --- | --- |
| `erp_it_setup` | `CREATE`, `SELECT`, `INSERT`, `TRIGGER` |
| `erp_it_runner` | `SELECT`, `INSERT`, `UPDATE`, `DELETE` |

El inicializador no crea usuarios ni concede permisos. Los permisos de la cuenta de
aprovisionamiento deben poder establecerse antes de que exista el esquema. Si la
política local no lo permite, hay que resolverlo con el administrador antes de
ejecutar el procedimiento; no utilizar root como sustituto.

La cuenta de aprovisionamiento necesita `TRIGGER` para tener visibilidad comprobada
sobre todos los triggers del esquema. La cuenta ejecutora nunca recibe ese permiso.
Antes de cualquier preparación y antes de devolver cada conexión ejecutora, se
comprueban identidad, roles, `@@GLOBAL.partial_revokes` y todos los permisos.
La consulta de triggers se ejecuta con la identidad de aprovisionamiento. Si falta
esa identidad, no se puede demostrar su visibilidad, la consulta falla o aparece
algún trigger, se aborta. No se interpreta un cero filtrado del ejecutor como prueba.

Los permisos deben representar un único esquema literal. Con `partial_revokes=OFF`,
los tres guiones bajos deben estar escapados: `erp\_little\_horizons\_test`.
Con `partial_revokes=ON`, se acepta únicamente el nombre literal sin escapes:
`erp_little_horizons_test`. La validación no convierte nombres de esquema a mayúsculas.
Una representación distinta o un estado no verificable se rechazan; no se cambia
la variable del servidor automáticamente. No ejecutar GRANT sin revisión del
administrador ni copiar estos ejemplos como si fueran comandos de aprovisionamiento.

Copiar manualmente `db.integration.properties.example` a:

- `.local-secrets/integration.properties`: cuenta de ejecución.
- `.local-secrets/integration-provisioner.properties`: cuenta de aprovisionamiento.

Completar las tres claves `db.url`, `db.user`, `db.password` en cada archivo.
Se exige contraseña no vacía. Los archivos no se generan automáticamente y están
excluidos de Git; nunca colocar secretos en el POM, comandos, recursos o capturas.
La carga usa el formato estándar de `java.util.Properties` (escape Unicode cuando
sea necesario), sin variables de entorno ni configuración de desarrollo como respaldo.

La URL admitida tiene exactamente este formato, sin parámetros adicionales:

```text
jdbc:mysql://127.0.0.1:3306/erp_little_horizons_test
```

También se admite `localhost` y un puerto explícito entre 1 y 65535. No se admiten
otros hosts, destinos múltiples, credenciales en la URL, codificación del catálogo,
parámetros de sesión ni catálogos alternativos. La conexión es local; para un servidor
remoto o una política TLS particular se requiere revisar este diseño antes de usarlo.

## Preparación futura de la base

1. Obtener aprobación para crear la base y cargar sus datos iniciales.
2. Preparar las cuentas y los archivos privados descritos arriba.
3. Configurar JDK 21 en `JAVA_HOME` y compilar el soporte:

   ```powershell
   mvn test-compile
   ```

4. Revisar el derivado `scripts/integration/initialize-test.sql`. Contiene las
   mismas 22 tablas y los 24 registros de catálogo oficiales. Todos los destinos y
   referencias están calificados con `erp_little_horizons_test`; no contiene
   `DROP`, `USE` ni creación de usuarios. No importa el SQL oficial completo.
5. El envoltorio admite una revisión estática sin conexión:

   ```powershell
   .\scripts\integration\prepare-test.ps1
   ```

6. Solo después de la autorización, la preparación con escritura sería:

   ```powershell
   .\scripts\integration\prepare-test.ps1 -Apply `
     -ConfigPath .\.local-secrets\integration-provisioner.properties
   ```

El envoltorio usa el Connector/J 8.3.0 del repositorio Maven estándar del usuario.
Si se utiliza un repositorio personalizado, ajustar esa ubicación tras revisión.
El inicializador Java vuelve a comprobar configuración y privilegios y exige que
el archivo SQL coincida exactamente con la derivación del oficial, antes de crear
la base. Si la base de pruebas ya existe, aborta sin modificarla. No hay borrado,
reinicio, `IF NOT EXISTS` ni reintentos destructivos.

MySQL confirma DDL implícitamente: si una inicialización falla a mitad de camino,
puede quedar un esquema parcial. Se detiene y exige revisión manual; no intenta
deshacerlo eliminando la base. Si la base ya fue preparada por otro procedimiento,
no ejecutar `-Apply`: verificar su estructura antes de las pruebas.

## Ejecución futura de las pruebas

Después de autorizar expresamente las escrituras:

```powershell
mvn -Pintegration -Dintegration.write.enabled=true verify
```

Para otra ubicación del archivo privado, pasar únicamente su ruta absoluta mediante
`-Dintegration.config=...`. No pasar la contraseña por línea de comandos.
La identidad separada se carga desde `.local-secrets/integration-provisioner.properties`
o la ruta absoluta `-Dintegration.verifier.config=...`. Debe usar el mismo destino
exacto y otro usuario. Su conexión de verificación se marca de solo lectura.

Failsafe hereda valores `DB_*` deliberadamente inutilizables. El soporte valida su
propio archivo, instala un driver de pruebas y configura el Singleton existente
antes de construir los DAO. El driver revisa catálogo, identidad y permisos en cada
conexión, verifica triggers con la identidad separada y bloquea cambios de catálogo.
El SQL se limita a plantillas exactas revisadas en
`src/test/resources/integration/allowed-sql.txt`, más formas cerradas de limpieza
por clave primaria. El archivo es estático: no se aprende SQL en tiempo de ejecución.
Al modificar un DAO, deben revisarse explícitamente sus nuevas plantillas. Se
normaliza el espacio entre tokens preservando sus límites y los literales; no se aceptan formas arbitrarias
por tener un prefijo de tabla conocido. Los resultados JDBC y de metadatos se
envuelven recursivamente, incluidos los objetos JDBC contenidos en arrays.
Los errores del driver no incluyen mensajes originales con posibles secretos.
No es un sandbox frente a código Java malicioso: el aislamiento depende también de
los privilegios restringidos del servidor y de mantener las pruebas revisadas.

## Datos ficticios y limpieza

Cada caso crea un profesor, acudiente, materia, estudiante, club, evento y recurso
ficticios, con documentos y códigos prefijados `IT` y correos `example.invalid`.
Los casos añaden relaciones, notas, asistencia, pagos, facturas y préstamos según
su objetivo. Se conservan los catálogos oficiales y no se generan PDFs en los IT.

Se registran las claves generadas y se limpia en orden inverso de dependencias,
exclusivamente por claves primarias comprobadas. Cada eliminación usa una transacción
y solo retira su entrada del registro después de un commit confirmado. Se exige
una fila afectada o se comprueba que la fila ya esté ausente antes de aceptar cero.
Una cantidad inesperada provoca rollback. Los fallos mantienen las claves pendientes
y se informan mediante tabla, clave e ID ficticio, sin mensajes del driver ni secretos.
El mismo registro permite reintentar y consultar las claves pendientes. Se rechazan
conexiones con una transacción previa para no confirmar trabajo ajeno. Las relaciones dependientes se eliminan mediante
las cascadas oficiales. Nunca se vacían tablas ni se desactivan claves foráneas.
Si falla la limpieza o se interrumpe la JVM, pueden quedar registros ficticios:
se informa el fallo y debe revisarse el esquema de pruebas antes de repetir.
Estos datos son para comportamiento; no cumplen por sí solos la entrega de
30 registros por tabla principal.

## Cobertura preparada

- Siete clases `*DAOIT`: inserciones, consultas, claves generadas, actualizaciones
  existentes, duplicados, relaciones, notas límite y préstamos/devoluciones.
- `TransactionIT`: commit visible desde otra conexión y rollback de una inserción
  después de una violación de clave foránea.
- `IntegrationSafetyTest`: rechazos sin conexión de URLs, cuentas, permisos, SQL,
  cambio de catálogo, acceso directo y scripts alterados.

No se supone que una transacción de JUnit deshace operaciones de DAO: estos abren
conexiones y algunos confirman internamente. Se distinguen las pruebas directas
de transacciones de las pruebas DAO con limpieza posterior.

Las regresiones sin conexión cubren los cinco hallazgos de seguridad. Debe realizarse
una nueva auditoría independiente antes de aprobar el entorno. Las comprobaciones
de visibilidad no impiden que un administrador externo cambie permisos o cree un
trigger después de la verificación; durante las futuras pruebas se requiere acceso
administrativo exclusivo o una instancia MySQL dedicada y sin cambios concurrentes.

Las pruebas reales están preparadas, pero no ejecutadas. Los consecutivos de Fase 3,
las carreras de cupos/préstamos y la recuperación de fallos financieros siguen
pendientes. Esta entrega no los corrige ni certifica.

Informes: unitarias en `target/surefire-reports`; integración futura en
`target/failsafe-reports`. No publicar configuraciones privadas ni logs históricos.

Referencias para la auditoría de permisos y visibilidad:
[semántica de GRANT y partial_revokes](https://dev.mysql.com/doc/refman/8.0/en/grant.html),
[visibilidad de INFORMATION_SCHEMA.TRIGGERS](https://dev.mysql.com/doc/refman/8.0/en/information-schema-triggers-table.html).
