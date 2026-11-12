---
name: mysql-database-engineer
description: >-
  Especialista en base de datos MySQL 8.0+ para el ERP Colegio Internacional Little Horizons.
  Usar esta skill para diseñar scripts DDL/DML, optimizar consultas SQL, administrar índices,
  garantizar integridad referencial con claves primarias y foráneas, y verificar transacciones ACID.
---

# MySQL Database Engineer - Little Horizons ERP

Esta Skill establece los lineamientos técnicos para el diseño, mantenimiento, optimización y consulta de la base de datos relacional **`erp_little_horizons`** en **MySQL 8.0 o superior**, asegurando integridad referencial estricta, rendimiento y concordancia total con la capa de persistencia del ERP.

---

## 1. Perfil y Responsabilidades

* **Motor de Base de Datos:** MySQL Server 8.0+ (Motor de almacenamiento **InnoDB** con soporte transaccional y restricciones `CHECK`).
* **Nombre de la Base de Datos:** `erp_little_horizons`.
* **Configuración de Conexión:** Gestionada externamente mediante [db.properties](file:///C:/Users/Usuario/Desktop/java/ERP/db.properties).
* **Integridad Referencial:** Claves primarias (`AUTO_INCREMENT`), claves foráneas (`FOREIGN KEY`) explícitas, restricciones de unicidad (`UNIQUE`) y restricciones de rango (`CHECK`).
* **Optimización:** Creación de índices estratégicos en columnas de búsqueda frecuente y claves foráneas.
* **Idioma:**
  * Documentación y explicaciones: **Español**.
  * Código SQL, nombres de esquemas, tablas y columnas existentes: respetar la nomenclatura definida en el proyecto.

---

## 2. Mapa Canónico de Tablas del Sistema (22 Tablas)

> [!IMPORTANT]
> **Regla de Oro:** No inventar tablas, columnas ni relaciones ajenas a las definidas en el modelo del ERP. Cualquier consulta o modificación debe apegarse estrictamente a las siguientes entidades:

### 2.1. Módulo Académico y Estudiantil
1. `estudiantes`: Datos personales, contacto, información médica y estado del estudiante.
2. `acudientes`: Información de acudientes y padres de familia.
3. `estudiante_acudiente`: Tabla intermedia de relación acudiente-estudiante con parentesco y flag `es_principal`.
4. `grados`: Niveles académicos ofrecidos por la institución.
5. `matriculas`: Registro de matrícula por estudiante, grado y año lectivo.
6. `profesores`: Directorio docente, especialidad y estado de contratación.
7. `materias`: Asignaturas vinculadas a los grados académicos.
8. `asignaciones_profesor`: Asignación de carga académica (profesor, materia, año lectivo).
9. `inscripciones_materia`: Inscripción de estudiantes en materias asignadas.
10. `registros_academicos`: Calificaciones por período (I, II, III, IV), valor de nota (`DECIMAL(2,1)` de 0.0 a 5.0) y observaciones.
11. `asistencias`: Registro diario de asistencia por materia con estado (`Presente`, `Ausente`, `Tardanza`, `Justificado`).

### 2.2. Módulo Extracurricular
12. `clubes`: Catálogo de clubes escolares (Robótica, Ciencias, etc.), cupos máximos y profesor a cargo.
13. `membresias_club`: Inscripciones de estudiantes a clubes con control de capacidad.
14. `eventos`: Cronograma de eventos escolares (Académicos, Culturales, Deportivos).
15. `participantes_evento`: Inscripción de estudiantes/profesores en eventos, roles y posición obtenida (`1er Lugar`, etc.).

### 2.3. Módulo Financiero y Facturación
16. `conceptos_pago`: Catálogo de cobros (Matrícula, Pensión, Uniforme, Transporte) y valor base.
17. `pagos`: Transacciones de pago efectuadas por estudiantes/acudientes.
18. `facturas`: Facturación electrónica simulada DIAN, CUFE (SHA-256), código QR y ruta del archivo PDF.

### 2.4. Módulo de Recursos (Biblioteca y Laboratorios)
19. `categorias_recurso`: Categorías (Libros, Equipos de Laboratorio, Tecnología, etc.).
20. `recursos`: Ítems disponibles con código institucional, estado y stock.
21. `prestamos_estudiante`: Control de préstamos de recursos a estudiantes con fecha pactada y devolución.
22. `prestamos_profesor`: Control de préstamos de recursos a profesores con fecha pactada y devolución.

---

## 3. Directrices de Integridad y Restricciones MySQL 8.0+

### 3.1. Restricciones CHECK
Asegurar que las calificaciones cumplan la escala oficial colombiana (0.0 a 5.0):
```sql
ALTER TABLE registros_academicos
  ADD CONSTRAINT chk_nota_rango CHECK (valor_nota >= 0.0 AND valor_nota <= 5.0);
```

### 3.2. Claves Foráneas con Acciones Referenciales
Garantizar la consistencia de datos evitando registros huérfanos:
```sql
ALTER TABLE inscripciones_materia
  ADD CONSTRAINT fk_inscripcion_estudiante
  FOREIGN KEY (estudiante_id) REFERENCES estudiantes(id)
  ON DELETE RESTRICT ON UPDATE CASCADE;
```

### 3.3. Manejo Transaccional en Consultas Complejas
Para operaciones atómicas (ej. registrar un pago y generar la factura asociada):
```sql
START TRANSACTION;
-- 1. Insertar el pago
INSERT INTO pagos (estudiante_id, concepto_id, monto, metodo_pago, fecha_pago, numero_recibo)
VALUES (?, ?, ?, ?, NOW(), ?);

-- 2. Insertar el registro de factura
INSERT INTO facturas (pago_id, numero_factura, cufe, fecha_emision, ruta_pdf)
VALUES (LAST_INSERT_ID(), ?, ?, NOW(), ?);

COMMIT;
```

---

## 4. Optimización de Consultas SQL

1. **Evitar `SELECT *`:** Especificar explícitamente las columnas necesarias para reducir tráfico I/O y uso de memoria.
2. **Uso de Índices:** Crear índices sobre campos de filtrado recurrente (`numero_documento`, `estudiante_id`, `fecha_pago`, `estado`).
3. **Planes de Ejecución:** Usar `EXPLAIN` o `EXPLAIN ANALYZE` en consultas con múltiples `JOIN` para descartar table scans masivos.
4. **Sentencias Preparadas:** Toda sentencia generada para la aplicación debe usar marcadores posicionales `?` para compatibilidad JDBC con `PreparedStatement`.

---

## 5. Protocolo de Verificación Previa a Modificaciones

1. Inspeccionar las sentencias SQL existentes en `src/main/java/com/littlehorizons/erp/dao/*.java`.
2. Validar que los nombres de columnas coincidan con los atributos leídos en los `ResultSet` correspondientes.
3. No alterar tipos de datos que causen incompatibilidades de mapeo con Java (`BigDecimal` para valores monetarios y notas, `LocalDate` para fechas).
