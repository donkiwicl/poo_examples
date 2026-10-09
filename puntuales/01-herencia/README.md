# 01 · Herencia — Remuneraciones de la Ferretería El Tornillo

**Tema:** `extends`, `super(...)`, `super.metodo()`, sobrescritura con `@Override`, upcasting.
**Tipo:** consola · **Tiempo estimado:** 1 bloque

> La guía de resolución está en la rama `solucion`, en el archivo `GUIA.md` de esta carpeta.

## Problema

La Ferretería El Tornillo calcula los sueldos en una planilla Excel y comete errores todos los meses. Quieren un programa que liquide los sueldos de sus tres tipos de trabajadores:

| Tipo | Sueldo del mes |
|---|---|
| **Empleado** | Sueldo base. |
| **Vendedor** | Sueldo base + comisión del **3 %** de sus ventas del mes. |
| **Jefatura** | Sueldo base + asignación de responsabilidad: **15 %** del sueldo base más **$10.000** por cada persona a cargo. |

Todos tienen RUT, nombre y un sueldo base que no puede ser menor que el sueldo mínimo. Vendedor y Jefatura **son** empleados: no deben repetir esos atributos ni sus validaciones.

## Código inicial

| Archivo | Estado |
|---|---|
| `Empleado.java` | Resuelto. Es la superclase: no la modifiques. |
| `Main.java` | Imprime la planilla. Tiene líneas comentadas que debes activar a medida que avanzas. |
| `src/test/.../PlanillaTest.java` | Pruebas de autoevaluación. **No compilan hasta que crees `Vendedor` y `Jefatura`.** |

## Requerimientos

**R1. `Vendedor`**
- Hereda de `Empleado` y agrega `ventasMes` (no negativo; si es negativo lanza `IllegalArgumentException`).
- Constructor `Vendedor(String rut, String nombre, int sueldoBase, int ventasMes)` que inicializa la parte heredada con `super(...)`.
- `calcularComision()` retorna el 3 % de las ventas, redondeado.
- Sobrescribe `calcularSueldo()` **reutilizando** el de la superclase (`super.calcularSueldo()`), no leyendo el sueldo base a mano.
- Sobrescribe `obtenerDetalle()` agregando `" | Comision: $..."` al detalle de la superclase.

**R2. `Jefatura`**
- Hereda de `Empleado` y agrega `personasACargo` (entre 1 y 50).
- `calcularAsignacion()` = 15 % del sueldo base (redondeado) + $10.000 por persona a cargo.
- Sobrescribe `calcularSueldo()` y `obtenerDetalle()` (agrega `" | A cargo: N | Asignacion: $..."`).

**R3. Planilla**
- Descomenta las líneas de `Main` y ejecuta. El programa recorre un arreglo `Empleado[]` sin preguntar de qué clase es cada elemento.

**R4. Desafío: herencia de varios niveles**
- Crea `JefeVentas extends Vendedor`, con `metaMes` (mayor que 0). Si `ventasMes >= metaMes` recibe un bono de **$150.000** además de todo lo del vendedor. Su detalle termina en `" | Meta: cumplida"` o `" | Meta: no cumplida"`.

## Resultado esperado

```
11.111.111-1 | Ana Rojas | Base: $650.000 | Sueldo: $650.000
22.222.222-2 | Bruno Diaz | Base: $560.000 | Comision: $120.000 | Sueldo: $680.000
33.333.333-3 | Carla Soto | Base: $1.200.000 | A cargo: 6 | Asignacion: $240.000 | Sueldo: $1.440.000
44.444.444-4 | Diego Pinto | Base: $900.000 | Comision: $360.000 | Meta: cumplida | Sueldo: $1.410.000
Total planilla: $4.180.000
```

## Cómo ejecutar

```bash
mvn compile exec:java    # ejecuta Main
mvn test                 # ejecuta las pruebas de autoevaluación
```

## Preguntas para pensar

1. ¿Por qué `Vendedor` no puede hacer `this.sueldoBase = ...` aunque herede de `Empleado`?
2. ¿Qué pasaría si en `calcularSueldo()` del vendedor escribieras `calcularSueldo() + calcularComision()` sin `super.`?
3. En `Main`, la variable `empleado` es de tipo `Empleado`. ¿Por qué se imprime la comisión del vendedor?
