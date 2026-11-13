# Configuracion MySQL y logs — Fase 1

Copie `db.properties.example` a `db.properties` en el directorio desde el que inicia
la aplicacion. Complete `db.url`, `db.user` y `db.password` localmente. Use una cuenta
dedicada con los permisos necesarios sobre `erp_little_horizons`.

La precedencia se conserva por compatibilidad: archivo externo; si no existe,
archivo aportado externamente en el classpath; variables `DB_URL`, `DB_USER` y
`DB_PASSWORD` para claves ausentes. Un archivo ilegible o mal formado produce
error de configuracion: no se intenta otra fuente silenciosamente. No hay URL,
usuario ni contraseña predeterminados. Una contraseña vacia solo se admite cuando
se declara explicitamente, para conservar instalaciones existentes.

Maven excluye `db.properties` de los recursos y del JAR. Los dos archivos locales
existentes se conservan en sus ubicaciones y tienen copias en `.local-secrets/`.
Esa carpeta y los archivos locales estan ignorados y se retiraron del indice de
Git; no se ha reescrito el historial. Si alguna credencial real se publico antes,
debe rotarse: retirarla ahora no la elimina de commits anteriores.

El menu de configuracion oculta los datos actuales y usa `Console.readPassword()`.
Si la terminal o el IDE no ofrece entrada oculta, indica configurar el archivo,
sin leer la contraseña con eco. La configuracion manual sigue siendo temporal.

Los logs ocultan URLs JDBC completas y asignaciones sensibles. Las excepciones
conservan su tipo y ubicaciones de llamada, pero no sus mensajes, causas ni errores
suprimidos, que pueden contener secretos sin etiquetas. Los errores JDBC se
propagan con un mensaje seguro sin adjuntar la excepcion original. No registre
secretos como texto libre: un filtro no puede identificar cualquier texto arbitrario.
Los logs historicos no se modifican y deben tratarse como archivos privados.

Las pruebas de configuracion y seguridad usan datos sinteticos, archivos temporales
y un driver JDBC simulado. No requieren servidor MySQL ni usan las credenciales
locales para conectarse. Ejecute `mvn clean test` con JDK 21.
