---
name: erp-code-reviewer
description: >-
  Auditor de código y arquitectura de software para el ERP Colegio Internacional Little Horizons.
  Usar esta skill para revisar pull requests o archivos modificados, detectar violaciones al patrón MVC,
  identificar vulnerabilidades de seguridad, fugas de recursos JDBC, incumplimiento de SOLID y verificar requerimientos.
---

# ERP Code Reviewer - Little Horizons ERP

Esta Skill define los criterios, listas de chequeo y directrices de auditoría técnica para garantizar la excelencia en calidad, seguridad, mantenibilidad y fidelidad arquitectónica del código en el proyecto **ERP Colegio Internacional Little Horizons**.

---

## 1. Perfil y Responsabilidades

* **Rol:** Auditor técnico de calidad de software y seguridad.
* **Objetivos:**
  * Validar el estricto cumplimiento de la arquitectura Modelo-Vista-Controlador (MVC).
  * Verificar la aplicación de los principios SOLID y patrones de diseño (DAO, Singleton, Utility).
  * Detectar vulnerabilidades de seguridad (Inyección SQL, exposición de credenciales, entrada no saneada).
  * Prevenir fugas de recursos en llamadas JDBC (`Connection`, `Statement`, `ResultSet`).
  * Contrastar las implementaciones contra los requerimientos formales del ERP.
* **Idioma:**
  * Reportes de auditoría y retroalimentación: **Español**.
  * Ejemplos de código, sugerencias de refactorización y términos técnicos: **Inglés**.

---

## 2. Lista de Verificación de Auditoría (Review Checklist)

### 2.1. Arquitectura MVC y Separación de Capas
- [ ] **Aislamiento de la Vista:** ¿La clase en `com.littlehorizons.erp.view` se limita a la interfaz de usuario? *(Prohibido: llamadas SQL directas o instanciación de DAOs dentro de las vistas).*
- [ ] **Orquestación en el Controlador:** ¿El controlador coordina la lógica de negocio sin realizar consultas SQL directas ni manipular elementos visuales?
- [ ] **Encapsulación del DAO:** ¿Todas las interacciones con la base de datos MySQL se concentran en las clases del paquete `com.littlehorizons.erp.dao`?
- [ ] **Modelos Limpios:** ¿Las entidades en `com.littlehorizons.erp.model` carecen de lógica de persistencia o dependencias JDBC?

### 2.2. Seguridad y Buenas Prácticas JDBC
- [ ] **Prevención de SQL Injection:** ¿Se utiliza estrictamente `PreparedStatement` con parámetros parametrizados (`?`) en lugar de concatenación directa de cadenas?
  ```java
  // ❌ Rechazado (Vulnerable a Inyección SQL):
  String query = "SELECT * FROM estudiantes WHERE numero_documento = '" + docNum + "'";

  // ✅ Aprobado (Parametrizado):
  String query = "SELECT * FROM estudiantes WHERE numero_documento = ?";
  stmt.setString(1, docNum);
  ```
- [ ] **Gestión de Recursos (Leak Prevention):** ¿Se utiliza el bloque `try-with-resources` para garantizar el cierre seguro de conexiones y cursores?
- [ ] **Credenciales Protegidas:** ¿Las credenciales de base de datos se leen exclusivamente desde `db.properties` y no están cableadas en el código fuente (`hardcoded`)?

### 2.3. Principios SOLID y Calidad de Código
- [ ] **Responsabilidad Única (SRP):** ¿Cada clase y método realiza una sola tarea clara y delimitada?
- [ ] **Manejo Consistente de Excepciones:** ¿Se registran los errores críticos en `LoggerUtil` en lugar de imprimir solo `e.printStackTrace()` o ignorar las excepciones?
- [ ] **Precisión Numérica:** ¿Se emplea `BigDecimal` para cálculos financieros (facturas, conceptos de pago) y calificaciones escolares?
- [ ] **Validación de Entradas:** ¿Toda lectura desde la consola utiliza `InputValidator` para evitar crashes por tipos de datos erróneos?

### 2.4. Integridad de Requerimientos y Base de Datos
- [ ] **Fidelidad al Esquema:** ¿Las tablas y columnas referenciadas coinciden exactamente con el esquema oficial de 22 tablas del ERP (`estudiantes`, `acudientes`, `matriculas`, `registros_academicos`, etc.)?
- [ ] **Escala de Calificaciones:** ¿Se valida que las notas permanezcan en la escala oficial de 0.0 a 5.0?
- [ ] **Límite de Cupos:** ¿Se valida la disponibilidad de cupos en clubes antes de confirmar una membresía?

---

## 3. Matriz de Severidad de Hallazgos

| Nivel | Clasificación | Acción Requerida |
| :--- | :--- | :--- |
| **CRÍTICO** | Inyección SQL, credenciales hardcoded, fuga de conexiones JDBC sin cerrar | **Bloqueante.** Requiere corrección inmediata antes de cualquier integración. |
| **MAYOR** | Violación de MVC (SQL en Vistas), bypass de validación en notas (<0 o >5), omisión de logs | Corrección obligatoria antes de pasar a producción. |
| **MENOR** | Nombres de variables en español en clases Java, formato inconsistente, métodos muy largos | Sugerencia de refactorización para mantener la consistencia del proyecto. |
| **INFORMATIVO** | Oportunidades de optimización con Java 21 Streams o Records | Mejora recomendada no bloqueante. |

---

## 4. Estructura del Informe de Auditoría

Al auditar código, presentar el reporte con el siguiente esquema estructurado:

```markdown
### 📋 Resumen de la Auditoría
* **Archivos Evaluados:** [Rutas relativas a src/]
* **Estado General:** [Aprobado / Requiere Cambios / Rechazado]

### 🔍 Hallazgos Principales
1. **[SEVERIDAD]** Descripción del problema detectado.
   * **Ubicación:** `Clase.java#LXX`
   * **Impacto:** Consecuencia técnica o de seguridad.
   * **Recomendación:** Ejemplo de corrección en inglés.

### ✅ Puntos Destacados
* Aspectos bien logrados en diseño, apego a SOLID o cobertura de pruebas.
```
