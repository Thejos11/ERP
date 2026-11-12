---
name: java-qa-testing
description: >-
  Especialista en pruebas de software, control de calidad y detección de errores en Java para el ERP Colegio Internacional Little Horizons.
  Usar esta skill para escribir y ejecutar pruebas unitarias con JUnit 5, validar conectividad JDBC,
  probar reglas de negocio, verificar casos límite y depurar fallos en los módulos del sistema.
---

# Java QA & Testing - Little Horizons ERP

Esta Skill establece los procedimientos y estándares para el aseguramiento de calidad (QA), desarrollo de pruebas automatizadas con **JUnit 5 Jupiter**, validación de integridad de datos y depuración de errores en el proyecto **ERP Colegio Internacional Little Horizons**.

---

## 1. Perfil y Responsabilidades

* **Framework de Pruebas:** JUnit Jupiter 5.10.2 (integrado vía Maven Surefire Plugin).
* **Alcance de Pruebas:**
  * Pruebas unitarias de componentes de negocio, formateo y utilidades (`InvoicePdfGenerator`, `InputValidator`, cálculos de promedios).
  * Pruebas de conectividad JDBC y verificación de configuración en `db.properties`.
  * Pruebas de validación de casos de borde y reglas de negocio institucionales.
  * Análisis de logs y rastreo de excepciones en `error_log.txt`.
* **Idioma:**
  * Documentación y reportes de prueba: **Español**.
  * Código de pruebas en Java, aserciones y nombres de métodos de prueba: **Inglés**.

---

## 2. Casos de Prueba Críticos y Reglas de Negocio a Validar

### 2.1. Validación de Calificaciones (Escala 0.0 a 5.0)
* **Caso Válido:** Notas en rango permitido (ej. `3.5`, `5.0`, `0.0`).
* **Caso Límite:** Intentar registrar valores fuera de rango (`-0.1`, `5.1`, `10.0`), verificando que se rechacen mediante excepción o mensaje de error.
* **Cálculo de Promedios:** Verificar redondeo a un decimal con `RoundingMode.HALF_UP` tanto por materia como general.

### 2.2. Validación de Capacidad de Clubes Extracurriculares
* **Cupo Máximo:** Verificar que un club con capacidad `maxCapacity = N` no admita el estudiante `N + 1`.
* **Inscripción Duplicada:** Asegurar que un estudiante no pueda inscribirse dos veces al mismo club en el mismo ciclo lectivo.

### 2.3. Control de Inasistencias Críticas
* **Umbral de Alerta:** Verificar que el sistema emita una alerta cuando un estudiante supere las **3 inasistencias** en una materia.

### 2.4. Módulo Financiero y Facturación Electrónica
* **Generación de CUFE:** Verificar que el hash SHA-256 cumpla con la longitud esperada (64 caracteres hexadecimales) y dependa de los datos de la factura.
* **Generación de PDF y QR:** Validar que el archivo PDF se genere en la carpeta `invoices/` y que el archivo no quede vacío (`file.length() > 0`).

### 2.5. Conectividad JDBC y Tolerancia a Fallos
* **Prueba de Conexión:** Validar la disponibilidad del servidor MySQL mediante `DatabaseConnection.getInstance().testConnection()`.
* **Manejo de Errores de Conexión:** Verificar que ante un servidor caído o credenciales inválidas no ocurra un crash fatal, sino que se registre el incidente en `error_log.txt`.

---

## 3. Estructura Estándar de Pruebas JUnit 5

Toda clase de prueba debe residir bajo `src/test/java/com/littlehorizons/erp/` y seguir el patrón Given-When-Then o Arrange-Act-Assert:

```java
package com.littlehorizons.erp.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

class AcademicGradeValidationTest {

    @Test
    @DisplayName("Should validate valid Colombian grade scale between 0.0 and 5.0")
    void testValidGradeRange() {
        // Arrange
        BigDecimal validGrade = new BigDecimal("4.5");

        // Act & Assert
        assertTrue(validGrade.compareTo(BigDecimal.ZERO) >= 0 && validGrade.compareTo(new BigDecimal("5.0")) <= 0,
                "Grade should be within 0.0 and 5.0 inclusive");
    }

    @Test
    @DisplayName("Should round average correctly with HALF_UP strategy")
    void testGradeRounding() {
        // Arrange
        double rawAverage = (4.5 + 4.2 + 4.8) / 3.0; // 4.5

        // Act
        BigDecimal rounded = BigDecimal.valueOf(rawAverage).setScale(1, RoundingMode.HALF_UP);

        // Assert
        assertEquals(new BigDecimal("4.5"), rounded);
    }
}
```

---

## 4. Ejecución y Comandos de Verificación Maven

Para ejecutar pruebas desde la terminal o consola:

* **Compilar y ejecutar todas las pruebas:**
  ```bash
  mvn clean test
  ```
* **Ejecutar una suite o clase de prueba específica:**
  ```bash
  mvn test -Dtest=InvoicePdfGeneratorTest
  ```
* **Verificar el informe de ejecución:**
  Revisar la salida en consola y los reportes en `target/surefire-reports/`.

---

## 5. Protocolo de Diagnóstico de Errores

1. **Revisión del Archivo de Logs:** Consultar [error_log.txt](file:///C:/Users/Usuario/Desktop/java/ERP/error_log.txt) para identificar el timestamp, nivel de severidad y traza de error (`StackTrace`).
2. **Inspección de Estado:** Verificar si el fallo se origina en la lectura de `db.properties`, en una consulta SQL mal formada o en una violación de integridad referencial.
3. **No Modificación Destructiva:** Nunca eliminar datos ni archivos existentes para ocultar un error; aislar la causa raíz con una prueba unitaria y corregir con mínima superficie de cambio.
