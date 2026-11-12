---
name: java-mvc-erp-developer
description: >-
  Especialista en desarrollo Java 21 y arquitectura MVC para el ERP Colegio Internacional Little Horizons.
  Usar esta skill al implementar o modificar modelos, controladores, vistas por consola, DAOs,
  lógica de negocio, integración JDBC con MySQL y generación de facturas PDF bajo principios SOLID.
---

# Java MVC ERP Developer - Little Horizons ERP

Esta Skill define los estándares de arquitectura, diseño e implementación en Java para el proyecto **ERP Colegio Internacional Little Horizons**, garantizando modularidad, seguridad, código limpio y cumplimiento riguroso del patrón MVC y los principios SOLID.

---

## 1. Perfil y Responsabilidades

* **Lenguaje y Versión:** Java 21 LTS (aprovechando características modernas como Records, Pattern Matching y Streams).
* **Arquitectura:** Modelo-Vista-Controlador (MVC) estricto.
* **Persistencia:** Patrón Data Access Object (DAO) mediante JDBC puro y MySQL 8.0+.
* **Principios de Diseño:** SOLID, Clean Code, separación de responsabilidades y modularidad.
* **Idioma:**
  * Instrucciones, documentación y mensajes en consola para el usuario: **Español**.
  * Código Java (nombres de clases, interfaces, métodos, variables y comentarios técnicos): **Inglés**.

---

## 2. Arquitectura de Paquetes del Proyecto

Toda nueva clase o refactorización debe ubicarse en su paquete correspondiente dentro de `com.littlehorizons.erp`:

```text
com.littlehorizons.erp
├── model/        # Entidades POJO de dominio (Student, Teacher, AcademicRecord, etc.)
├── dao/          # Data Access Objects con JDBC y consultas SQL parametrizadas
├── controller/   # Lógica de negocio y orquestación entre DAO y Vista
├── view/         # Vistas de consola interactivas (I/O, tablas formateadas)
└── util/         # Clases utilitarias (DatabaseConnection, InputValidator, LoggerUtil, InvoicePdfGenerator)
```

---

## 3. Directrices Arquitectónicas Obligatorias

### 3.1. Separación Estricta de Capas (MVC)
1. **Model:**
   * Representa los datos del negocio sin lógica de persistencia ni interfaz de usuario.
   * Encapsulamiento con constructores completos, getters, setters y `toString()`.
2. **View:**
   * Responsable exclusiva de la interacción con el usuario en consola (System.out y Scanner vía `InputValidator`).
   * **Prohibido:** No debe contener sentencias SQL, llamadas directas a DAOs ni cálculos complejos de negocio.
3. **Controller:**
   * Orquesta el flujo de la aplicación.
   * Valida reglas de negocio (ej. validación de cupo disponible antes de inscribir en un club).
   * Se comunica con la capa DAO para persistir o recuperar datos y envía los resultados a la Vista.
4. **DAO (Data Access Object):**
   * Encapsula todas las operaciones SQL hacia MySQL.
   * Utiliza `DatabaseConnection.getInstance()` para obtener conexiones.
   * **Obligatorio:** Utilizar `PreparedStatement` para prevenir inyección SQL y bloques `try-with-resources` para garantizar el cierre automático de `Connection`, `PreparedStatement` y `ResultSet`.

---

## 4. Estándares de Codificación Java

### 4.1. Patrón DAO y Gestión de Recursos JDBC
Siempre implementar el patrón `try-with-resources` y captura de excepciones con registro en `LoggerUtil`:

```java
public Optional<Student> findById(int id) {
    String sql = "SELECT id, tipo_documento, numero_documento, primer_nombre, primer_apellido "
               + "FROM estudiantes WHERE id = ?";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setInt(1, id);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                Student student = new Student();
                student.setId(rs.getInt("id"));
                student.setDocumentType(rs.getString("tipo_documento"));
                student.setDocumentNumber(rs.getString("numero_documento"));
                student.setFirstName(rs.getString("primer_nombre"));
                student.setLastName(rs.getString("primer_apellido"));
                return Optional.of(student);
            }
        }
    } catch (SQLException e) {
        LoggerUtil.logError("Failed to fetch student by ID: " + id, e);
    }
    return Optional.empty();
}
```

### 4.2. Principios SOLID Aplicados
* **SRP (Single Responsibility):** Cada clase tiene un único propósito. No mezclar persistencia en controladores ni interacción por consola en DAOs.
* **OCP (Open/Closed):** Diseñar métodos extensibles y parametrizables, apoyándose en la API de Streams para transformaciones y agregaciones.
* **LSP (Liskov Substitution):** Jerarquías coherentes (como `StudentLoan` y `TeacherLoan`).
* **ISP (Interface Segregation):** DAOs modulares por contexto delimitado (`AcademicDAO`, `FinanceDAO`, `ResourceDAO`, `TeacherDAO`, `StudentDAO`).
* **DIP (Dependency Inversion):** Los controladores reciben o gestionan dependencias abstractas o modulares sin acoplamiento a drivers de bajo nivel.

### 4.3. Manejo de Errores y Validaciones
* Utilizar `InputValidator` para toda lectura de entrada por teclado en vistas.
* Usar `LoggerUtil.logError()`, `LoggerUtil.logWarning()` y `LoggerUtil.logInfo()` para auditoría persistente en `error_log.txt`.
* No tragar excepciones vacías (`catch (Exception e) {}`).

---

## 5. Protocolo de Trabajo Antes de Modificar Código

1. **Inspección Previa:** Leer y analizar la clase objetivo y sus dependencias existentes en `src/main/java/com/littlehorizons/erp/`.
2. **Respeto a la Estructura:** No renombrar paquetes ni alterar firmas públicas sin verificar el impacto en el resto del ERP.
3. **Preservación:** No eliminar ni sobrescribir archivos existentes sin respaldo o justificación justificada.
4. **Verificación:** Ejecutar compilación y pruebas con Maven (`mvn test-compile` o `mvn clean test`) tras cada cambio.
