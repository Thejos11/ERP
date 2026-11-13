[CmdletBinding()]
param(
    [switch]$Apply,
    [string]$ConfigPath,
    [string]$JavaHome = $env:JAVA_HOME
)
$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$scriptPath = Join-Path $PSScriptRoot 'initialize-test.sql'
$officialPath = Join-Path $projectRoot 'script-bd-pry-erp-colegio-little-horizon.sql'
$scriptText = [IO.File]::ReadAllText($scriptPath)

# Offline checks run by default. No JDBC connection or Maven execution here.
$withoutComments = [regex]::Replace($scriptText, '(?m)^--[^\r\n]*', '')
$statements = @($withoutComments -split ';' | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
if ($statements.Count -ne 25) { throw 'Se esperaban 22 tablas y 3 bloques de datos iniciales.' }
foreach ($statement in $statements) {
    $sql = $statement.Trim()
    if ($sql -notmatch '^(CREATE TABLE|INSERT INTO) erp_little_horizons_test\.[a-z_]+\s*\(') {
        throw 'Sentencia o destino no permitido.'
    }
    $operationText = [regex]::Replace($sql, '(?i)ON DELETE CASCADE', '')
    if ($operationText -match '(?i)\b(DROP|TRUNCATE|ALTER|DELETE|USE|GRANT|REVOKE|CREATE DATABASE)\b') {
        throw 'Operación no permitida en el script.'
    }
    if ($sql -match '(?i)\berp_little_horizons\b') { throw 'Referencia a la base principal bloqueada.' }
    foreach ($reference in [regex]::Matches($sql, '(?i)REFERENCES\s+([^\s(]+)')) {
        if ($reference.Groups[1].Value -notmatch '^erp_little_horizons_test\.[a-z_]+$') {
            throw 'Referencia fuera del esquema de pruebas.'
        }
    }
}
Write-Output 'Validación estática completada. No se abrió conexión MySQL.'
if (-not $Apply) { return }

# This branch requires a later, separate authorization to create/write the test schema.
# Java proves literal GRANT scope using partial_revokes, then checks trigger visibility
# with the provisioning identity (CREATE, SELECT, INSERT, TRIGGER on the test schema).
if ([string]::IsNullOrWhiteSpace($ConfigPath)) { throw 'Indique el archivo privado de la cuenta de aprovisionamiento.' }
if ([string]::IsNullOrWhiteSpace($JavaHome)) { throw 'Configure JAVA_HOME para Java 21.' }
$classes = Join-Path $projectRoot 'target/test-classes'
$driver = Join-Path $env:USERPROFILE '.m2/repository/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar'
if (-not (Test-Path -LiteralPath (Join-Path $classes 'com/littlehorizons/erp/integration/support/TestDatabaseInitializer.class'))) {
    throw 'Compile primero las pruebas con mvn test-compile; este comando no las ejecuta.'
}
if (-not (Test-Path -LiteralPath $driver)) { throw 'No se encontró Connector/J en el repositorio Maven local.' }
$java = Join-Path $JavaHome 'bin/java.exe'
& $java -cp ($classes + [IO.Path]::PathSeparator + $driver) `
    com.littlehorizons.erp.integration.support.TestDatabaseInitializer `
    '--apply-test-only' ([IO.Path]::GetFullPath($ConfigPath)) $scriptPath $officialPath
if ($LASTEXITCODE -ne 0) { throw 'La inicialización no se completó. No se realizará limpieza automática.' }
