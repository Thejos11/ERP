# Proyecto ERP para el Colegio "Little Horizons"

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36.svg)](https://maven.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Sistema integral de Planificación de Recursos Empresariales (ERP) educativo para la institución **Colegio Internacional Little Horizons**. Centraliza y automatiza los procesos académicos, extracurriculares, financieros (con facturación electrónica DIAN simulada en PDF) y gestión de inventario/préstamos de recursos escolares.

---

## 1. Título del Proyecto

**Little Horizons ERP - Enterprise Educational Resource Planning System**  
*Desarrollado bajo arquitectura Modelo-Vista-Controlador (MVC) en Java 21 y MySQL 8.0.*

---

## 2. Descripción

### Resumen del Propósito del Sistema
El Colegio Internacional "Little Horizons" atiende a más de 500 estudiantes desde preescolar hasta grado 11°, con 40 profesores y diversas actividades curriculares y extracurriculares. Este sistema ERP reemplaza los registros en papel y hojas de cálculo dispersas por una solución centralizada, robusta, segura y basada en datos.

### Problemas que Resuelve
* **Pérdida de Información Académica:** Elimina cuadernos físicos y hojas de cálculo. Registra notas con precisión (`DECIMAL(2,1)` en escala colombiana de 0.0 a 5.0), calculando promedios por período y por materia automáticamente.
* **Desorganización en Actividades Extracurriculares:** Controla cupos máximos en clubes escolares y gestiona la participación en eventos y competencias con asignación de posiciones/podios.
* **Caos en la Facturación:** Automatiza el cobro de conceptos (matrícula, pensión, transporte, uniformes) y genera facturas electrónicas profesionales en formato PDF con CUFE (SHA-256) y código QR para cumplir con la simulación de normativas DIAN.
* **Desperdicio de Recursos de Biblioteca y Laboratorio:** Controla el inventario de recursos educativos y registra préstamos tanto a estudiantes como a profesores con fechas límite y reportes de préstamos vencidos.
* **Control de Asistencia:** Permite el pase de lista diario por materia y alerta sobre ausencias reiteradas (más de 3 inasistencias).

---

## 3. Autores

* **José Díaz** (`Thejos11`)
  * Correo electrónico: [josediaz7217@gmail.com](mailto:josediaz7217@gmail.com)
  * Rol: Desarrollador Principal / Arquitecto de Software

---

## 4. Tecnologías Utilizadas

* **Lenguaje de Programación:** Java 21 (LTS).
* **Herramienta de Construcción y Dependencias:** Apache Maven 3.9+.
* **Base de Datos:** MySQL 8.0 o superior (Motor InnoDB con soporte para CHECK constraints y transacciones).
* **Conectividad:** MySQL Connector/J `8.3.0`.
* **Librerías Externas:**
  * **OpenPDF (`1.3.40`):** Generación de documentos PDF de facturas electrónicas formateadas profesionalmente.
  * **ZXing Core & JavaSE (`3.5.3`):** Generación de códigos QR bidimensionales integrados dentro de las facturas PDF.
  * **JUnit Jupiter (`5.10.2`):** Pruebas unitarias automatizadas para los componentes de facturación y utilidades.
* **IDE / Editor:** IntelliJ IDEA / Eclipse / VS Code.

---

## 5. Requerimientos Previos

Para compilar y ejecutar el proyecto se requiere:
1. **Java Development Kit (JDK):** Versión 21 o superior instalada y configurada en la variable de entorno `JAVA_HOME`.
2. **Apache Maven:** Versión 3.8 o superior (`mvn`).
3. **MySQL Server:** **MySQL 8.0 o superior (REQUERIDO)**.
   > **Nota importante:** Se requiere MySQL 8.0+ para la correcta aplicación y validación de las restricciones `CHECK` (por ejemplo, validación de notas entre 0.0 y 5.0). Versiones anteriores ignoran las restricciones `CHECK`.

---

## 6. Configuración de la Base de Datos

### 6.1. Creación de la Base de Datos
El sistema se conecta a la base de datos llamada `erp_little_horizons`. Asegúrese de que el servidor MySQL se encuentre en ejecución y que la base de datos exista con su respectiva estructura de tablas.

### 6.2. Configuración segura de credenciales

Copie `db.properties` a `db.properties` en el directorio de ejecución y complete `db.url`, `db.user` y `db.password`. Use una cuenta dedicada para `erp_little_horizons`. No hay usuario `root`, contraseña ni URL predeterminados.

Los archivos locales se excluyen de Git y del empaquetado Maven. Se conserva la precedencia: archivo externo, classpath aportado externamente si no existe el archivo, y variables `DB_URL`, `DB_USER`, `DB_PASSWORD` para claves ausentes. Una contraseña vacía requiere configuración explícita.

La opción 8 permite configuración temporal con entrada oculta. En terminales sin `Console`, configure el archivo local. Consulte [la guía de seguridad](docs/database-security.md) para detalles y respaldos.

### 6.3. Instalación del Driver JDBC
El driver oficial `mysql-connector-j` se descarga y gestiona automáticamente mediante Maven a través del `pom.xml`:

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.3.0</version>
</dependency>
```

---

## 7. Instalación y Ejecución

### 7.1. Clonar el Repositorio
```bash
git clone https://github.com/Thejos11/ERP.git
cd ERP
```

### 7.2. Abrir el Proyecto en el IDE
* **IntelliJ IDEA:** Seleccione `File` -> `Open` y seleccione la carpeta del proyecto `ERP`. El IDE detectará automáticamente el archivo `pom.xml`.
* **VS Code:** Abra la carpeta y asegúrese de contar con el "Extension Pack for Java".

### 7.3. Compilar y Ejecutar Pruebas
```bash
mvn clean test
```

### 7.4. Ejecutar la Aplicación
Puede iniciar la aplicación de dos maneras:

**Opción A: Mediante Maven Exec Plugin:**
```bash
mvn compile exec:java -Dexec.mainClass="com.littlehorizons.erp.Main"
```

**Opción B: Generando y ejecutando el Fat JAR (Shade Plugin):**
```bash
mvn clean package
java -jar target/little-horizons-erp-1.0.0.jar
```

---

## 8. Estructura del Proyecto

El código fuente sigue estrictamente el patrón de arquitectura **Modelo-Vista-Controlador (MVC)**, con todo el código fuente escrito en inglés respetando las convenciones oficiales de nomenclatura de Java:

```
ERP/
├── .gitignore
├── db.properties               # Configuración externa de conexión a MySQL
├── pom.xml                     # Configuración de Maven y dependencias
├── invoices/                   # Carpeta donde se guardan las facturas PDF generadas
├── docs/                       # Documentación técnica del proyecto
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/littlehorizons/erp/
    │   │       ├── Main.java              # Clase de inicio del programa
    │   │       ├── model/                 # Entidades del dominio de negocio
    │   │       │   ├── Student.java
    │   │       │   ├── Guardian.java
    │   │       │   ├── GradeLevel.java
    │   │       │   ├── Enrollment.java
    │   │       │   ├── Teacher.java
    │   │       │   ├── Subject.java
    │   │       │   ├── AcademicRecord.java
    │   │       │   ├── Attendance.java
    │   │       │   ├── Club.java
    │   │       │   ├── SchoolEvent.java
    │   │       │   ├── Payment.java
    │   │       │   ├── PaymentConcept.java
    │   │       │   ├── Invoice.java
    │   │       │   ├── ResourceItem.java
    │   │       │   └── ...
    │   │       ├── dao/                   # Data Access Objects (JDBC & SQL seguro)
    │   │       │   ├── StudentDAO.java
    │   │       │   ├── GuardianDAO.java
    │   │       │   ├── TeacherDAO.java
    │   │       │   ├── AcademicDAO.java
    │   │       │   ├── ExtracurricularDAO.java
    │   │       │   ├── FinanceDAO.java
    │   │       │   └── ResourceDAO.java
    │   │       ├── controller/            # Controladores y Lógica de Negocio
    │   │       │   ├── StudentController.java
    │   │       │   ├── TeacherController.java
    │   │       │   ├── AcademicController.java
    │   │       │   ├── ExtracurricularController.java
    │   │       │   ├── FinanceController.java
    │   │       │   ├── ResourceController.java
    │   │       │   └── ReportsController.java
    │   │       ├── view/                  # Vistas de consola interactivas
    │   │       │   ├── ConsoleView.java
    │   │       │   ├── StudentView.java
    │   │       │   ├── TeacherView.java
    │   │       │   ├── AcademicView.java
    │   │       │   ├── ExtracurricularView.java
    │   │       │   ├── FinanceView.java
    │   │       │   ├── ResourceView.java
    │   │       │   └── ReportsView.java
    │   │       └── util/                  # Utilidades compartidas del sistema
    │   │           ├── DatabaseConnection.java  # Singleton JDBC Manager
    │   │           ├── LoggerUtil.java          # Sistema de Logs (error_log.txt)
    │   │           ├── InputValidator.java      # Validaciones seguras de teclado
    │   │           └── InvoicePdfGenerator.java # Generador PDF & QR con OpenPDF/ZXing
    │   └── resources/
    │       └── db.properties              # Archivo local ignorado; excluido del JAR
    └── test/
        └── java/
            └── com/littlehorizons/erp/util/
                └── InvoicePdfGeneratorTest.java  # Pruebas unitarias de facturación
```

---

## 9. Principios y Patrones de Diseño Aplicados

### 9.1. Principios SOLID

1. **Single Responsibility Principle (SRP - Responsabilidad Única):**
   Cada clase tiene una única razón para cambiar.
   * `StudentDAO` se encarga exclusivamente de la persistencia de datos de estudiantes en MySQL.
   * `StudentController` gestiona las reglas de negocio y orquestación de llamadas.
   * `StudentView` se limita a interactuar con el usuario mediante la consola.
   * `InvoicePdfGenerator` es responsable únicamente de la maquetación y generación del PDF y cálculo del CUFE.

2. **Open/Closed Principle (OCP - Abierto/Cerrado):**
   Las entidades y servicios están diseñados para ser extendibles sin necesidad de modificar el código existente. Por ejemplo, los métodos de cálculo de promedios o exportación de reportes procesan colecciones mediante la API de Streams de Java, facilitando añadir nuevos criterios de filtrado o agrupación sin alterar las rutinas base.

3. **Liskov Substitution Principle (LSP - Sustitución de Liskov):**
   Las jerarquías de entidades de préstamo (`StudentLoan` y `TeacherLoan`) y participantes reflejan contratos coherentes con sus correspondientes recursos asociados, pudiendo ser procesadas de manera uniforme por métodos utilitarios.

4. **Interface Segregation Principle (ISP - Segregación de Interfaces):**
   Los módulos DAO están desacoplados por dominio de negocio (`AcademicDAO`, `FinanceDAO`, `ResourceDAO`, etc.) en lugar de concentrar todas las operaciones en un único repositorio masivo.

5. **Dependency Inversion Principle (DIP - Inversión de Dependencias):**
   Los controladores no manejan conexiones directas ni sentencias SQL en bajo nivel; dependen de la abstracción provista por la capa DAO, la cual a su vez centraliza la gestión del ciclo de vida de la conexión mediante el gestor `DatabaseConnection`.

### 9.2. Patrones de Diseño Implementados

* **Singleton Pattern (`DatabaseConnection`):**
  Garantiza una única instancia del gestor de conexiones a la base de datos a lo largo de toda la ejecución de la aplicación, evitando la sobrecarga de conexiones y centralizando la lectura de `db.properties`.
  ```java
  public static synchronized DatabaseConnection getInstance() {
      if (instance == null) {
          instance = new DatabaseConnection();
      }
      return instance;
  }
  ```

* **Data Access Object (DAO Pattern):**
  Separa por completo la lógica de negocio del acceso a los datos. Todas las operaciones SQL utilizan `PreparedStatement` para prevenir inyección SQL y el bloque `try-with-resources` para garantizar el cierre inmediato de conexiones, sentencias y result sets.

* **Utility Class Pattern (`LoggerUtil`, `InputValidator`, `InvoicePdfGenerator`):**
  Clases con constructores privados que proveen métodos estáticos utilitarios de propósito específico, centralizando el manejo de excepciones y validaciones.

---

## 10. Funcionalidades Principales por Módulo

### 1. Módulo de Gestión Académica
* Registro de estudiantes con información personal, contactos médicos y de emergencia.
* Asociación de acudientes con parentesco y soporte para acudientes con múltiples hijos matriculados.
* Matrícula de estudiantes por grado académico y año lectivo.
* Inscripción en materias asignadas al grado.
* Directorio de profesores y asignación de carga académica por materia y sección.
* Ingreso de calificaciones por período con validación estricta de escala colombiana (0.0 a 5.0).
* Cálculo automático de promedio por materia, promedio del estudiante y promedio general del curso.
* Consulta de historial académico completo de notas.
* Registro de asistencia diaria y generación de reportes de inasistencias críticas (> 3 fallas).

### 2. Módulo de Actividades Extracurriculares
* Catálogo de clubes escolares (Ciencias, Artes, Deportes, Robótica, etc.) con asignación de docente líder.
* Inscripción de estudiantes con control estricto de cupo máximo.
* Calendario de eventos institucionales clasificados por tipo (Académico, Cultural, Deportivo, Social).
* Registro de participación y posiciones de competencia (1er lugar, 2do lugar, etc.).

### 3. Módulo Financiero y Facturación Electrónica DIAN
* Catálogo de conceptos de pago (Matrícula, Pensión, Uniforme, Transporte, etc.) y personalización de montos.
* Registro de pagos con método de pago (Efectivo, Transferencia, Tarjeta Débito/Crédito) y número de recibo.
* Historial de pagos por estudiante o acudiente.
* Reporte de estudiantes con morosidad en pagos.
* **Generación de Factura Electrónica en PDF:**
  * Cumplimiento de especificaciones DIAN (Razón social, NIT `901.458.732-4`, Resolución DIAN).
  * Cálculo algorítmico de **CUFE simulado** mediante hash SHA-256.
  * Generación dinámica de **Código QR bidimensional** con metadatos de la factura.
  * Formato profesional guardado en la carpeta `invoices/factura_XXXXXX.pdf`.

### 4. Módulo de Gestión de Recursos (Biblioteca y Laboratorios)
* Catálogo e inventario de recursos por categoría (Libros, Equipos de Laboratorio, Tecnología, Deportes).
* Registro de préstamos tanto a estudiantes como a profesores con validación de disponibilidad del recurso.
* Control de devoluciones con actualización inmediata del estado a "Disponible".
* Reporte de préstamos vencidos no entregados en la fecha límite pactada.

### 5. Reportes y Estadísticas del Sistema
* Resumen consolidado del rendimiento académico.
* Recaudación financiera por concepto de pago.
* Estadísticas de uso e inventario de recursos.

---

## 11. Manejo de Errores y Logs

El sistema implementa una política integral de tolerancia a fallos:
* **Archivo de Logs:** Todos los eventos críticos, advertencias y excepciones del sistema son capturados y almacenados en el archivo **`error_log.txt`** en la raíz del proyecto.
* **Formato de Registro:**
  ```
  [2026-11-11 16:59:20] [INFO] System startup initiated.
  [2026-11-11 17:02:15] [WARNING] Initial database connection test failed.
  [2026-11-11 17:05:30] [ERROR] Failed to obtain database connection: Communications link failure...
  ```
* **Prevención de Inyección SQL:** Se utilizan exclusivamente objetos `PreparedStatement` parametrizados en todos los DAOs.
* **Cierre Seguro de Recursos:** Todos los recursos JDBC (`Connection`, `PreparedStatement`, `ResultSet`) se gestionan mediante bloques `try-with-resources`, garantizando la liberación de conexiones incluso ante excepciones imprevistas.
* **Validación de Entradas:** `InputValidator` previene terminaciones anormales por ingresos mal formateados en la consola.

---

## 12. Menú Principal de la Consola

```
╔══════════════════════════════════════════════╗
║   ERP Colegio Little Horizons                ║
║   Sistema de Gestión Educativa               ║
╚══════════════════════════════════════════════╝

1. Gestión de Estudiantes
2. Gestión Académica
3. Gestión de Profesores
4. Actividades Extracurriculares
5. Gestión Financiera
6. Gestión de Recursos
7. Reportes y Estadísticas
8. Configurar Conexión a Base de Datos
9. Salir

Seleccione una opción: _
```

---

## 13. Problemas Conocidos y Limitaciones

* La interfaz de usuario es actualmente para terminal/consola.
* La facturación electrónica genera un CUFE y código QR simulados para efectos académicos y demostrativos, requiriendo un proveedor tecnológico habilitado ante la DIAN para emisión tributaria real en producción.

---

## 14. Futuras Mejoras

* Implementación de una interfaz gráfica moderna de escritorio (JavaFX) o web (Spring Boot + React).
* Envío automatizado de facturas PDF y reportes de inasistencias por correo electrónico a los acudientes.
* Exportación de reportes académicos a hojas de cálculo Excel (`.xlsx`).

---

## 15. Licencia

Este proyecto se encuentra bajo la Licencia [MIT](LICENSE).

---

## 16. Contacto

Para soporte, dudas o inquietudes sobre el sistema:
* **Autor:** José Díaz
* **GitHub:** [@Thejos11](https://github.com/Thejos11)
* **Correo:** [josediaz7217@gmail.com](mailto:josediaz7217@gmail.com)
* **Institución:** Colegio Internacional Little Horizons S.A.S.
