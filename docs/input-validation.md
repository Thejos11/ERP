# Fase 2: notas, fechas y entrada de consola

La precisión está definida por el README, el modelo AcademicRecord y el SQL oficial:
`valor_nota DECIMAL(2,1)`, entre 0.0 y 5.0 inclusive. Se valida el valor original
antes de ajustar su escala. No se redondean notas introducidas: `5.04`, `-0.04`
y `4.55` se rechazan. Se aceptan enteros y decimales representables exactamente
con un decimal, incluidos ceros finales (`4.50` equivale a `4.5`). La consola
admite coma o punto decimal. Los promedios no cambian.

GradeValidator comparte la regla entre la consola y AcademicController. El
controlador rechaza notas nulas o inválidas antes de invocar el DAO y no altera
el valor recibido. Se conserva el constructor sin argumentos y se añade otro
para proporcionar un DAO en pruebas. Las fechas recibidas como LocalDate ya
representan fechas existentes; el controlador también rechaza asistencia sin fecha.

Las fechas de consola exigen AAAA-MM-DD, con resolución estricta y año 0001..9999.
Se rechazan días inexistentes, meses inválidos, años no bisiestos y formatos
incompletos. No se agregan restricciones de edad o de fechas futuras.

InputValidator conserva sus firmas públicas y delega en ConsoleInput, que recibe
Scanner y salida para poder probar las lecturas sin cambiar System.in global.
Entradas incorrectas o vacías obligatorias se vuelven a solicitar. Un campo
opcional vacío devuelve null. EOF o Scanner cerrado generan EndOfInputException:
Main la maneja como cierre normal, incluso desde un submenú, sin guardar respuestas
inventadas ni registrar un error fatal. La pausa sigue la misma regla. Los flujos
de entrada estándar no se cierran desde la utilidad.

La lectura monetaria conserva su comportamiento anterior; no se modifican módulos
de facturación, seguridad de Fase 1 ni SQL. Las pruebas existentes de esas áreas
se ejecutan como regresión.

Validación pendiente con MySQL: inserción/actualización y lectura de notas límite,
fidelidad de DECIMAL(2,1), restricciones CHECK, claves foráneas, períodos y
persistencia de asistencia con fechas válidas. Los DAO no se modificaron.
