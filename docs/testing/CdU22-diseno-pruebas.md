# CdU22 · Dar de alta paciente: diseño de las pruebas

Requisito: RF-15, el personal administrativo da de alta pacientes.
Metodología: ISO II, T.4 Gestión de Testing. Diseño (fase 1) y resultados de la ejecución (fase 2, anexo C). Los apartados 1 a 10 describen el código anterior a las correcciones; lo que estas cambian está en el anexo C. Todos los datos de pacientes son ficticios (RGPD).

**Revisión tras integrar `develop`.** No ha cambiado ni se ha movido ninguna clase del alta. `schema.sql` solo añade la tabla `imagen`: `paciente` sigue con `nombre` y `apellidos` `VARCHAR(255)`, así que PF-3 se mantiene. `Servidor` y `AplicacionCliente` han cambiado, pero están excluidos. El diseño de los casos no varía.

**Cómo leer las tablas**

- Cada valor de prueba tiene un código (`D1`, `F3`...) y los casos se escriben con esos códigos. Se definen en el apartado 4.
- Identificador: `unidad-criterio-número`. Unidades: `SP` servicio, `SC` su constructor; `CP` controlador del servidor, `PC` su constructor; `RG`, `RB` y `RC` repositorio MySQL (guardar, buscar y constructor); `MG` y `MB` repositorio en memoria (guardar y buscar); `CA` controlador del cliente, `CC` su constructor; `CH` cliente HTTP, `CU` su constructor; `VA` `obtenerDatos`, `VL` `limpiar`, `VB` `alPulsarAlta`; `CF` y `CT` conversor de fecha (`fromString` y `toString`). Criterios: `EU` each use, `PW` pairwise, `DC` decisiones, `MC` MC/DC, `PF` posible fallo.
- Columna «Test actual»: **✔** el caso ya está cubierto por un test existente; **◐** el test existente comprueba el resultado pero no todo el oráculo (se indica qué falta); **✘** no está cubierto; **= ID** mismas entradas que otro caso de este documento, así que se implementa una sola vez.
- **⚠** caso que puede destapar un fallo (anexo A). El resultado que figura es el **correcto**, no el que daba el código al diseñar los casos. El estado actual de cada uno está en el anexo C.
- `␣` es un espacio.

Los tests actuales se citan sin el paquete: `ServicioPacientesImplTest.daDeAltaPacienteConDatosValidos` es la clase y el método.

---

## 1. Pseudocódigo

### 1.1 `ServicioPacientesImpl`

```
constructor(repositorio, reloj)
    si repositorio es null -> NullPointerException
    si reloj es null       -> NullPointerException

darDeAlta(datos)
    d1: si datos es null -> DatosInvalidosException
    validarTexto(datos.dni, "dni")
    validarTexto(datos.nombre, "nombre")
    validarTexto(datos.apellidos, "apellidos")
    d2: si datos.fechaNacimiento es null -> DatosInvalidosException
    dni <- datos.dni sin espacios en los extremos y en mayúsculas
    validarDni(dni)
    validarFechaNacimiento(datos.fechaNacimiento)
    telefono <- normalizarTelefono(datos.telefono)
    d3: si repositorio.buscarPorDni(dni) encuentra un paciente -> PacienteDuplicadoException
    paciente <- (dni, nombre recortado, apellidos recortados, fechaNacimiento, telefono)
    repositorio.guardar(paciente)
    devolver paciente

validarTexto(valor, campo)
    d4: si valor es null O valor está en blanco -> DatosInvalidosException(campo)

validarDni(dni)
    d5: si dni no son 8 dígitos y una letra -> DatosInvalidosException
    d6: si la letra no es la que corresponde a (número mod 23) -> DatosInvalidosException

validarFechaNacimiento(fecha)
    d7: si fecha es posterior a hoy(reloj) -> DatosInvalidosException

normalizarTelefono(telefono)
    d8: si telefono es null O telefono está en blanco -> devolver null
    limpio <- telefono sin espacios en los extremos
    d9: si limpio no son 9 dígitos -> DatosInvalidosException
    devolver limpio
```

### 1.2 `ControladorPacientes`

```
constructor(servicio)
    si servicio es null -> NullPointerException

darDeAlta(datos)
    intentar
        paciente <- servicio.darDeAlta(datos)
        devolver (true, "Paciente <nombre> <apellidos> (DNI <dni>) dado de alta.")
    si DatosInvalidosException o PacienteDuplicadoException -> devolver (false, mensaje de la excepción)
    si IllegalStateException -> devolver (false, ERROR_INTERNO)
```

### 1.3 `PacienteRepositorioMySql`

```
constructor(dataSource)
    si dataSource es null -> NullPointerException

guardar(paciente)
    si paciente es null -> NullPointerException
    intentar
        abrir conexión, preparar INSERT, poner los 5 parámetros, ejecutar
    si SQLException
        d1: si código es 1062 -> PacienteDuplicadoException
        -> IllegalStateException (con la causa)
    cerrar siempre la conexión y la sentencia

buscarPorDni(dni)
    intentar
        abrir conexión, preparar SELECT, poner el dni, ejecutar
        d2: si no hay fila -> devolver vacío
        devolver el paciente de la fila
    si SQLException -> IllegalStateException (con la causa)
    cerrar siempre la conexión y la sentencia
```

### 1.4 `PacienteRepositorioEnMemoria`

```
guardar(paciente)
    mapa[paciente.dni] <- paciente

buscarPorDni(dni)
    devolver mapa[dni], o vacío si no está
```

### 1.5 `ControladorAltaPacienteImpl` (cliente)

```
constructor(vista, cliente)
    si vista es null   -> NullPointerException
    si cliente es null -> NullPointerException

darDeAlta()
    datos <- vista.obtenerDatos()
    d1: si datos es null -> vista.mostrarError(FECHA_INCORRECTA) y terminar
    intentar
        resultado <- cliente.darDeAlta(datos)
        d2: si resultado.correcto -> vista.mostrarExito(mensaje); vista.limpiar()
            si no                 -> vista.mostrarError(mensaje)
    si ErrorComunicacionException -> vista.mostrarError(SIN_CONEXION)
```

### 1.6 `ClientePacientesHttp` (cliente)

```
constructor(urlServidor, http)
    si urlServidor es null -> NullPointerException
    urlPacientes <- urlServidor sin barras finales + "/pacientes"
                    (IllegalArgumentException si no es una URI)
    si http es null -> NullPointerException

darDeAlta(datos)
    intentar
        enviar POST urlPacientes con los datos en JSON
        devolver la respuesta convertida a ResultadoAlta
    si IOException          -> ErrorComunicacionException
    si InterruptedException -> volver a marcar el hilo como interrumpido; ErrorComunicacionException
```

### 1.7 `VentanaAltaPaciente` (cliente)

```
obtenerDatos()
    texto <- texto del campo fecha sin espacios en los extremos
    d1: si texto está vacío -> fecha <- null
        si no -> intentar fecha <- leer texto como dd/MM/yyyy
                 si DateTimeParseException -> devolver null
    devolver (dni, nombre y apellidos recortados, fecha, telefono recortado)

limpiar()
    vaciar los cinco campos (en la fecha, el valor y el texto) y poner el foco en el DNI

alPulsarAlta(accion)
    al pulsar el botón se ejecuta accion

ConversorFecha.toString(fecha)
    d2: si fecha es null -> "" ; si no -> fecha como dd/MM/yyyy

ConversorFecha.fromString(texto)
    d3: si texto es null O texto está en blanco -> null
    intentar devolver texto recortado leído como dd/MM/yyyy
    si DateTimeParseException -> null
```

---

## 2. Unidades de prueba

Son pruebas unitarias: la unidad es la clase o el método. Lo que una unidad necesita de otra se sustituye por un doble (repositorio, servicio, JDBC, `HttpClient` o vista falsos).

| Módulo | Clase | Método | Código |
|---|---|---|---|
| servidor | `ServicioPacientesImpl` (**método crítico**) | `darDeAlta`, con sus privados `validarTexto`, `validarDni`, `validarFechaNacimiento` y `normalizarTelefono` | SP |
| servidor | `ServicioPacientesImpl` | constructores | SC |
| servidor | `ControladorPacientes` | `darDeAlta` / constructor | CP / PC |
| servidor | `PacienteRepositorioMySql` | `guardar` / `buscarPorDni` / constructor con `DataSource` | RG / RB / RC |
| servidor | `PacienteRepositorioEnMemoria` | `guardar` / `buscarPorDni` | MG / MB |
| cliente | `ControladorAltaPacienteImpl` | `darDeAlta` / constructor | CA / CC |
| cliente | `ClientePacientesHttp` | `darDeAlta` / constructores | CH / CU |
| cliente | `VentanaAltaPaciente` | `obtenerDatos` / `limpiar` / `alPulsarAlta` | VA / VL / VB |
| cliente | `VentanaAltaPaciente.ConversorFecha` | `fromString` / `toString` | CF / CT |

Los métodos privados no se llaman directamente: sus entradas se controlan desde el método público que los usa. `ConversorFecha` es una clase privada y se obtiene con `getConverter()` del campo de fecha.

### Exclusiones justificadas

| Excluido | Justificación |
|---|---|
| Interfaces `ServicioPacientes`, `PacienteRepositorio`, `ClientePacientes`, `ControladorAltaPaciente`, `VistaAltaPaciente` y `Rutas` | No tienen código que ejecutar. Se prueban sus implementaciones, y su Javadoc se usa como oráculo. |
| Records `DatosAltaPaciente`, `Paciente` y `ResultadoAlta` (en los módulos donde existen) | Solo contienen datos, sin lógica propia. Aparecen como entradas y salidas de los casos. |
| Excepciones `DatosInvalidosException`, `PacienteDuplicadoException` y `ErrorComunicacionException` | Solo tienen constructor. Cada una es una salida y tiene al menos un caso que la provoca. |
| `RutasPacientes` | Su método es privado y solo se alcanza arrancando Javalin y enviando HTTP: es una prueba de integración, no unitaria. Ya tiene `RutasPacientesTest` (5 tests). |
| `ServicioPacientesImpl.buscarPorDni` | El alta no lo llama: usa `repositorio.buscarPorDni`. Los tests lo utilizan para comprobar postcondiciones. |
| `PacienteRepositorioMySql()` sin parámetros, `dataSourceDesdeEntorno` y `valorEntornoObligatorio` | Dependen de variables de entorno del proceso, que un test unitario no puede fijar de forma fiable. La decisión `valor == null \|\| valor.isBlank()` queda sin MC/DC. |
| `VentanaAltaPaciente.mostrarExito`, `mostrarError` y `mostrarDialogo` | Abren un diálogo con `showAndWait`, que bloquea hasta que una persona lo cierra. |
| Maquetación de la ventana (constructor, textos y posiciones) | No tiene decisiones ni una salida que el requisito defina. Se ejecuta en todos los casos `VA`. |
| `alPulsarAlta(null)` y `setText(null)` en los campos | Ningún usuario puede provocarlos: el único llamador pasa `controlador::darDeAlta`, y escribir en un campo de texto no produce null. |
| `Servidor` y `AplicacionCliente` | Arranque y conexión de objetos, comunes a todos los casos de uso. |
| `usuario.Paciente` (en los dos módulos) | El alta no la usa; pertenece al informe. |

---

## 3. Variables que influyen en cada método

| Unidad | Parámetros | Estado del objeto (this) | Estado del sistema |
|---|---|---|---|
| SP | `datos`: null, o sus campos `dni` (D), `nombre` (N), `apellidos` (A), `fechaNacimiento` (F) y `telefono` (T) | `repositorio` y `reloj` inyectados | **Fecha de hoy** según el `Clock` (K). **Estado del repositorio** (R): el DNI existe o no, o el repositorio falla |
| SC | `repositorio`, `reloj` | — | — |
| CP | `datos` (J) | `servicio` inyectado | Respuesta del servicio (S) |
| PC | `servicio` | — | — |
| RG | `paciente` (P) | `dataSource` inyectado | Respuesta de la base de datos (B), que incluye si el DNI ya existe (código 1062) |
| RB | `dni` (Q) | `dataSource` inyectado | Respuesta de la base de datos (G): hay fila o no |
| RC | `dataSource` | — | — |
| MG | `paciente` (M) | mapa de pacientes: el DNI ya está o no (E) | — |
| MB | `dni` (Z) | mapa de pacientes (E) | — |
| CA | — | `vista` y `cliente` inyectados | Datos de la vista (V); respuesta del cliente HTTP (C) |
| CC | `vista`, `cliente` | — | — |
| CH | `datos` (Y) | URL y `http` inyectados | Respuesta del servidor o fallo de red (H) |
| CU | `urlServidor` (U), `http` | — | — |
| VA | — | texto de los campos: fecha (W), dni, nombre, apellidos y teléfono (X) | — |
| VL | — | contenido de los campos (O) | — |
| VB | `accion` | — | — |
| CF / CT | `texto` / `fecha` | — | — |

Si `datos` es null en SP no existen las demás variables: se trata como un caso aparte (SP-EU-38) y no entra en las combinaciones. El reloj y el repositorio no influyen en CA, CH ni VA, porque el cliente no valida.

---

## 4. Valores de prueba

Técnicas: **CE** clases de equivalencia; **VL** valores límite (variante pesada: límite, valor adyacente de la clase vecina y valor inmediatamente inferior al límite dentro de la clase); **CJ** conjetura de errores.

### 4.1 SP · `ServicioPacientesImpl.darDeAlta`

`H` es la fecha de referencia, 18/11/2026.

| Variable | Clase de equivalencia | Valor | Técnica |
|---|---|---|---|
| dni | válida: 8 dígitos y su letra | **D1** `12345678Z` | CE |
| dni | válida tras normalizar (minúscula y espacios) | **D2** `␣12345678z␣` | CE, CJ |
| dni | válida, número mínimo | **D3** `00000000T` | VL (límite inferior) |
| dni | válida, anterior al máximo | **D4** `99999998T` | VL (pesada) |
| dni | válida, número máximo | **D5** `99999999R` | VL (límite superior) |
| dni | inválida: ausente | **D6** null | CE, CJ |
| dni | inválida: vacío (longitud 0) | **D7** cadena vacía | CE, CJ |
| dni | inválida: en blanco | **D8** un espacio | CE, CJ |
| dni | inválida: menos de 9 caracteres | **D9** `1234567Z` (8) | CE, VL (adyacente) |
| dni | inválida: más de 9 caracteres | **D10** `100000000W` (10; el número máximo + 1, con su letra) | CE, VL (adyacente) |
| dni | inválida: sin letra | **D11** `123456789` | CE, CJ |
| dni | inválida: otro formato (NIE) | **D12** `X1234567L` | CE, CJ |
| dni | inválida: letra de control incorrecta | **D13** `12345678A` | CE |
| dni | inválida: cadena muy larga | **D14** 256 veces `1` | CJ |
| nombre | válida: de 1 a 255 caracteres | **N1** `Alex` | CE |
| nombre | válida tras recortar | **N2** `␣Alex␣` | CE, CJ |
| nombre | válida, 1 carácter | **N3** `A` | VL (límite inferior) |
| nombre | válida, 254 caracteres | **N4** 254 veces `a` | VL (pesada) |
| nombre | válida, 255 caracteres | **N5** 255 veces `a` | VL (límite superior) |
| nombre | inválida: más de 255 | **N6** 256 veces `a` ⚠ PF-3 | VL (adyacente), CJ |
| nombre | inválida: ausente | **N7** null | CE, CJ |
| nombre | inválida: vacío (longitud 0) | **N8** cadena vacía | CE, VL (adyacente), CJ |
| nombre | inválida: en blanco | **N9** un espacio | CE, CJ |
| nombre | inválida: solo caracteres de control | **N10** `\u0001` ⚠ PF-2 | CJ |
| apellidos | los mismos diez valores que nombre, con `Ejemplo` como valor típico | **A1** a **A10** (A6 ⚠ PF-3, A10 ⚠ PF-2) | CE, VL, CJ |
| fechaNacimiento | válida: pasada | **F1** 12/05/1990 | CE |
| fechaNacimiento | relativa a hoy | **F2** H − 1 | VL |
| fechaNacimiento | relativa a hoy | **F3** H | VL |
| fechaNacimiento | relativa a hoy | **F4** H + 1 | VL |
| fechaNacimiento | inválida: ausente | **F5** null | CE, CJ |
| fechaNacimiento | inválida: imposible para una persona viva | **F6** 01/01/0001 ⚠ PF-4 | CJ |
| hoy (`Clock`) | día anterior al de referencia | **K1** H − 1 | VL |
| hoy (`Clock`) | día de referencia | **K2** H | VL |
| hoy (`Clock`) | día siguiente al de referencia | **K3** H + 1 | VL |
| telefono | válida: 9 dígitos | **T1** `600000001` | CE |
| telefono | válida: ausente (es opcional) | **T2** null | CE, CJ |
| telefono | válida: vacío | **T3** cadena vacía | CE, CJ |
| telefono | válida: en blanco | **T4** un espacio | CE, CJ |
| telefono | válida tras recortar | **T5** `␣600000001␣` | CE, CJ |
| telefono | inválida: menos de 9 | **T6** `60000000` (8) | CE, VL (adyacente) |
| telefono | inválida: más de 9 | **T7** `6000000010` (10) | CE, VL (adyacente) |
| telefono | inválida: con letra | **T8** `60000000A` | CE, CJ |
| telefono | inválida: con prefijo | **T9** `+34600000001` | CJ |
| telefono | inválida: cadena muy larga | **T10** 256 veces `6` | CJ |
| repositorio | el DNI no existe | **R1** vacío | CE |
| repositorio | el DNI ya existe | **R2** contiene un paciente con ese DNI | CE, CJ |
| repositorio | falla al buscar | **R3** `buscarPorDni` lanza `IllegalStateException` | CJ |
| repositorio | falla al guardar | **R4** `guardar` lanza `IllegalStateException` | CJ |
| repositorio | otro alta con el mismo DNI se adelanta | **R5** `buscarPorDni` no encuentra y `guardar` lanza `PacienteDuplicadoException` | CJ |

Notas:

- Una fecha es inválida si es posterior a hoy: F3 con K1, y F4 con K1 o K2. F4 con K3 es «nacido hoy» y es válida.
- El teléfono solo se valida por longitud y por ser dígitos, así que sus límites son de longitud (8, 9 y 10). En el DNI importa además el número, porque de él sale la letra, y por eso tiene límites numéricos. El anterior a D3 (−1) no se puede escribir con 8 dígitos. En DNI y teléfono la clase válida tiene una sola longitud, 9, que es a la vez límite inferior y superior, y no hay un valor inferior dentro de la clase.
- El contrato no fija la longitud máxima de nombre y apellidos. Se toma 255 porque es el tamaño de la columna (`VARCHAR(255)` en `schema.sql`); es una suposición pendiente de decisión del equipo (apartado 11).
- En R2, si el DNI del caso es inválido, el repositorio contiene al paciente `12345678Z`.

| Salida | Casos que la producen |
|---|---|
| `OK`: devuelve el paciente y lo guarda | SP-EU-01 a SP-EU-05 |
| `DI`: `DatosInvalidosException` | SP-EU-06 a 32, 37 y 38 (hay al menos uno por cada mensaje: datos nulos, cada campo obligatorio, formato del DNI, letra, fecha futura y teléfono) |
| `DUP`: `PacienteDuplicadoException`, por la búsqueda o por el guardado | SP-EU-33 y SP-EU-36 |
| `ISE`: `IllegalStateException`, al buscar o al guardar | SP-EU-34 y SP-EU-35 |
| Teléfono devuelto: 9 dígitos o null | SP-EU-01 y SP-EU-02 |

### 4.2 SC, PC, RC y CC · constructores

| Variable | Clase de equivalencia | Valor | Técnica |
|---|---|---|---|
| `repositorio`, `reloj`, `servicio`, `dataSource`, `vista`, `cliente` (cada uno) | válida | un objeto | CE |
| los mismos | inválida | null | CE, CJ |

Salidas: objeto creado (casos 01) y `NullPointerException` (el resto).

### 4.3 CP · `ControladorPacientes.darDeAlta`

| Variable | Clase de equivalencia | Valor | Técnica |
|---|---|---|---|
| datos | presentes | **J1** los de D1 N1 A1 F1 T1 | CE |
| datos | ausentes | **J2** null | CE, CJ |
| servicio | da de alta | **S1** devuelve el paciente `12345678Z`, Alex Ejemplo | CE |
| servicio | rechaza los datos | **S2** lanza `DatosInvalidosException("El campo nombre es obligatorio.")` | CE |
| servicio | el DNI ya existe | **S3** lanza `PacienteDuplicadoException("Ya existe un paciente con el DNI 12345678Z.")` | CE |
| servicio | fallo de persistencia | **S4** lanza `IllegalStateException("detalle interno")` | CE |
| servicio | fallo no previsto | **S5** lanza `NullPointerException` | CJ |

Salidas: alta con su mensaje (S1); rechazo con el mensaje de la excepción (S2 y S3); `(false, ERROR_INTERNO)` (S4); `NullPointerException` propagada (S5).

### 4.4 RG y RB · `PacienteRepositorioMySql`

| Variable | Clase de equivalencia | Valor | Técnica |
|---|---|---|---|
| paciente | completo | **P1** `12345678Z`, Alex, Ejemplo, 12/05/1990, `600000001` | CE |
| paciente | sin teléfono | **P2** P1 con teléfono null | CE, CJ |
| paciente | ausente | **P3** null | CE, CJ |
| paciente | sin fecha de nacimiento | **P4** P1 con fecha null | CJ |
| base de datos (guardar) | inserta | **B1** sin error | CE |
| base de datos (guardar) | el DNI ya existe | **B2** `SQLException` de código 1062 | CE, VL (límite) |
| base de datos (guardar) | otro error | **B3** código 1061 | VL (adyacente) |
| base de datos (guardar) | otro error | **B4** código 1063 | VL (adyacente) |
| base de datos (guardar) | otro error | **B5** código 0 al ejecutar | CE |
| base de datos (guardar) | sin conexión | **B6** `getConnection` lanza `SQLException` | CJ |
| dni (buscar) | con formato | **Q1** `12345678Z` | CE |
| dni (buscar) | vacío | **Q2** cadena vacía | CJ |
| dni (buscar) | ausente | **Q3** null | CJ |
| base de datos (buscar) | hay fila | **G1** fila de P1 | CE |
| base de datos (buscar) | hay fila sin teléfono | **G2** fila de P2 | CE, CJ |
| base de datos (buscar) | no hay fila | **G3** sin filas | CE |
| base de datos (buscar) | error | **G4** `SQLException` | CE |

La decisión «código es 1062» parte los enteros en tres clases: menor, igual y mayor. La clase «igual» tiene un solo valor, así que no hay un valor inferior dentro de ella.

Salidas de guardar: inserción (RG-PW-01), `PacienteDuplicadoException` (RG-PW-02), `IllegalStateException` (RG-PW-05) y `NullPointerException` (RG-PW-13). Salidas de buscar: paciente (RB-PW-01), vacío (RB-PW-03) e `IllegalStateException` (RB-PW-04).

### 4.5 MG y MB · `PacienteRepositorioEnMemoria`

| Variable | Clase de equivalencia | Valor | Técnica |
|---|---|---|---|
| mapa | el DNI no está | **E1** vacío | CE |
| mapa | el DNI está | **E2** contiene `12345678Z`, Alex Ejemplo | CE, CJ |
| paciente (guardar) | presente | **M1** `12345678Z`, Sam Prueba | CE |
| paciente (guardar) | ausente | **M2** null | CJ |
| dni (buscar) | el que contiene E2 | **Z1** `12345678Z` | CE |
| dni (buscar) | otro | **Z2** `00000000T` | CE |
| dni (buscar) | ausente | **Z3** null | CJ |

Salidas de guardar: guardado (MG-PW-01), `PacienteDuplicadoException` (MG-PW-02 ⚠ PF-6) y `NullPointerException` (MG-PW-03). Salidas de buscar: paciente (MB-PW-02) y vacío (MB-PW-03).

### 4.6 CA · `ControladorAltaPacienteImpl.darDeAlta`

| Variable | Clase de equivalencia | Valor | Técnica |
|---|---|---|---|
| datos de la vista | presentes | **V1** los de D1 N1 A1 F1 T1 | CE |
| datos de la vista | fecha mal escrita | **V2** null | CE, CJ |
| cliente | alta correcta | **C1** `(true, "Paciente dado de alta.")` | CE |
| cliente | alta rechazada | **C2** `(false, "Ya existe un paciente con el DNI 12345678Z.")` | CE |
| cliente | sin conexión | **C3** lanza `ErrorComunicacionException` | CE |
| cliente | respuesta nula | **C4** null ⚠ PF-5 | CJ |
| cliente | fallo no previsto | **C5** lanza `IllegalArgumentException` | CJ |

Salidas: éxito y formulario limpio (C1); error del servidor y formulario intacto (C2); error de conexión (C3); error por respuesta nula (C4); excepción propagada (C5); error de fecha sin llamar al cliente (V2).

### 4.7 CH y CU · `ClientePacientesHttp`

| Variable | Clase de equivalencia | Valor | Técnica |
|---|---|---|---|
| datos | completos | **Y1** los de D1 N1 A1 F1 T1 | CE |
| datos | ausentes | **Y2** null | CJ |
| datos | con campos nulos | **Y3** Y1 con fecha y teléfono null | CE, CJ |
| respuesta | alta correcta | **H1** 201, `{"correcto":true,"mensaje":"Paciente dado de alta."}` | CE |
| respuesta | rechazo | **H2** 400, `{"correcto":false,"mensaje":"La letra del campo dni no es correcta."}` | CE |
| respuesta | no es JSON | **H3** 500, `Internal server error` | CE, CJ |
| respuesta | cuerpo vacío | **H4** 200, cadena vacía | CJ |
| respuesta | JSON nulo | **H5** 200, `null` ⚠ PF-5 | CJ |
| respuesta | otro JSON | **H6** 200, `{"otro":1}` | CJ |
| respuesta | fallo de red | **H7** `IOException` | CE |
| respuesta | hilo interrumpido | **H8** `InterruptedException` | CE |
| urlServidor | sin barra final | **U1** `http://localhost:7070` | CE |
| urlServidor | con una barra final | **U2** `http://localhost:7070/` | CE |
| urlServidor | con varias barras finales | **U3** `http://localhost:7070///` | CJ |
| urlServidor | ausente | **U4** null | CE, CJ |
| urlServidor | no es una URI | **U5** `http://servidor malo` (con un espacio) | CE, CJ |
| urlServidor | sin esquema http | **U6** `localhost:7070` (⚠ PF-7 al enviar) | CJ |
| http | presente / ausente | un `HttpClient` / null | CE, CJ |

Salidas de `darDeAlta`: resultado true (CH-PW-01), resultado false (CH-PW-02), `ErrorComunicacionException` por `IOException` (CH-PW-07) y por `InterruptedException` (CH-PW-08). Salidas del constructor: objeto (CU-PW-01), `NullPointerException` (CU-PW-04) e `IllegalArgumentException` (CU-PW-05).

### 4.8 VA, VL, VB, CF y CT · `VentanaAltaPaciente`

| Variable | Clase de equivalencia | Valor | Técnica |
|---|---|---|---|
| fecha | vacía | **W1** cadena vacía | CE, CJ |
| fecha | en blanco | **W2** tres espacios | CE, CJ |
| fecha | válida | **W3** `12/05/1990` | CE |
| fecha | válida tras recortar | **W4** `␣12/05/1990␣` | CE, CJ |
| fecha | otro separador | **W5** `12-05-1990` | CE, CJ |
| fecha | día mínimo y mes mínimo | **W6** `01/01/2000` | VL (límite) |
| fecha | día 0 | **W7** `00/01/2000` | VL (adyacente) |
| fecha | día anterior al máximo | **W8** `30/01/2000` | VL (pesada) |
| fecha | día máximo | **W9** `31/01/2000` | VL (límite) |
| fecha | día 32 | **W10** `32/01/2000` | VL (adyacente) |
| fecha | mes anterior al máximo | **W11** `15/11/2000` | VL (pesada) |
| fecha | mes máximo | **W12** `15/12/2000` | VL (límite) |
| fecha | mes 13 | **W13** `15/13/2000` | VL (adyacente) |
| fecha | mes 0 | **W14** `15/00/2000` | VL (adyacente) |
| fecha | anterior al último día de febrero | **W15** `27/02/2001` | VL (pesada) |
| fecha | último día de febrero | **W16** `28/02/2001` | VL (límite) |
| fecha | 29 de febrero en año no bisiesto | **W17** `29/02/2001` ⚠ PF-1 | VL (adyacente), CJ |
| fecha | 29 de febrero en año bisiesto | **W18** `29/02/2000` | CE |
| fecha | día que no existe en ese mes | **W19** `31/02/2000` ⚠ PF-1 | CJ |
| fecha | cadena muy larga | **W20** 256 veces `1` | CJ |
| dni, nombre, apellidos y teléfono (cada uno) | con texto | **X1** `12345678Z` / `Alex` / `Ejemplo` / `600000001` | CE |
| los mismos | vacío | **X2** cadena vacía | CE, CJ |
| los mismos | con espacios en los extremos | **X3** X1 con un espacio a cada lado | CE, CJ |
| los mismos | cadena muy larga | **X4** 256 veces `x` | CJ |
| formulario (VL) | relleno | **O1** X1 y fecha 12/05/1990 | CE |
| formulario (VL) | vacío | **O2** todo vacío | CE, CJ |
| formulario (VL) | con la fecha mal escrita | **O3** X1 y fecha `12-05-1990` | CJ |
| accion (VB) | presente | una acción que anota que se ha ejecutado | CE |
| texto (CF) | ausente, vacío, en blanco | null; cadena vacía; tres espacios | CE, CJ |
| texto (CF) | válido, válido tras recortar | `12/05/1990`; `␣12/05/1990␣` | CE |
| texto (CF) | mal escrito, imposible | `12-05-1990`; `31/02/2000` ⚠ PF-1 | CE, CJ |
| fecha (CT) | ausente, presente | null; 12/05/1990 | CE, CJ |

La vista no valida los textos: los recorta y los pasa. Salidas de VA: datos con fecha (VA-EU-03), datos con fecha nula (VA-EU-01) y null (VA-EU-05).

---

## 5. Número máximo de casos (producto cartesiano)

| Unidad | Cálculo | Casos |
|---|---|---|
| SP | 14 (D) × 10 (N) × 10 (A) × 6 (F) × 3 (K) × 10 (T) × 5 (R) = 1.260.000, más `datos` null | 1.260.001 |
| SC / CC | 2 × 2 cada uno | 4 / 4 |
| PC / RC | 2 cada uno | 2 / 2 |
| CP | 2 × 5 | 10 |
| RG | 4 × 6 | 24 |
| RB | 3 × 4 | 12 (8 posibles: con DNI vacío o null no puede haber fila) |
| MG / MB | 2 × 2 / 3 × 2 | 4 / 6 |
| CA | 2 × 5 | 10 |
| CH / CU | 3 × 8 / 6 × 2 | 24 / 12 |
| VA | 20 × 4 × 4 × 4 × 4 | 5.120 |
| VL / VB / CF / CT | una variable cada uno | 3 / 1 / 7 / 2 |

---

## 6. Each use

El mínimo de each use es el número de valores de la variable que más tiene (14 en SP). Con ese mínimo, varios valores inválidos irían en el mismo caso y el primero ocultaría a los demás. Por eso **los valores válidos se combinan entre sí y cada valor inválido va solo**, con el resto válidos.

### 6.1 SP

- Precondiciones: el reloj y el repositorio están como indican K y R.
- Postcondiciones: **PC1**, el repositorio contiene al paciente devuelto; **PC2**, el repositorio queda como estaba y no se guarda nada.
- Oráculo de `OK`: el paciente devuelto es igual al esperado, campo a campo (textos recortados, DNI en mayúsculas y teléfono null si no se indica).
- Oráculo de `DI(campo)`: `DatosInvalidosException` cuyo mensaje contiene el nombre del campo.

| Id | Objetivo | Entradas y precondiciones | Resultado esperado | Post. | Test actual |
|---|---|---|---|---|---|
| SP-EU-01 | Alta normal | D1 N1 A1 F1 K2 T1 R1 | OK | PC1 | ✔ `ServicioPacientesImplTest.daDeAltaPacienteConDatosValidos` y `.permiteRecuperarPacientePorDniTrasElAlta` |
| SP-EU-02 | Normaliza DNI y textos; nacido hoy con reloj H−1; teléfono null | D2 N2 A2 F2 K1 T2 R1 | OK | PC1 | ✘ |
| SP-EU-03 | DNI y textos mínimos; nacido hoy; teléfono vacío | D3 N3 A3 F3 K2 T3 R1 | OK | PC1 | ✘ |
| SP-EU-04 | Textos de 254; nacido hoy con reloj H+1; teléfono en blanco | D4 N4 A4 F4 K3 T4 R1 | OK | PC1 | ✘ |
| SP-EU-05 | DNI máximo; textos de 255; nacido hace dos días; teléfono con espacios | D5 N5 A5 F2 K3 T5 R1 | OK | PC1 | ✘ |
| SP-EU-06 | DNI null | D6 N1 A1 F1 K2 T2 R1 | DI(dni) | PC2 | ◐ `ServicioPacientesImplTest.rechazaDniNuloConMensajeClaro` (falta PC2) |
| SP-EU-07 | DNI vacío | D7 N1 A1 F1 K2 T2 R1 | DI(dni) | PC2 | ◐ `ServicioPacientesImplTest.rechazaDniVacioConMensajeClaro` (falta PC2) |
| SP-EU-08 | DNI en blanco | D8 N1 A1 F1 K2 T2 R1 | DI(dni) | PC2 | ✘ |
| SP-EU-09 | DNI corto | D9 N1 A1 F1 K2 T2 R1 | DI(dni) | PC2 | ◐ `ServicioPacientesImplTest.rechazaDniConFormatoIncorrecto` (falta PC2) |
| SP-EU-10 | DNI largo | D10 N1 A1 F1 K2 T2 R1 | DI(dni) | PC2 | ✘ |
| SP-EU-11 | DNI sin letra | D11 N1 A1 F1 K2 T2 R1 | DI(dni) | PC2 | ◐ `ServicioPacientesImplTest.rechazaDniConFormatoIncorrecto` (falta PC2) |
| SP-EU-12 | NIE | D12 N1 A1 F1 K2 T2 R1 | DI(dni) | PC2 | ✘ |
| SP-EU-13 | Letra incorrecta | D13 N1 A1 F1 K2 T2 R1 | DI(dni) | PC2 | ◐ `ServicioPacientesImplTest.rechazaDniConLetraIncorrecta` (falta PC2) |
| SP-EU-14 | DNI muy largo | D14 N1 A1 F1 K2 T2 R1 | DI(dni) | PC2 | ✘ |
| SP-EU-15 | Nombre de 256 | D1 N6 A1 F1 K2 T2 R1 | DI(nombre) ⚠ PF-3 | PC2 | ✘ |
| SP-EU-16 | Nombre null | D1 N7 A1 F1 K2 T2 R1 | DI(nombre) | PC2 | ◐ `ServicioPacientesImplTest.rechazaNombreNuloConMensajeClaro` (falta PC2) |
| SP-EU-17 | Nombre vacío | D3 N8 A1 F1 K2 T1 R1 | DI(nombre) | PC2 | ◐ `ServicioPacientesImplTest.altaConDatosInvalidosNoGuardaElPaciente` (falta comprobar el mensaje) |
| SP-EU-18 | Nombre en blanco | D1 N9 A1 F1 K2 T2 R1 | DI(nombre) | PC2 | ◐ `ServicioPacientesImplTest.rechazaNombreVacioConMensajeClaro` (falta PC2) |
| SP-EU-19 | Nombre con solo un carácter de control | D1 N10 A1 F1 K2 T2 R1 | DI(nombre) ⚠ PF-2 | PC2 | ✘ |
| SP-EU-20 | Apellidos de 256 | D1 N1 A6 F1 K2 T2 R1 | DI(apellidos) ⚠ PF-3 | PC2 | ✘ |
| SP-EU-21 | Apellidos null | D1 N1 A7 F1 K2 T2 R1 | DI(apellidos) | PC2 | ◐ `ServicioPacientesImplTest.rechazaApellidosNulosConMensajeClaro` (falta PC2) |
| SP-EU-22 | Apellidos vacíos | D1 N1 A8 F1 K2 T2 R1 | DI(apellidos) | PC2 | ◐ `ServicioPacientesImplTest.rechazaApellidosVaciosConMensajeClaro` (falta PC2) |
| SP-EU-23 | Apellidos en blanco | D1 N1 A9 F1 K2 T2 R1 | DI(apellidos) | PC2 | ✘ |
| SP-EU-24 | Apellidos con solo un carácter de control | D1 N1 A10 F1 K2 T2 R1 | DI(apellidos) ⚠ PF-2 | PC2 | ✘ |
| SP-EU-25 | Fecha null | D1 N1 A1 F5 K2 T2 R1 | DI(fechaNacimiento) | PC2 | ◐ `ServicioPacientesImplTest.rechazaFechaNacimientoNulaConMensajeClaro` (falta PC2) |
| SP-EU-26 | Fecha del año 1 | D1 N1 A1 F6 K2 T2 R1 | DI(fechaNacimiento) ⚠ PF-4 | PC2 | ✘ |
| SP-EU-27 | Nacido mañana (hoy + 1) | D1 N1 A1 F4 K2 T2 R1 | DI(fechaNacimiento) | PC2 | ◐ `ServicioPacientesImplTest.rechazaFechaNacimientoFutura` (falta PC2) |
| SP-EU-28 | Teléfono de 8 dígitos | D1 N1 A1 F1 K2 T6 R1 | DI(telefono) | PC2 | ✘ |
| SP-EU-29 | Teléfono de 10 dígitos | D1 N1 A1 F1 K2 T7 R1 | DI(telefono) | PC2 | ✘ |
| SP-EU-30 | Teléfono con letra | D1 N1 A1 F1 K2 T8 R1 | DI(telefono) | PC2 | ◐ `ServicioPacientesImplTest.rechazaTelefonoConFormatoIncorrecto` (falta PC2) |
| SP-EU-31 | Teléfono con prefijo | D1 N1 A1 F1 K2 T9 R1 | DI(telefono) | PC2 | ✘ |
| SP-EU-32 | Teléfono muy largo | D1 N1 A1 F1 K2 T10 R1 | DI(telefono) | PC2 | ✘ |
| SP-EU-33 | El DNI ya existe | D1 N1 A1 F1 K2 T1 R2 | DUP | PC2 | ✔ `ServicioPacientesImplTest.rechazaAltaConDniDuplicado` y `.altaDuplicadaNoModificaElPacienteOriginal` |
| SP-EU-34 | El repositorio falla al buscar | D1 N1 A1 F1 K2 T1 R3 | ISE | PC2 | ✘ |
| SP-EU-35 | El repositorio falla al guardar | D1 N1 A1 F1 K2 T1 R4 | ISE | PC2 | ✘ |
| SP-EU-36 | Otro alta se adelanta | D1 N1 A1 F1 K2 T1 R5 | DUP | PC2 | ✘ |
| SP-EU-37 | La fecha es posterior a hoy porque el reloj está en H−1 | D1 N1 A1 F3 K1 T2 R1 | DI(fechaNacimiento) | PC2 | ✘ |
| SP-EU-38 | Datos null | `datos` null, repositorio vacío | `DatosInvalidosException` | PC2 | ✔ `ServicioPacientesImplTest.rechazaAltaConDatosNulos` (sin DNI no hay nada que buscar en el repositorio) |

### 6.2 VA

Precondición: ventana recién creada, con los campos rellenos según los códigos, en el orden fecha, dni, nombre, apellidos y teléfono. `DATOS`: los textos recortados y la fecha leída. `NULO`: devuelve null. Postcondición: los campos no cambian.

| Id | Objetivo | Entradas | Resultado esperado | Test actual |
|---|---|---|---|---|
| VA-EU-01 | Fecha vacía | W1 X1 X2 X2 X2 | DATOS con fecha nula | ◐ `VentanaAltaPacienteTest.fechaVaciaSeEnviaComoNula` (falta comprobar los demás campos) |
| VA-EU-02 | Fecha en blanco y textos muy largos | W2 X4 X4 X4 X4 | DATOS con fecha nula | ✘ |
| VA-EU-03 | Fecha válida y textos con espacios | W3 X3 X3 X1 X1 | DATOS | ✔ `VentanaAltaPacienteTest.obtenerDatosLeeLosCamposSinEspacios` |
| VA-EU-04 | Fecha con espacios | W4 X2 X1 X3 X3 | DATOS | ✘ |
| VA-EU-05 | Otro separador | W5 X2 X2 X2 X2 | NULO | ✔ `VentanaAltaPacienteTest.fechaMalEscritaDevuelveNulo` |
| VA-EU-06 | Día y mes mínimos | W6 X1 X1 X1 X1 | DATOS | ✘ |
| VA-EU-07 | Día 0 | W7 X1 X1 X1 X1 | NULO | ✘ |
| VA-EU-08 | Día anterior al máximo | W8 X1 X1 X1 X1 | DATOS | ✘ |
| VA-EU-09 | Día máximo | W9 X1 X1 X1 X1 | DATOS | ✘ |
| VA-EU-10 | Día 32 | W10 X1 X1 X1 X1 | NULO | ✘ |
| VA-EU-11 | Mes anterior al máximo | W11 X1 X1 X1 X1 | DATOS | ✘ |
| VA-EU-12 | Mes máximo | W12 X1 X1 X1 X1 | DATOS | ✘ |
| VA-EU-13 | Mes 13 | W13 X1 X1 X1 X1 | NULO | ✘ |
| VA-EU-14 | Mes 0 | W14 X1 X1 X1 X1 | NULO | ✘ |
| VA-EU-15 | Anterior al último de febrero | W15 X1 X1 X1 X1 | DATOS | ✘ |
| VA-EU-16 | Último de febrero | W16 X1 X1 X1 X1 | DATOS | ✘ |
| VA-EU-17 | 29 de febrero en año no bisiesto | W17 X1 X1 X1 X1 | NULO ⚠ PF-1 | ✘ |
| VA-EU-18 | 29 de febrero en año bisiesto | W18 X1 X1 X1 X1 | DATOS | ✘ |
| VA-EU-19 | 31 de febrero | W19 X1 X1 X1 X1 | NULO ⚠ PF-1 | ✘ |
| VA-EU-20 | Fecha muy larga | W20 X1 X1 X1 X1 | NULO | ✘ |

### 6.3 Unidades con una sola variable

Con una variable, each use es el conjunto completo de valores y no hay pares.

| Id | Objetivo | Entradas y precondiciones | Resultado esperado | Post. | Test actual |
|---|---|---|---|---|---|
| PC-EU-01 | Constructor correcto | servicio presente | objeto creado que usa ese servicio | — | ✔ `ControladorPacientesTest.altaCorrectaDevuelveResultadoCorrecto` |
| PC-EU-02 | Servicio obligatorio | servicio null | `NullPointerException` | no se crea | ✘ |
| RC-EU-01 | Constructor correcto | `DataSource` presente | objeto creado que usa ese origen | — | ✔ `PacienteRepositorioMySqlTest.guardarInsertaTodosLosCampos` |
| RC-EU-02 | `DataSource` obligatorio | null | `NullPointerException` | no se crea | ✔ `PacienteRepositorioMySqlTest.dataSourceNuloLanzaExcepcion` |
| VL-EU-01 | Limpiar relleno | O1 | `obtenerDatos` devuelve textos vacíos y fecha nula | todos los campos vacíos | ✔ `VentanaAltaPacienteTest.limpiarVaciaTodosLosCampos` |
| VL-EU-02 | Limpiar vacío | O2 | igual que VL-EU-01 | todos los campos vacíos | ✘ |
| VL-EU-03 | Limpiar con la fecha mal escrita | O3 | igual que VL-EU-01 | el texto de la fecha también se borra | ✘ |
| VB-EU-01 | El botón ejecuta la acción | acción presente; se pulsa el botón | la acción se ejecuta | — | ✔ `VentanaAltaPacienteTest.pulsarElBotonEjecutaLaAccionDelControlador` |
| CF-EU-01 | Texto null | null | null | — | ✘ |
| CF-EU-02 | Texto vacío | cadena vacía | null | — | ✘ |
| CF-EU-03 | Texto en blanco | tres espacios | null | — | ✘ |
| CF-EU-04 | Texto válido | `12/05/1990` | 12/05/1990 | — | ✘ |
| CF-EU-05 | Texto válido con espacios | `␣12/05/1990␣` | 12/05/1990 | — | ✘ |
| CF-EU-06 | Texto mal escrito | `12-05-1990` | null | — | ✘ |
| CF-EU-07 | Fecha imposible | `31/02/2000` | null ⚠ PF-1 | — | ✘ |
| CT-EU-01 | Fecha null | null | cadena vacía | — | ✘ |
| CT-EU-02 | Fecha presente | 12/05/1990 | `12/05/1990` | — | ✘ |

Precondición de VL, VB, CF y CT: ventana recién creada.

### 6.4 Unidades con dos variables

En ellas cada par es una combinación completa, así que pairwise coincide con el producto cartesiano. Los casos están en el apartado 7.4 y este es el subconjunto que cumple each use:

| Unidad | Casos que forman each use | Número |
|---|---|---|
| SC | SC-PW-01 a 03 | 3 |
| CP | CP-PW-01 a 04 y 10 | 5 |
| RG | RG-PW-01 a 07, 13 y 19 | 9 |
| RB | RB-PW-01 a 05 y 07 | 6 |
| MG | MG-PW-01 a 03 | 3 |
| MB | MB-PW-02, 03 y 05 | 3 |
| CA | CA-PW-01 a 04 y 10 | 5 |
| CC | CC-PW-01 a 03 | 3 |
| CH | CH-PW-01, 02, 03, 05, 07, 08, 12 y 22 | 8 |
| CU | CU-PW-01 a 07 | 7 |

---

## 7. Pairwise

### 7.1 SP

**Construcción.** El algoritmo usado es de crecimiento por parámetros (**IPO**, *In-Parameter-Order*): se cubren primero los pares de dos variables y después se añade una variable cada vez.

1. Se ordenan las variables: D (14 valores), N (10), A (10), T (10), F (6), R (5) y K (3).
2. Se parte del producto D × N: 140 casos. Es el mínimo posible, porque cada par (D, N) necesita un caso distinto.
3. Crecimiento horizontal: se añade la variable siguiente y, en cada caso, se elige el valor que cubre más pares nuevos con las variables ya puestas.
4. Crecimiento vertical: los pares que queden sin cubrir se meten en casos nuevos.
5. Se repiten los pasos 3 y 4 con cada variable.

Un programa auxiliar, que no forma parte del proyecto, ha construido la tabla y ha comprobado los **1.399 pares**: no falta ninguno. Salen **157 casos**. `datos` null no forma pares y se queda en SP-EU-38.

- **Objetivo común:** cubrir los pares de valores.
- **Precondiciones:** las de K y R. **Postcondiciones:** PC1 si el resultado es OK y PC2 en el resto.
- **Oráculo con varios campos inválidos:** `DI(a, b)` significa `DatosInvalidosException` cuyo mensaje nombra **alguno** de esos campos, porque el contrato no fija en qué orden se validan.
- **⚠** marca los casos en los que lo único inválido es un valor de un posible fallo.
- **Test actual:** ninguno cubre estos casos; «= ID» señala los que repiten un caso de otro apartado.

| Id | D | N | A | F | K | T | R | Resultado esperado | Test actual |
|---|---|---|---|---|---|---|---|---|---|
| SP-PW-001 | D1 | N1 | A1 | F1 | K1 | T1 | R1 | OK | ✘ |
| SP-PW-002 | D1 | N2 | A2 | F2 | K2 | T2 | R2 | DUP | ✘ |
| SP-PW-003 | D1 | N3 | A3 | F3 | K3 | T3 | R3 | ISE | ✘ |
| SP-PW-004 | D1 | N4 | A4 | F4 | K1 | T4 | R4 | DI(fechaNacimiento) | ✘ |
| SP-PW-005 | D1 | N5 | A5 | F5 | K2 | T5 | R5 | DI(fechaNacimiento) | ✘ |
| SP-PW-006 | D1 | N6 | A6 | F6 | K3 | T6 | R1 | DI(nombre, apellidos, fechaNacimiento, telefono) | ✘ |
| SP-PW-007 | D1 | N7 | A7 | F1 | K3 | T7 | R2 | DI(nombre, apellidos, telefono) | ✘ |
| SP-PW-008 | D1 | N8 | A8 | F2 | K1 | T8 | R3 | DI(nombre, apellidos, telefono) | ✘ |
| SP-PW-009 | D1 | N9 | A9 | F3 | K2 | T9 | R4 | DI(nombre, apellidos, telefono) | ✘ |
| SP-PW-010 | D1 | N10 | A10 | F4 | K3 | T10 | R5 | DI(nombre, apellidos, telefono) | ✘ |
| SP-PW-011 | D2 | N1 | A2 | F5 | K3 | T3 | R4 | DI(fechaNacimiento) | ✘ |
| SP-PW-012 | D2 | N2 | A1 | F6 | K2 | T4 | R3 | DI(fechaNacimiento) ⚠ | ✘ |
| SP-PW-013 | D2 | N3 | A4 | F2 | K1 | T1 | R5 | DUP | ✘ |
| SP-PW-014 | D2 | N4 | A3 | F1 | K2 | T2 | R1 | OK | ✘ |
| SP-PW-015 | D2 | N5 | A6 | F3 | K1 | T7 | R2 | DI(apellidos, fechaNacimiento, telefono) | ✘ |
| SP-PW-016 | D2 | N6 | A5 | F4 | K2 | T8 | R2 | DI(nombre, fechaNacimiento, telefono) | ✘ |
| SP-PW-017 | D2 | N7 | A8 | F6 | K1 | T5 | R4 | DI(nombre, apellidos, fechaNacimiento) | ✘ |
| SP-PW-018 | D2 | N8 | A7 | F5 | K1 | T6 | R1 | DI(nombre, apellidos, fechaNacimiento, telefono) | ✘ |
| SP-PW-019 | D2 | N9 | A10 | F1 | K1 | T9 | R3 | DI(nombre, apellidos, telefono) | ✘ |
| SP-PW-020 | D2 | N10 | A9 | F2 | K1 | T10 | R1 | DI(nombre, apellidos, telefono) | ✘ |
| SP-PW-021 | D3 | N1 | A3 | F2 | K3 | T4 | R5 | DUP | ✘ |
| SP-PW-022 | D3 | N2 | A4 | F1 | K2 | T3 | R1 | OK | ✘ |
| SP-PW-023 | D3 | N3 | A1 | F4 | K3 | T2 | R4 | ISE | ✘ |
| SP-PW-024 | D3 | N4 | A2 | F3 | K3 | T1 | R3 | ISE | ✘ |
| SP-PW-025 | D3 | N5 | A7 | F6 | K3 | T8 | R5 | DI(apellidos, fechaNacimiento, telefono) | ✘ |
| SP-PW-026 | D3 | N6 | A8 | F5 | K2 | T7 | R2 | DI(nombre, apellidos, fechaNacimiento, telefono) | ✘ |
| SP-PW-027 | D3 | N7 | A5 | F3 | K2 | T6 | R3 | DI(nombre, telefono) | ✘ |
| SP-PW-028 | D3 | N8 | A6 | F4 | K2 | T5 | R3 | DI(nombre, apellidos, fechaNacimiento) | ✘ |
| SP-PW-029 | D3 | N9 | A9 | F5 | K3 | T10 | R2 | DI(nombre, apellidos, fechaNacimiento, telefono) | ✘ |
| SP-PW-030 | D3 | N10 | A10 | F6 | K2 | T9 | R2 | DI(nombre, apellidos, fechaNacimiento, telefono) | ✘ |
| SP-PW-031 | D4 | N1 | A4 | F3 | K1 | T2 | R3 | DI(fechaNacimiento) | ✘ |
| SP-PW-032 | D4 | N2 | A3 | F4 | K1 | T1 | R4 | DI(fechaNacimiento) | ✘ |
| SP-PW-033 | D4 | N3 | A2 | F1 | K2 | T4 | R1 | OK | ✘ |
| SP-PW-034 | D4 | N4 | A1 | F2 | K3 | T3 | R5 | DUP | ✘ |
| SP-PW-035 | D4 | N5 | A8 | F1 | K3 | T6 | R4 | DI(apellidos, telefono) | ✘ |
| SP-PW-036 | D4 | N6 | A7 | F2 | K1 | T5 | R4 | DI(nombre, apellidos) | ✘ |
| SP-PW-037 | D4 | N7 | A6 | F5 | K1 | T8 | R5 | DI(nombre, apellidos, fechaNacimiento, telefono) | ✘ |
| SP-PW-038 | D4 | N8 | A5 | F6 | K3 | T7 | R4 | DI(nombre, fechaNacimiento, telefono) | ✘ |
| SP-PW-039 | D4 | N9 | A9 | F6 | K2 | T1 | R2 | DI(nombre, apellidos, fechaNacimiento) | ✘ |
| SP-PW-040 | D4 | N10 | A10 | F5 | K1 | T2 | R3 | DI(nombre, apellidos, fechaNacimiento) | ✘ |
| SP-PW-041 | D5 | N1 | A5 | F4 | K2 | T9 | R1 | DI(fechaNacimiento, telefono) | ✘ |
| SP-PW-042 | D5 | N2 | A6 | F3 | K3 | T10 | R5 | DI(apellidos, telefono) | ✘ |
| SP-PW-043 | D5 | N3 | A7 | F6 | K1 | T3 | R2 | DI(apellidos, fechaNacimiento) | ✘ |
| SP-PW-044 | D5 | N4 | A8 | F5 | K2 | T4 | R2 | DI(apellidos, fechaNacimiento) | ✘ |
| SP-PW-045 | D5 | N5 | A1 | F1 | K3 | T5 | R1 | OK | ✘ |
| SP-PW-046 | D5 | N6 | A2 | F2 | K1 | T6 | R5 | DI(nombre, telefono) | ✘ |
| SP-PW-047 | D5 | N7 | A3 | F4 | K2 | T7 | R1 | DI(nombre, fechaNacimiento, telefono) | ✘ |
| SP-PW-048 | D5 | N8 | A4 | F3 | K3 | T8 | R4 | DI(nombre, telefono) | ✘ |
| SP-PW-049 | D5 | N9 | A9 | F4 | K1 | T2 | R5 | DI(nombre, apellidos, fechaNacimiento) | ✘ |
| SP-PW-050 | D5 | N10 | A10 | F3 | K2 | T1 | R1 | DI(nombre, apellidos) ⚠ | ✘ |
| SP-PW-051 | D6 | N1 | A6 | F2 | K3 | T9 | R2 | DI(dni, apellidos, telefono) | ✘ |
| SP-PW-052 | D6 | N2 | A5 | F1 | K1 | T10 | R3 | DI(dni, telefono) | ✘ |
| SP-PW-053 | D6 | N3 | A8 | F4 | K2 | T3 | R5 | DI(dni, apellidos, fechaNacimiento) | ✘ |
| SP-PW-054 | D6 | N4 | A7 | F3 | K2 | T4 | R3 | DI(dni, apellidos) | ✘ |
| SP-PW-055 | D6 | N5 | A2 | F6 | K3 | T5 | R4 | DI(dni, fechaNacimiento) | ✘ |
| SP-PW-056 | D6 | N6 | A1 | F5 | K1 | T6 | R2 | DI(dni, nombre, fechaNacimiento, telefono) | ✘ |
| SP-PW-057 | D6 | N7 | A4 | F2 | K3 | T7 | R1 | DI(dni, nombre, telefono) | ✘ |
| SP-PW-058 | D6 | N8 | A3 | F1 | K1 | T8 | R5 | DI(dni, nombre, telefono) | ✘ |
| SP-PW-059 | D6 | N9 | A9 | F1 | K2 | T3 | R3 | DI(dni, nombre, apellidos) | ✘ |
| SP-PW-060 | D6 | N10 | A10 | F2 | K3 | T4 | R4 | DI(dni, nombre, apellidos) | ✘ |
| SP-PW-061 | D7 | N1 | A7 | F6 | K2 | T10 | R4 | DI(dni, apellidos, fechaNacimiento, telefono) | ✘ |
| SP-PW-062 | D7 | N2 | A8 | F5 | K1 | T9 | R1 | DI(dni, apellidos, fechaNacimiento, telefono) | ✘ |
| SP-PW-063 | D7 | N3 | A5 | F5 | K3 | T1 | R2 | DI(dni, fechaNacimiento) | ✘ |
| SP-PW-064 | D7 | N4 | A6 | F6 | K1 | T2 | R3 | DI(dni, apellidos, fechaNacimiento) | ✘ |
| SP-PW-065 | D7 | N5 | A3 | F3 | K2 | T5 | R2 | DI(dni) | ✘ |
| SP-PW-066 | D7 | N6 | A4 | F4 | K3 | T6 | R5 | DI(dni, nombre, telefono) | ✘ |
| SP-PW-067 | D7 | N7 | A1 | F1 | K1 | T7 | R3 | DI(dni, nombre, telefono) | ✘ |
| SP-PW-068 | D7 | N8 | A2 | F2 | K2 | T8 | R1 | DI(dni, nombre, telefono) | ✘ |
| SP-PW-069 | D7 | N9 | A9 | F2 | K3 | T4 | R1 | DI(dni, nombre, apellidos) | ✘ |
| SP-PW-070 | D7 | N10 | A10 | F1 | K1 | T3 | R4 | DI(dni, nombre, apellidos) | ✘ |
| SP-PW-071 | D8 | N1 | A8 | F3 | K2 | T10 | R5 | DI(dni, apellidos, telefono) | ✘ |
| SP-PW-072 | D8 | N2 | A7 | F4 | K3 | T9 | R2 | DI(dni, apellidos, telefono) | ✘ |
| SP-PW-073 | D8 | N3 | A6 | F1 | K1 | T1 | R4 | DI(dni, apellidos) | ✘ |
| SP-PW-074 | D8 | N4 | A5 | F2 | K2 | T2 | R3 | DI(dni) | ✘ |
| SP-PW-075 | D8 | N5 | A4 | F5 | K3 | T5 | R1 | DI(dni, fechaNacimiento) | ✘ |
| SP-PW-076 | D8 | N6 | A3 | F6 | K1 | T6 | R3 | DI(dni, nombre, fechaNacimiento, telefono) | ✘ |
| SP-PW-077 | D8 | N7 | A2 | F4 | K2 | T7 | R5 | DI(dni, nombre, fechaNacimiento, telefono) | ✘ |
| SP-PW-078 | D8 | N8 | A1 | F3 | K3 | T8 | R2 | DI(dni, nombre, telefono) | ✘ |
| SP-PW-079 | D8 | N9 | A9 | F5 | K1 | T5 | R4 | DI(dni, nombre, apellidos, fechaNacimiento) | ✘ |
| SP-PW-080 | D8 | N10 | A10 | F6 | K2 | T6 | R5 | DI(dni, nombre, apellidos, fechaNacimiento, telefono) | ✘ |
| SP-PW-081 | D9 | N1 | A9 | F3 | K3 | T7 | R1 | DI(dni, apellidos, telefono) | ✘ |
| SP-PW-082 | D9 | N2 | A10 | F4 | K1 | T8 | R2 | DI(dni, apellidos, fechaNacimiento, telefono) | ✘ |
| SP-PW-083 | D9 | N3 | A1 | F5 | K2 | T9 | R5 | DI(dni, fechaNacimiento, telefono) | ✘ |
| SP-PW-084 | D9 | N4 | A2 | F6 | K3 | T10 | R3 | DI(dni, fechaNacimiento, telefono) | ✘ |
| SP-PW-085 | D9 | N5 | A3 | F2 | K1 | T1 | R4 | DI(dni) | ✘ |
| SP-PW-086 | D9 | N6 | A4 | F1 | K2 | T2 | R2 | DI(dni, nombre) | ✘ |
| SP-PW-087 | D9 | N7 | A5 | F3 | K3 | T3 | R1 | DI(dni, nombre) | ✘ |
| SP-PW-088 | D9 | N8 | A6 | F4 | K1 | T4 | R3 | DI(dni, nombre, apellidos, fechaNacimiento) | ✘ |
| SP-PW-089 | D9 | N9 | A7 | F5 | K2 | T6 | R4 | DI(dni, nombre, apellidos, fechaNacimiento, telefono) | ✘ |
| SP-PW-090 | D9 | N10 | A8 | F6 | K3 | T5 | R5 | DI(dni, nombre, apellidos, fechaNacimiento) | ✘ |
| SP-PW-091 | D10 | N1 | A10 | F1 | K1 | T5 | R1 | DI(dni, apellidos) | ✘ |
| SP-PW-092 | D10 | N2 | A9 | F2 | K2 | T6 | R2 | DI(dni, apellidos, telefono) | ✘ |
| SP-PW-093 | D10 | N3 | A1 | F3 | K3 | T10 | R3 | DI(dni, telefono) | ✘ |
| SP-PW-094 | D10 | N4 | A2 | F4 | K1 | T9 | R4 | DI(dni, fechaNacimiento, telefono) | ✘ |
| SP-PW-095 | D10 | N5 | A3 | F5 | K2 | T2 | R5 | DI(dni, fechaNacimiento) | ✘ |
| SP-PW-096 | D10 | N6 | A4 | F6 | K3 | T1 | R1 | DI(dni, nombre, fechaNacimiento) | ✘ |
| SP-PW-097 | D10 | N7 | A5 | F1 | K1 | T4 | R2 | DI(dni, nombre) | ✘ |
| SP-PW-098 | D10 | N8 | A6 | F2 | K2 | T3 | R3 | DI(dni, nombre, apellidos) | ✘ |
| SP-PW-099 | D10 | N9 | A8 | F3 | K3 | T7 | R4 | DI(dni, nombre, apellidos, telefono) | ✘ |
| SP-PW-100 | D10 | N10 | A7 | F4 | K1 | T8 | R5 | DI(dni, nombre, apellidos, fechaNacimiento, telefono) | ✘ |
| SP-PW-101 | D11 | N1 | A1 | F5 | K2 | T8 | R1 | DI(dni, fechaNacimiento, telefono) | ✘ |
| SP-PW-102 | D11 | N2 | A2 | F6 | K3 | T7 | R2 | DI(dni, fechaNacimiento, telefono) | ✘ |
| SP-PW-103 | D11 | N3 | A9 | F1 | K1 | T5 | R3 | DI(dni, apellidos) | ✘ |
| SP-PW-104 | D11 | N4 | A10 | F2 | K2 | T6 | R4 | DI(dni, apellidos, telefono) | ✘ |
| SP-PW-105 | D11 | N5 | A3 | F4 | K3 | T9 | R5 | DI(dni, telefono) | ✘ |
| SP-PW-106 | D11 | N6 | A4 | F3 | K1 | T10 | R1 | DI(dni, nombre, fechaNacimiento, telefono) | ✘ |
| SP-PW-107 | D11 | N7 | A5 | F5 | K2 | T1 | R2 | DI(dni, nombre, fechaNacimiento) | ✘ |
| SP-PW-108 | D11 | N8 | A6 | F6 | K3 | T2 | R3 | DI(dni, nombre, apellidos, fechaNacimiento) | ✘ |
| SP-PW-109 | D11 | N9 | A7 | F1 | K1 | T3 | R4 | DI(dni, nombre, apellidos) | ✘ |
| SP-PW-110 | D11 | N10 | A8 | F2 | K2 | T4 | R5 | DI(dni, nombre, apellidos) | ✘ |
| SP-PW-111 | D12 | N1 | A1 | F3 | K3 | T6 | R1 | DI(dni, telefono) | ✘ |
| SP-PW-112 | D12 | N2 | A2 | F4 | K1 | T5 | R2 | DI(dni, fechaNacimiento) | ✘ |
| SP-PW-113 | D12 | N3 | A10 | F5 | K2 | T7 | R3 | DI(dni, apellidos, fechaNacimiento, telefono) | ✘ |
| SP-PW-114 | D12 | N4 | A9 | F6 | K3 | T8 | R4 | DI(dni, apellidos, fechaNacimiento, telefono) | ✘ |
| SP-PW-115 | D12 | N5 | A3 | F1 | K1 | T10 | R5 | DI(dni, telefono) | ✘ |
| SP-PW-116 | D12 | N6 | A4 | F2 | K2 | T9 | R1 | DI(dni, nombre, telefono) | ✘ |
| SP-PW-117 | D12 | N7 | A5 | F3 | K3 | T2 | R2 | DI(dni, nombre) | ✘ |
| SP-PW-118 | D12 | N8 | A6 | F4 | K1 | T1 | R3 | DI(dni, nombre, apellidos, fechaNacimiento) | ✘ |
| SP-PW-119 | D12 | N9 | A7 | F5 | K2 | T3 | R4 | DI(dni, nombre, apellidos, fechaNacimiento) | ✘ |
| SP-PW-120 | D12 | N10 | A8 | F6 | K3 | T4 | R5 | DI(dni, nombre, apellidos, fechaNacimiento) | ✘ |
| SP-PW-121 | D13 | N1 | A1 | F1 | K1 | T9 | R1 | DI(dni, telefono) | ✘ |
| SP-PW-122 | D13 | N2 | A2 | F2 | K2 | T10 | R2 | DI(dni, telefono) | ✘ |
| SP-PW-123 | D13 | N3 | A3 | F3 | K3 | T8 | R3 | DI(dni, telefono) | ✘ |
| SP-PW-124 | D13 | N4 | A4 | F4 | K1 | T7 | R4 | DI(dni, fechaNacimiento, telefono) | ✘ |
| SP-PW-125 | D13 | N5 | A9 | F5 | K2 | T3 | R5 | DI(dni, apellidos, fechaNacimiento) | ✘ |
| SP-PW-126 | D13 | N6 | A10 | F6 | K3 | T4 | R1 | DI(dni, nombre, apellidos, fechaNacimiento) | ✘ |
| SP-PW-127 | D13 | N7 | A5 | F1 | K1 | T1 | R2 | DI(dni, nombre) | ✘ |
| SP-PW-128 | D13 | N8 | A6 | F2 | K2 | T2 | R3 | DI(dni, nombre, apellidos) | ✘ |
| SP-PW-129 | D13 | N9 | A7 | F3 | K3 | T1 | R4 | DI(dni, nombre, apellidos) | ✘ |
| SP-PW-130 | D13 | N10 | A8 | F4 | K1 | T2 | R5 | DI(dni, nombre, apellidos, fechaNacimiento) | ✘ |
| SP-PW-131 | D14 | N1 | A1 | F5 | K2 | T9 | R1 | DI(dni, fechaNacimiento, telefono) | ✘ |
| SP-PW-132 | D14 | N2 | A2 | F6 | K3 | T10 | R2 | DI(dni, fechaNacimiento, telefono) | ✘ |
| SP-PW-133 | D14 | N3 | A3 | F1 | K1 | T6 | R3 | DI(dni, telefono) | ✘ |
| SP-PW-134 | D14 | N4 | A4 | F2 | K2 | T5 | R4 | DI(dni) | ✘ |
| SP-PW-135 | D14 | N5 | A10 | F3 | K3 | T4 | R5 | DI(dni, apellidos) | ✘ |
| SP-PW-136 | D14 | N6 | A9 | F4 | K1 | T3 | R1 | DI(dni, nombre, apellidos, fechaNacimiento) | ✘ |
| SP-PW-137 | D14 | N7 | A5 | F5 | K2 | T7 | R2 | DI(dni, nombre, fechaNacimiento, telefono) | ✘ |
| SP-PW-138 | D14 | N8 | A6 | F6 | K3 | T8 | R3 | DI(dni, nombre, apellidos, fechaNacimiento, telefono) | ✘ |
| SP-PW-139 | D14 | N9 | A7 | F1 | K1 | T2 | R4 | DI(dni, nombre, apellidos) | ✘ |
| SP-PW-140 | D14 | N10 | A8 | F2 | K2 | T1 | R5 | DI(dni, nombre, apellidos) | ✘ |
| SP-PW-141 | D4 | N7 | A9 | F3 | K3 | T9 | R1 | DI(nombre, apellidos, telefono) | ✘ |
| SP-PW-142 | D4 | N7 | A10 | F4 | K1 | T10 | R2 | DI(nombre, apellidos, fechaNacimiento, telefono) | ✘ |
| SP-PW-143 | D5 | N8 | A9 | F5 | K2 | T9 | R3 | DI(nombre, apellidos, fechaNacimiento, telefono) | ✘ |
| SP-PW-144 | D1 | N8 | A10 | F6 | K3 | T10 | R4 | DI(nombre, apellidos, fechaNacimiento, telefono) | ✘ |
| SP-PW-145 | D3 | N9 | A1 | F1 | K1 | T8 | R5 | DI(nombre, telefono) | ✘ |
| SP-PW-146 | D8 | N9 | A2 | F2 | K2 | T3 | R1 | DI(dni, nombre) | ✘ |
| SP-PW-147 | D8 | N9 | A3 | F3 | K3 | T4 | R2 | DI(dni, nombre) | ✘ |
| SP-PW-148 | D13 | N9 | A4 | F4 | K1 | T5 | R3 | DI(dni, nombre, fechaNacimiento) | ✘ |
| SP-PW-149 | D13 | N9 | A5 | F5 | K2 | T6 | R4 | DI(dni, nombre, fechaNacimiento, telefono) | ✘ |
| SP-PW-150 | D1 | N9 | A6 | F6 | K3 | T7 | R5 | DI(nombre, apellidos, fechaNacimiento, telefono) | ✘ |
| SP-PW-151 | D1 | N10 | A1 | F1 | K1 | T7 | R1 | DI(nombre, telefono) | ✘ |
| SP-PW-152 | D6 | N10 | A2 | F2 | K2 | T1 | R2 | DI(dni, nombre) | ✘ |
| SP-PW-153 | D6 | N10 | A3 | F3 | K3 | T2 | R3 | DI(dni, nombre) | ✘ |
| SP-PW-154 | D1 | N10 | A4 | F4 | K1 | T3 | R4 | DI(nombre, fechaNacimiento) | ✘ |
| SP-PW-155 | D1 | N10 | A5 | F5 | K2 | T4 | R5 | DI(nombre, fechaNacimiento) | ✘ |
| SP-PW-156 | D1 | N10 | A6 | F6 | K3 | T5 | R1 | DI(nombre, apellidos, fechaNacimiento) ⚠ | ✘ |
| SP-PW-157 | D1 | N5 | A1 | F1 | K1 | T1 | R3 | ISE | ✘ |

### 7.2 VA

Se ha construido con el mismo algoritmo IPO, partiendo de W × dni (20 × 4 = 80 casos, el mínimo posible). Los **416 pares** quedan cubiertos con **80 casos**. Objetivo común: cubrir los pares. Precondición y postcondición: las del apartado 6.2. **Test actual:** ninguno cubre estos casos; «= ID» señala los que repiten un caso de otro apartado.

| Id | fecha | dni | nombre | apellidos | teléfono | Resultado esperado | Test actual |
|---|---|---|---|---|---|---|---|
| VA-PW-01 | W1 | X1 | X1 | X1 | X1 | DATOS con fecha nula | ✘ |
| VA-PW-02 | W1 | X2 | X2 | X2 | X2 | DATOS con fecha nula | ✘ |
| VA-PW-03 | W1 | X3 | X3 | X3 | X3 | DATOS con fecha nula | ✘ |
| VA-PW-04 | W1 | X4 | X4 | X4 | X4 | DATOS con fecha nula | ✘ |
| VA-PW-05 | W2 | X1 | X2 | X3 | X4 | DATOS con fecha nula | ✘ |
| VA-PW-06 | W2 | X2 | X1 | X4 | X3 | DATOS con fecha nula | ✘ |
| VA-PW-07 | W2 | X3 | X4 | X1 | X2 | DATOS con fecha nula | ✘ |
| VA-PW-08 | W2 | X4 | X3 | X2 | X1 | DATOS con fecha nula | ✘ |
| VA-PW-09 | W3 | X1 | X3 | X4 | X2 | DATOS | ✘ |
| VA-PW-10 | W3 | X2 | X4 | X3 | X1 | DATOS | ✘ |
| VA-PW-11 | W3 | X3 | X1 | X2 | X4 | DATOS | ✘ |
| VA-PW-12 | W3 | X4 | X2 | X1 | X3 | DATOS | ✘ |
| VA-PW-13 | W4 | X1 | X4 | X2 | X3 | DATOS | ✘ |
| VA-PW-14 | W4 | X2 | X3 | X1 | X4 | DATOS | ✘ |
| VA-PW-15 | W4 | X3 | X2 | X4 | X1 | DATOS | ✘ |
| VA-PW-16 | W4 | X4 | X1 | X3 | X2 | DATOS | ✘ |
| VA-PW-17 | W5 | X1 | X1 | X1 | X1 | NULO | ✘ |
| VA-PW-18 | W5 | X2 | X2 | X2 | X2 | NULO | = VA-EU-05 |
| VA-PW-19 | W5 | X3 | X3 | X3 | X3 | NULO | ✘ |
| VA-PW-20 | W5 | X4 | X4 | X4 | X4 | NULO | ✘ |
| VA-PW-21 | W6 | X1 | X1 | X1 | X1 | DATOS | = VA-EU-06 |
| VA-PW-22 | W6 | X2 | X2 | X2 | X2 | DATOS | ✘ |
| VA-PW-23 | W6 | X3 | X3 | X3 | X3 | DATOS | ✘ |
| VA-PW-24 | W6 | X4 | X4 | X4 | X4 | DATOS | ✘ |
| VA-PW-25 | W7 | X1 | X1 | X1 | X1 | NULO | = VA-EU-07 |
| VA-PW-26 | W7 | X2 | X2 | X2 | X2 | NULO | ✘ |
| VA-PW-27 | W7 | X3 | X3 | X3 | X3 | NULO | ✘ |
| VA-PW-28 | W7 | X4 | X4 | X4 | X4 | NULO | ✘ |
| VA-PW-29 | W8 | X1 | X1 | X1 | X1 | DATOS | = VA-EU-08 |
| VA-PW-30 | W8 | X2 | X2 | X2 | X2 | DATOS | ✘ |
| VA-PW-31 | W8 | X3 | X3 | X3 | X3 | DATOS | ✘ |
| VA-PW-32 | W8 | X4 | X4 | X4 | X4 | DATOS | ✘ |
| VA-PW-33 | W9 | X1 | X1 | X1 | X1 | DATOS | = VA-EU-09 |
| VA-PW-34 | W9 | X2 | X2 | X2 | X2 | DATOS | ✘ |
| VA-PW-35 | W9 | X3 | X3 | X3 | X3 | DATOS | ✘ |
| VA-PW-36 | W9 | X4 | X4 | X4 | X4 | DATOS | ✘ |
| VA-PW-37 | W10 | X1 | X1 | X1 | X1 | NULO | = VA-EU-10 |
| VA-PW-38 | W10 | X2 | X2 | X2 | X2 | NULO | ✘ |
| VA-PW-39 | W10 | X3 | X3 | X3 | X3 | NULO | ✘ |
| VA-PW-40 | W10 | X4 | X4 | X4 | X4 | NULO | ✘ |
| VA-PW-41 | W11 | X1 | X1 | X1 | X1 | DATOS | = VA-EU-11 |
| VA-PW-42 | W11 | X2 | X2 | X2 | X2 | DATOS | ✘ |
| VA-PW-43 | W11 | X3 | X3 | X3 | X3 | DATOS | ✘ |
| VA-PW-44 | W11 | X4 | X4 | X4 | X4 | DATOS | ✘ |
| VA-PW-45 | W12 | X1 | X1 | X1 | X1 | DATOS | = VA-EU-12 |
| VA-PW-46 | W12 | X2 | X2 | X2 | X2 | DATOS | ✘ |
| VA-PW-47 | W12 | X3 | X3 | X3 | X3 | DATOS | ✘ |
| VA-PW-48 | W12 | X4 | X4 | X4 | X4 | DATOS | ✘ |
| VA-PW-49 | W13 | X1 | X1 | X1 | X1 | NULO | = VA-EU-13 |
| VA-PW-50 | W13 | X2 | X2 | X2 | X2 | NULO | ✘ |
| VA-PW-51 | W13 | X3 | X3 | X3 | X3 | NULO | ✘ |
| VA-PW-52 | W13 | X4 | X4 | X4 | X4 | NULO | ✘ |
| VA-PW-53 | W14 | X1 | X1 | X1 | X1 | NULO | = VA-EU-14 |
| VA-PW-54 | W14 | X2 | X2 | X2 | X2 | NULO | ✘ |
| VA-PW-55 | W14 | X3 | X3 | X3 | X3 | NULO | ✘ |
| VA-PW-56 | W14 | X4 | X4 | X4 | X4 | NULO | ✘ |
| VA-PW-57 | W15 | X1 | X1 | X1 | X1 | DATOS | = VA-EU-15 |
| VA-PW-58 | W15 | X2 | X2 | X2 | X2 | DATOS | ✘ |
| VA-PW-59 | W15 | X3 | X3 | X3 | X3 | DATOS | ✘ |
| VA-PW-60 | W15 | X4 | X4 | X4 | X4 | DATOS | ✘ |
| VA-PW-61 | W16 | X1 | X1 | X1 | X1 | DATOS | = VA-EU-16 |
| VA-PW-62 | W16 | X2 | X2 | X2 | X2 | DATOS | ✘ |
| VA-PW-63 | W16 | X3 | X3 | X3 | X3 | DATOS | ✘ |
| VA-PW-64 | W16 | X4 | X4 | X4 | X4 | DATOS | ✘ |
| VA-PW-65 | W17 | X1 | X1 | X1 | X1 | NULO ⚠ | = VA-EU-17 |
| VA-PW-66 | W17 | X2 | X2 | X2 | X2 | NULO ⚠ | ✘ |
| VA-PW-67 | W17 | X3 | X3 | X3 | X3 | NULO ⚠ | ✘ |
| VA-PW-68 | W17 | X4 | X4 | X4 | X4 | NULO ⚠ | ✘ |
| VA-PW-69 | W18 | X1 | X1 | X1 | X1 | DATOS | = VA-EU-18 |
| VA-PW-70 | W18 | X2 | X2 | X2 | X2 | DATOS | ✘ |
| VA-PW-71 | W18 | X3 | X3 | X3 | X3 | DATOS | ✘ |
| VA-PW-72 | W18 | X4 | X4 | X4 | X4 | DATOS | ✘ |
| VA-PW-73 | W19 | X1 | X1 | X1 | X1 | NULO ⚠ | = VA-EU-19 |
| VA-PW-74 | W19 | X2 | X2 | X2 | X2 | NULO ⚠ | ✘ |
| VA-PW-75 | W19 | X3 | X3 | X3 | X3 | NULO ⚠ | ✘ |
| VA-PW-76 | W19 | X4 | X4 | X4 | X4 | NULO ⚠ | ✘ |
| VA-PW-77 | W20 | X1 | X1 | X1 | X1 | NULO | = VA-EU-20 |
| VA-PW-78 | W20 | X2 | X2 | X2 | X2 | NULO | ✘ |
| VA-PW-79 | W20 | X3 | X3 | X3 | X3 | NULO | ✘ |
| VA-PW-80 | W20 | X4 | X4 | X4 | X4 | NULO | ✘ |

### 7.3 Comprobación con PICT

En esta carpeta hay un modelo por unidad, `CdU22-SP.pict.txt` y `CdU22-VA.pict.txt`, porque SP y VA tienen variables distintas y un fichero de PICT describe un solo modelo. Con cada uno va una semilla con los casos de las tablas anteriores: `CdU22-SP.semilla.txt` y `CdU22-VA.semilla.txt`.

- `pict CdU22-SP.pict.txt` genera un conjunto propio de PICT, para comparar el número de casos.
- `pict CdU22-SP.pict.txt /e:CdU22-SP.semilla.txt` parte de los 157 casos de la tabla. Si ya cubren todos los pares, PICT no tiene que añadir ninguno.

Con VA se hace igual (80 casos). PICT puede dar otro número de casos, porque usa otro algoritmo; lo que hay que comparar es la cobertura de los pares. Los modelos no usan `~`, porque aquí también cuentan los pares de dos valores inválidos.

### 7.4 Unidades con dos variables

**SC · constructor de `ServicioPacientesImpl`**

| Id | Objetivo | repositorio | reloj | Resultado esperado | Post. | Test actual |
|---|---|---|---|---|---|---|
| SC-PW-01 | Creación correcta | objeto | objeto | objeto creado que usa ambos | — | ✔ `ServicioPacientesImplTest.daDeAltaPacienteConDatosValidos` (repositorio) y `.rechazaFechaNacimientoFutura` (reloj) |
| SC-PW-02 | Repositorio obligatorio | null | objeto | `NullPointerException` | no se crea | ✘ |
| SC-PW-03 | Reloj obligatorio | objeto | null | `NullPointerException` | no se crea | ✘ |
| SC-PW-04 | Ambos obligatorios | null | null | `NullPointerException` | no se crea | ✘ |

El constructor de un parámetro delega en este con el reloj del sistema; lo ejecuta `ControladorPacientesTest.crearControlador`.

**CP · `ControladorPacientes.darDeAlta`**

Precondición: servicio falso que responde según S. Postcondición: el servicio recibe los datos una vez y sin cambios.

| Id | Objetivo | J | S | Resultado esperado | Test actual |
|---|---|---|---|---|---|
| CP-PW-01 | Alta correcta | J1 | S1 | `(true, "Paciente Alex Ejemplo (DNI 12345678Z) dado de alta.")` | ◐ `ControladorPacientesTest.altaCorrectaDevuelveResultadoCorrecto` (solo comprueba que el mensaje contiene el DNI) |
| CP-PW-02 | Datos rechazados | J1 | S2 | `(false, "El campo nombre es obligatorio.")` | ✔ `ControladorPacientesTest.datosInvalidosDevuelvenErrorConMensaje` |
| CP-PW-03 | Duplicado | J1 | S3 | `(false, "Ya existe un paciente con el DNI 12345678Z.")` | ◐ `ControladorPacientesTest.dniDuplicadoDevuelveError` (solo comprueba que el mensaje contiene el DNI) |
| CP-PW-04 | Fallo de persistencia sin detalles internos | J1 | S4 | `(false, ERROR_INTERNO)` | ✔ `ControladorPacientesTest.falloDePersistenciaDevuelveErrorGenerico` |
| CP-PW-05 | Fallo no previsto | J1 | S5 | `NullPointerException` | ✘ |
| CP-PW-06 | Datos null y respuesta de alta | J2 | S1 | como CP-PW-01 | ✘ |
| CP-PW-07 | Datos null rechazados | J2 | S2 | como CP-PW-02 | ◐ `ControladorPacientesTest.datosNulosDevuelvenError` (no comprueba el mensaje) |
| CP-PW-08 | Datos null y duplicado | J2 | S3 | como CP-PW-03 | ✘ |
| CP-PW-09 | Datos null y fallo de persistencia | J2 | S4 | como CP-PW-04 | ✘ |
| CP-PW-10 | Datos null y fallo no previsto | J2 | S5 | `NullPointerException` | ✘ |

Los tests actuales usan el servicio real en vez de un doble; se dan por cubiertos cuando el resultado comprobado es el mismo.

**RG · `PacienteRepositorioMySql.guardar`**

Precondición: JDBC falso que responde según B. Postcondiciones: **PC3**, se ejecuta el INSERT con los cinco parámetros y se cierra la conexión; **PC4**, no se inserta nada y, si la conexión se abrió, se cierra.

| Id | Objetivo | P | B | Resultado esperado | Post. | Test actual |
|---|---|---|---|---|---|---|
| RG-PW-01 | Inserción | P1 | B1 | termina sin excepción | PC3 | ✔ `PacienteRepositorioMySqlTest.guardarInsertaTodosLosCampos` |
| RG-PW-02 | El DNI ya existe | P1 | B2 | `PacienteDuplicadoException` con el DNI | PC4 | ◐ `PacienteRepositorioMySqlTest.guardarConDniDuplicadoEnLaBaseDeDatosLanzaPacienteDuplicado` (falta PC4) |
| RG-PW-03 | Código 1061 | P1 | B3 | `IllegalStateException` con la causa | PC4 | ✘ |
| RG-PW-04 | Código 1063 | P1 | B4 | `IllegalStateException` con la causa | PC4 | ✘ |
| RG-PW-05 | Otro código | P1 | B5 | `IllegalStateException` con la causa | PC4 | ◐ `PacienteRepositorioMySqlTest.otroErrorAlGuardarLanzaIllegalState` (falta PC4) |
| RG-PW-06 | Sin conexión | P1 | B6 | `IllegalStateException` con la causa | PC4 | ✘ |
| RG-PW-07 | Inserción sin teléfono | P2 | B1 | termina sin excepción; teléfono null | PC3 | ✘ |
| RG-PW-08 | Sin teléfono, el DNI ya existe | P2 | B2 | `PacienteDuplicadoException` con el DNI | PC4 | ✘ |
| RG-PW-09 | Sin teléfono, 1061 | P2 | B3 | `IllegalStateException` con la causa | PC4 | ✘ |
| RG-PW-10 | Sin teléfono, 1063 | P2 | B4 | `IllegalStateException` con la causa | PC4 | ✘ |
| RG-PW-11 | Sin teléfono, otro código | P2 | B5 | `IllegalStateException` con la causa | PC4 | ✘ |
| RG-PW-12 | Sin teléfono, sin conexión | P2 | B6 | `IllegalStateException` con la causa | PC4 | ✘ |
| RG-PW-13 | Paciente null | P3 | B1 | `NullPointerException` | PC4 | ◐ `PacienteRepositorioMySqlTest.guardarNuloLanzaExcepcion` (falta PC4) |
| RG-PW-14 | Paciente null, 1062 | P3 | B2 | `NullPointerException` | PC4 | ✘ |
| RG-PW-15 | Paciente null, 1061 | P3 | B3 | `NullPointerException` | PC4 | ✘ |
| RG-PW-16 | Paciente null, 1063 | P3 | B4 | `NullPointerException` | PC4 | ✘ |
| RG-PW-17 | Paciente null, otro código | P3 | B5 | `NullPointerException` | PC4 | ✘ |
| RG-PW-18 | Paciente null, sin conexión | P3 | B6 | `NullPointerException` | PC4 | ✘ |
| RG-PW-19 | Sin fecha | P4 | B1 | `NullPointerException` | PC4 | ✘ |
| RG-PW-20 | Sin fecha, 1062 | P4 | B2 | `NullPointerException` | PC4 | ✘ |
| RG-PW-21 | Sin fecha, 1061 | P4 | B3 | `NullPointerException` | PC4 | ✘ |
| RG-PW-22 | Sin fecha, 1063 | P4 | B4 | `NullPointerException` | PC4 | ✘ |
| RG-PW-23 | Sin fecha, otro código | P4 | B5 | `NullPointerException` | PC4 | ✘ |
| RG-PW-24 | Sin fecha, sin conexión | P4 | B6 | `IllegalStateException` con la causa | PC4 | ✘ |

**RB · `PacienteRepositorioMySql.buscarPorDni`**

Precondición: JDBC falso que responde según G. Postcondición: se consulta con ese DNI y se cierra la conexión.

| Id | Objetivo | Q | G | Resultado esperado | Test actual |
|---|---|---|---|---|---|
| RB-PW-01 | Hay fila | Q1 | G1 | paciente P1 | ◐ `PacienteRepositorioMySqlTest.buscarPorDniDevuelveElPacienteDeLaFila` (no comprueba el cierre) |
| RB-PW-02 | Fila sin teléfono | Q1 | G2 | paciente P2 | ✘ |
| RB-PW-03 | Sin filas | Q1 | G3 | vacío | ◐ `PacienteRepositorioMySqlTest.buscarPorDniSinFilasDevuelveVacio` (no comprueba la consulta ni el cierre) |
| RB-PW-04 | Error | Q1 | G4 | `IllegalStateException` con la causa | ◐ `PacienteRepositorioMySqlTest.errorAlBuscarLanzaIllegalState` (no comprueba la causa ni el cierre) |
| RB-PW-05 | DNI vacío | Q2 | G3 | vacío | ✘ |
| RB-PW-06 | DNI vacío y error | Q2 | G4 | `IllegalStateException` con la causa | ✘ |
| RB-PW-07 | DNI null | Q3 | G3 | vacío | ✘ |
| RB-PW-08 | DNI null y error | Q3 | G4 | `IllegalStateException` con la causa | ✘ |

Los pares de Q2 y Q3 con G1 y G2 son imposibles.

**MG y MB · `PacienteRepositorioEnMemoria`**

| Id | Objetivo | Entrada | Estado | Resultado esperado | Post. | Test actual |
|---|---|---|---|---|---|---|
| MG-PW-01 | Guardar nuevo | M1 | E1 | termina sin excepción | el mapa contiene a M1 | ✔ `PacienteRepositorioEnMemoriaTest.devuelveElPacienteGuardado` |
| MG-PW-02 | Guardar con el DNI ya presente | M1 | E2 | `PacienteDuplicadoException` ⚠ PF-6 | sigue Alex Ejemplo | ✘ |
| MG-PW-03 | Guardar null | M2 | E1 | `NullPointerException` | el mapa no cambia | ✘ |
| MG-PW-04 | Guardar null con el mapa ocupado | M2 | E2 | `NullPointerException` | el mapa no cambia | ✘ |
| MB-PW-01 | Buscar en vacío | Z1 | E1 | vacío | sin cambios | ✘ |
| MB-PW-02 | Buscar el que está | Z1 | E2 | Alex Ejemplo | sin cambios | ✔ `PacienteRepositorioEnMemoriaTest.devuelveElPacienteGuardado` |
| MB-PW-03 | Buscar otro en vacío | Z2 | E1 | vacío | sin cambios | ✔ `PacienteRepositorioEnMemoriaTest.dniDesconocidoDevuelveVacio` |
| MB-PW-04 | Buscar otro | Z2 | E2 | vacío | sin cambios | ✘ |
| MB-PW-05 | Buscar null en vacío | Z3 | E1 | vacío | sin cambios | ✘ |
| MB-PW-06 | Buscar null | Z3 | E2 | vacío | sin cambios | ✘ |

**CA · `ControladorAltaPacienteImpl.darDeAlta`**

Precondición: vista y cliente falsos que responden según V y C.

| Id | Objetivo | V | C | Resultado esperado | Post. | Test actual |
|---|---|---|---|---|---|---|
| CA-PW-01 | Alta correcta | V1 | C1 | muestra éxito con el mensaje | formulario limpio | ✔ `ControladorAltaPacienteImplTest.altaCorrectaMuestraExitoYLimpiaElFormulario` |
| CA-PW-02 | Alta rechazada | V1 | C2 | muestra el error del servidor | formulario intacto | ✔ `ControladorAltaPacienteImplTest.altaRechazadaMuestraElMensajeDelServidorYConservaLosDatos` |
| CA-PW-03 | Sin conexión | V1 | C3 | muestra `MENSAJE_SIN_CONEXION` | formulario intacto | ✔ `ControladorAltaPacienteImplTest.sinConexionMuestraError` |
| CA-PW-04 | Respuesta nula | V1 | C4 | muestra un error, sin excepción ⚠ PF-5 | formulario intacto | ✘ |
| CA-PW-05 | Fallo no previsto | V1 | C5 | `IllegalArgumentException` | no muestra éxito; formulario intacto | ✘ |
| CA-PW-06 | Fecha mal escrita | V2 | C1 | muestra `MENSAJE_FECHA_INCORRECTA` | no llama al cliente | ✘ |
| CA-PW-07 | Fecha mal escrita | V2 | C2 | muestra `MENSAJE_FECHA_INCORRECTA` | no llama al cliente | ✘ |
| CA-PW-08 | Fecha mal escrita | V2 | C3 | muestra `MENSAJE_FECHA_INCORRECTA` | no llama al cliente | ✘ |
| CA-PW-09 | Fecha mal escrita | V2 | C4 | muestra `MENSAJE_FECHA_INCORRECTA` | no llama al cliente | ✔ `ControladorAltaPacienteImplTest.fechaMalEscritaNoLlamaAlServidor` |
| CA-PW-10 | Fecha mal escrita | V2 | C5 | muestra `MENSAJE_FECHA_INCORRECTA` | no llama al cliente | ✘ |

**CC · constructor de `ControladorAltaPacienteImpl`**

| Id | Objetivo | vista | cliente | Resultado esperado | Post. | Test actual |
|---|---|---|---|---|---|---|
| CC-PW-01 | Creación correcta | objeto | objeto | objeto creado que usa ambos | — | ✔ `ControladorAltaPacienteImplTest.altaCorrectaMuestraExitoYLimpiaElFormulario` |
| CC-PW-02 | Vista obligatoria | null | objeto | `NullPointerException` | no se crea | ✘ |
| CC-PW-03 | Cliente obligatorio | objeto | null | `NullPointerException` | no se crea | ✘ |
| CC-PW-04 | Ambos obligatorios | null | null | `NullPointerException` | no se crea | ✘ |

**CH · `ClientePacientesHttp.darDeAlta`**

Precondición: URL U1 y `HttpClient` falso que responde según H. `EC`: `ErrorComunicacionException`. Postcondición: se envía un solo POST a `/pacientes` con los datos en JSON (con Y2, `null`).

| Id | Objetivo | Y | H | Resultado esperado | Test actual |
|---|---|---|---|---|---|
| CH-PW-01 | Alta correcta | Y1 | H1 | `(true, "Paciente dado de alta.")` | ✔ `ClientePacientesHttpTest.enviaLosDatosEnJsonYDevuelveElAltaCorrecta` |
| CH-PW-02 | Rechazo | Y1 | H2 | `(false, "La letra del campo dni no es correcta.")` | ✔ `ClientePacientesHttpTest.devuelveElRechazoDelServidor` |
| CH-PW-03 | No es JSON | Y1 | H3 | EC | ✔ `ClientePacientesHttpTest.respuestaQueNoEsUnResultadoEsErrorDeComunicacion` |
| CH-PW-04 | Cuerpo vacío | Y1 | H4 | EC | ✘ |
| CH-PW-05 | JSON nulo | Y1 | H5 | EC ⚠ PF-5 | ✘ |
| CH-PW-06 | Otro JSON | Y1 | H6 | EC | ✘ |
| CH-PW-07 | Fallo de red | Y1 | H7 | EC | ✔ `ClientePacientesHttpTest.servidorParadoEsErrorDeComunicacion` |
| CH-PW-08 | Interrupción | Y1 | H8 | EC, y el hilo queda marcado como interrumpido | ✘ |
| CH-PW-09 | Datos null, alta | Y2 | H1 | `(true, "Paciente dado de alta.")` | ✘ |
| CH-PW-10 | Datos null, rechazo | Y2 | H2 | `(false, "La letra del campo dni no es correcta.")` | ✘ |
| CH-PW-11 | Datos null, no es JSON | Y2 | H3 | EC | ✘ |
| CH-PW-12 | Datos null, cuerpo vacío | Y2 | H4 | EC | ✘ |
| CH-PW-13 | Datos null, JSON nulo | Y2 | H5 | EC ⚠ PF-5 | ✘ |
| CH-PW-14 | Datos null, otro JSON | Y2 | H6 | EC | ✘ |
| CH-PW-15 | Datos null, fallo de red | Y2 | H7 | EC | ✘ |
| CH-PW-16 | Datos null, interrupción | Y2 | H8 | EC, hilo interrumpido | ✘ |
| CH-PW-17 | Campos nulos, alta | Y3 | H1 | `(true, "Paciente dado de alta.")` | ✘ |
| CH-PW-18 | Campos nulos, rechazo | Y3 | H2 | `(false, "La letra del campo dni no es correcta.")` | ✘ |
| CH-PW-19 | Campos nulos, no es JSON | Y3 | H3 | EC | ✘ |
| CH-PW-20 | Campos nulos, cuerpo vacío | Y3 | H4 | EC | ✘ |
| CH-PW-21 | Campos nulos, JSON nulo | Y3 | H5 | EC ⚠ PF-5 | ✘ |
| CH-PW-22 | Campos nulos, otro JSON | Y3 | H6 | EC | ✘ |
| CH-PW-23 | Campos nulos, fallo de red | Y3 | H7 | EC | ✘ |
| CH-PW-24 | Campos nulos, interrupción | Y3 | H8 | EC, hilo interrumpido | ✘ |

Los tests actuales usan un servidor HTTP local en vez de un `HttpClient` falso; se dan por cubiertos porque el resultado comprobado es el mismo.

**CU · constructor de `ClientePacientesHttp`**

Oráculo de la creación: al enviar, la petición va a `http://localhost:7070/pacientes`. En U6 solo se comprueba que el objeto se crea.

| Id | Objetivo | U | http | Resultado esperado | Post. | Test actual |
|---|---|---|---|---|---|---|
| CU-PW-01 | URL sin barra | U1 | objeto | objeto creado | URL correcta | ✘ |
| CU-PW-02 | URL con barra | U2 | objeto | objeto creado | URL correcta | ✔ `ClientePacientesHttpTest.enviaLosDatosEnJsonYDevuelveElAltaCorrecta` |
| CU-PW-03 | URL con varias barras | U3 | objeto | objeto creado | URL correcta | ✘ |
| CU-PW-04 | URL null | U4 | objeto | `NullPointerException` | no se crea | ✘ |
| CU-PW-05 | URL que no es una URI | U5 | objeto | `IllegalArgumentException` | no se crea | ✘ |
| CU-PW-06 | URL sin esquema | U6 | objeto | objeto creado | — | ✘ |
| CU-PW-07 | http null, sin barra | U1 | null | `NullPointerException` | no se crea | ✘ |
| CU-PW-08 | http null, con barra | U2 | null | `NullPointerException` | no se crea | ✘ |
| CU-PW-09 | http null, varias barras | U3 | null | `NullPointerException` | no se crea | ✘ |
| CU-PW-10 | Ambos null | U4 | null | `NullPointerException` | no se crea | ✘ |
| CU-PW-11 | http null, no es una URI | U5 | null | `IllegalArgumentException` | no se crea | ✘ |
| CU-PW-12 | http null, sin esquema | U6 | null | `NullPointerException` | no se crea | ✘ |
| CU-PF-01 | Enviar con una URL sin esquema | U6, datos Y1 | objeto | `ErrorComunicacionException` ⚠ PF-7 | no se envía nada | ✘ |

El constructor de un parámetro delega en este; lo ejecuta `ClientePacientesHttpTest.cliente`.

---

## 8. Cobertura de decisiones

Cada decisión da true y false al menos una vez. Los valores de esta tabla son los del apartado 1.

### 8.1 SP (d1 a d9)

| Id | Objetivo | Entradas y precondiciones | Resultado esperado | Post. | Test actual |
|---|---|---|---|---|---|
| SP-DC-01 | Todas a false | D1 N1 A1 F1 K2 T1 R1 | OK | PC1 | = SP-EU-01 (✔) |
| SP-DC-02 | d1 true | `datos` null | `DatosInvalidosException` | PC2 | = SP-EU-38 (✔) |
| SP-DC-03 | d4 true | D1 N7 A1 F1 K2 T2 R1 | DI(nombre) | PC2 | = SP-EU-16 |
| SP-DC-04 | d2 true | D1 N1 A1 F5 K2 T2 R1 | DI(fechaNacimiento) | PC2 | = SP-EU-25 |
| SP-DC-05 | d5 true | D9 N1 A1 F1 K2 T2 R1 | DI(dni) | PC2 | = SP-EU-09 |
| SP-DC-06 | d6 true | D13 N1 A1 F1 K2 T2 R1 | DI(dni) | PC2 | = SP-EU-13 |
| SP-DC-07 | d7 true | D1 N1 A1 F4 K2 T2 R1 | DI(fechaNacimiento) | PC2 | = SP-EU-27 |
| SP-DC-08 | d9 true | D1 N1 A1 F1 K2 T8 R1 | DI(telefono) | PC2 | = SP-EU-30 |
| SP-DC-09 | d8 y d3 true | D1 N1 A1 F1 K2 T2 R2 | DUP | PC2 | ✘ |

| Decisión | true | false |
|---|---|---|
| d1 `datos == null` | SP-DC-02 | SP-DC-01 |
| d2 `fechaNacimiento == null` | SP-DC-04 | SP-DC-01 |
| d3 el DNI ya existe | SP-DC-09 | SP-DC-01 |
| d4 `valor == null \|\| valor.isBlank()` | SP-DC-03 | SP-DC-01 |
| d5 formato del DNI incorrecto | SP-DC-05 | SP-DC-01 |
| d6 letra incorrecta | SP-DC-06 | SP-DC-01 |
| d7 fecha posterior a hoy | SP-DC-07 | SP-DC-01 |
| d8 `telefono == null \|\| telefono.isBlank()` | SP-DC-09 | SP-DC-01 |
| d9 formato del teléfono incorrecto | SP-DC-08 | SP-DC-01 |

Nueve casos es el mínimo: seis decisiones que acaban en excepción (d1, d2, d4, d5, d6 y d7) necesitan un caso cada una, d9 otro, d3 otro (que puede llevar también d8) y hace falta uno que las deje todas a false.

### 8.2 Resto de unidades

| Id | Objetivo | Entradas y precondiciones | Resultado esperado | Test actual |
|---|---|---|---|---|
| RG-DC-01 | d1 «código es 1062» true | P1 B2 | `PacienteDuplicadoException` | = RG-PW-02 |
| RG-DC-02 | d1 false | P1 B5 | `IllegalStateException` | = RG-PW-05 |
| RB-DC-01 | d2 «no hay fila» true | Q1 G3 | vacío | = RB-PW-03 |
| RB-DC-02 | d2 false | Q1 G1 | paciente P1 | = RB-PW-01 |
| CA-DC-01 | d1 «datos null» true | V2 C4 | error de fecha | = CA-PW-09 (✔) |
| CA-DC-02 | d1 false y d2 «correcto» true | V1 C1 | éxito | = CA-PW-01 (✔) |
| CA-DC-03 | d1 false y d2 false | V1 C2 | error del servidor | = CA-PW-02 (✔) |
| VA-DC-01 | d1 «fecha vacía» true | W1 X1 X2 X2 X2 | DATOS con fecha nula | = VA-EU-01 |
| VA-DC-02 | d1 false | W3 X3 X3 X1 X1 | DATOS | = VA-EU-03 (✔) |
| CT-DC-01 | d2 «fecha null» true | null | cadena vacía | = CT-EU-01 |
| CT-DC-02 | d2 false | 12/05/1990 | `12/05/1990` | = CT-EU-02 |
| CF-DC-01 | d3 true | null | null | = CF-EU-01 |
| CF-DC-02 | d3 false | `12/05/1990` | 12/05/1990 | = CF-EU-04 |

Las precondiciones y postcondiciones son las del caso al que remite cada fila. CP, MG, MB, CH, VL, VB y los constructores no tienen `if`. Sus caminos de excepción se cubren como salidas en el apartado 4.

---

## 9. MC/DC

Hay tres decisiones con más de una condición, todas de la forma `A || B`. Para dos condiciones bastan 3 casos (número de condiciones + 1). `A` es «es null» y `B` es «está en blanco».

Java evalúa en cortocircuito: si A es true, B no se evalúa (`isBlank()` sobre null lanzaría una excepción). Por eso la combinación A = true con B = true no existe, y en la fila con A = true se pone «–» en B.

### 9.1 SP · d4, `valor == null || valor.isBlank()` (con el nombre)

| Fila | A | B | A o B | Condición dominante | Caso |
|---|---|---|---|---|---|
| 1 | true | – | true | A | SP-MC-01 |
| 2 | false | true | true | B | SP-MC-02 |
| 3 | false | false | false | A y B | SP-MC-03 |

A se demuestra comparando las filas 1 y 3; B, comparando las filas 2 y 3.

| Id | Objetivo | Entradas y precondiciones | Resultado esperado | Post. | Test actual |
|---|---|---|---|---|---|
| SP-MC-01 | Domina A | D1 N7 A1 F1 K2 T2 R1 | DI(nombre) | PC2 | = SP-EU-16 |
| SP-MC-02 | Domina B | D1 N9 A1 F1 K2 T2 R1 | DI(nombre) | PC2 | = SP-EU-18 |
| SP-MC-03 | Dominan A y B | D1 N1 A1 F1 K2 T1 R1 | OK | PC1 | = SP-EU-01 (✔) |

### 9.2 SP · d8, `telefono == null || telefono.isBlank()`

| Fila | A | B | A o B | Condición dominante | Caso |
|---|---|---|---|---|---|
| 1 | true | – | true | A | SP-MC-04 |
| 2 | false | true | true | B | SP-MC-05 |
| 3 | false | false | false | A y B | SP-MC-06 |

| Id | Objetivo | Entradas y precondiciones | Resultado esperado | Post. | Test actual |
|---|---|---|---|---|---|
| SP-MC-04 | Domina A: teléfono devuelto null | D1 N1 A1 F1 K2 T2 R1 | OK | PC1 | ✘ |
| SP-MC-05 | Domina B: teléfono devuelto null | D1 N1 A1 F1 K2 T4 R1 | OK | PC1 | ◐ `ServicioPacientesImplTest.permiteTelefonoVacioYLoGuardaComoNulo` (falta PC1 y los demás campos) |
| SP-MC-06 | Dominan A y B: teléfono devuelto `600000001` | D1 N1 A1 F1 K2 T1 R1 | OK | PC1 | = SP-EU-01 (✔) |

### 9.3 CF · d3, `texto == null || texto.isBlank()`

| Fila | A | B | A o B | Condición dominante | Caso |
|---|---|---|---|---|---|
| 1 | true | – | true | A | CF-MC-01 |
| 2 | false | true | true | B | CF-MC-02 |
| 3 | false | false | false | A y B | CF-MC-03 |

| Id | Objetivo | Entradas y precondiciones | Resultado esperado | Test actual |
|---|---|---|---|---|
| CF-MC-01 | Domina A | null | null | = CF-EU-01 |
| CF-MC-02 | Domina B | tres espacios | null | = CF-EU-03 |
| CF-MC-03 | Dominan A y B | `12/05/1990` | 12/05/1990 | = CF-EU-04 |

Queda fuera `valorEntornoObligatorio` (apartado 2).

---

## 10. Comentario final

### Cuántos casos salen

| Unidad | Cartesiano | Each use | Pairwise | Decisiones | MC/DC |
|---|---|---|---|---|---|
| SP | 1.260.001 | 38 | 157 | 9 | 6 (2 decisiones) |
| SC | 4 | 3 | 4 | — | — |
| CP | 10 | 5 | 10 | — | — |
| RG | 24 | 9 | 24 | 2 | — |
| RB | 12 (8 posibles) | 6 | 8 | 2 | — |
| MG | 4 | 3 | 4 | — | — |
| MB | 6 | 3 | 6 | — | — |
| CA | 10 | 5 | 10 | 3 | — |
| CC | 4 | 3 | 4 | — | — |
| CH | 24 | 8 | 24 | — | — |
| CU | 12 | 7 | 12 | — | — |
| VA | 5.120 | 20 | 80 | 2 | — |
| PC / RC / VL / VB | 2 / 2 / 3 / 1 | 2 / 2 / 3 / 1 | — | — | — |
| CF | 7 | 7 | — | 2 | 3 |
| CT | 2 | 2 | — | 2 | — |
| **Total** | | **127** | **343** | **22** | **9** |

En las unidades de dos variables, los casos each use son un subconjunto de los pairwise. Contando además CU-PF-01, el documento tiene **450 identificadores**:

| Situación | Casos |
|---|---|
| ✔ Ya cubiertos por los tests actuales | 27 |
| ◐ Cubiertos en parte | 24 |
| = Repiten las entradas de otro caso | 44 |
| ✘ Sin cubrir | 355 |
| **Por implementar en la fase 2 (◐ y ✘)** | **379** |

Hay 25 casos marcados con ⚠ (23 sin contar los repetidos).


### Qué cobertura se alcanza

- **Each use:** todos los valores del apartado 4 aparecen al menos una vez, y los inválidos van solos. Solo queda por probar lo excluido en el apartado 2.
- **Pairwise:** todos los pares posibles. En SP son 1.399 pares en 157 casos, frente a 1.260.001 del producto cartesiano; en VA, 416 pares en 80 casos, frente a 5.120. En RB hay 4 pares imposibles.
- **Decisiones:** las 16 decisiones de las unidades incluidas dan true y false (9 en SP, 2 en los repositorios, 2 en CA y 3 en la ventana).
- **MC/DC:** las 3 decisiones compuestas incluidas, con 3 casos cada una. En las decisiones de una sola condición, la cobertura de decisiones ya equivale a MC/DC.
- **Salidas:** cada tipo de salida y cada excepción tiene al menos un caso.

### Cómo ha influido la implementación

- **Orden de validación.** El servicio se detiene en el primer error. De los 157 casos pairwise de SP, 144 tienen algún campo inválido y en 134 hay dos o más, así que solo se comprueba uno de ellos. Pairwise detecta mal los fallos de validación cuando mezcla valores inválidos; por eso each use prueba cada valor inválido solo.
- **Oráculo independiente del orden.** El contrato no dice qué campo se valida antes, así que con varios inválidos se acepta el mensaje de cualquiera de ellos. Un cambio en el orden no rompería los tests.
- **Métodos privados.** `validarTexto` se usa con tres campos, pero es una sola decisión: MC/DC la prueba con el nombre y each use la repite con dni y apellidos.
- **Reloj inyectado.** Permite tratar la fecha de hoy como una variable más y probar H − 1, H y H + 1 sin depender del día en que se ejecute.
- **Cortocircuito de `||`.** Deja una fila de la tabla de verdad sin caso posible.
- **`trim()` e `isBlank()`.** No consideran espacio lo mismo: `trim()` quita también los caracteres de control. De ahí sale PF-2.
- **Validación repartida.** La fecha se interpreta en la vista y se valida en el servidor. Por eso las fechas imposibles (PF-1) solo se pueden detectar probando la vista: al servidor ya le llega una fecha válida.
- **Fallos de persistencia.** El servicio no los trata, los deja pasar, así que son salidas suyas (R3 a R5), y el controlador los convierte en mensaje (S4).

---

## 11. Suposiciones del equipo

El contrato (Javadoc de las interfaces) y RF-15 no fijan el resultado de estos cuatro casos. En las tablas figura el resultado propuesto. **PF-3, PF-4 y PF-6 están pendientes de decisión del equipo**: sus tests están desactivados con `@Disabled` y el código no se ha cambiado para ellos. PF-5 está aceptado y corregido.

| Código | Qué no fija el contrato | Resultado propuesto | Motivo | Estado |
|---|---|---|---|---|
| PF-3 | La longitud máxima de nombre y apellidos | Más de 255 caracteres se rechaza con `DatosInvalidosException` | La columna es `VARCHAR(255)`: si no se rechaza antes, el dato no cabe en la base de datos y el usuario solo ve un error genérico | Pendiente |
| PF-4 | Una fecha de nacimiento mínima (solo prohíbe las futuras) | 01/01/0001 se rechaza con `DatosInvalidosException` | Nadie vivo ha nacido ese año, así que es un error al escribir. No se fija un límite: se prueba una fecha que sería inválida con cualquiera razonable | Pendiente |
| PF-5 | Qué hace el controlador del cliente si recibe una respuesta nula | Muestra un error y conserva el formulario | El usuario debe saber que el alta no se ha confirmado. La parte del cliente HTTP sí la fija el contrato (devuelve una respuesta o lanza `ErrorComunicacionException`) | Aceptado y corregido |
| PF-6 | Qué hace `PacienteRepositorio.guardar` si el DNI ya existe | El repositorio en memoria lanza `PacienteDuplicadoException` y conserva al paciente | El de MySQL, que implementa la misma interfaz, ya lo hace. Dos implementaciones intercambiables deben comportarse igual, y sobrescribir pierde datos de un paciente | Pendiente |

---

## Anexo A. Posibles fallos

Se encontraron leyendo el código, antes de ejecutarlo. El resultado de la ejecución está en el anexo C.

| Código | Posible fallo | Resultado correcto | Por qué es el correcto | Casos |
|---|---|---|---|---|
| PF-1 | `31/02/2000` o `29/02/2001` se aceptarían cambiando el día al último del mes | `obtenerDatos` devuelve null y el conversor, null | `VistaAltaPaciente`: «null si la fecha de nacimiento está mal escrita» | VA-EU-17, VA-EU-19, CF-EU-07 y los VA-PW marcados |
| PF-2 | Un texto con solo caracteres de control pasa la validación y se guarda vacío | `DatosInvalidosException` | `ServicioPacientes`: «si falta algún dato obligatorio» | SP-EU-19, SP-EU-24 y los SP-PW marcados |
| PF-3 | Nombre o apellidos de más de 255 caracteres se aceptan, aunque no caben en la base de datos | `DatosInvalidosException` | Suposición pendiente (apartado 11) | SP-EU-15, SP-EU-20 y los SP-PW marcados |
| PF-4 | Se acepta una fecha de nacimiento del año 1 | `DatosInvalidosException` | Suposición pendiente (apartado 11) | SP-EU-26 y los SP-PW marcados |
| PF-5 | Si el servidor responde `null`, el cliente HTTP devuelve null y el controlador lanza `NullPointerException` | El cliente HTTP lanza `ErrorComunicacionException` y el controlador muestra un error | `ClientePacientes` siempre devuelve una respuesta o lanza esa excepción. En el controlador, suposición del equipo (apartado 11) | CH-PW-05, 13 y 21; CA-PW-04 |
| PF-6 | El repositorio en memoria sobrescribe al paciente si el DNI ya está | `PacienteDuplicadoException` | Suposición pendiente (apartado 11) | MG-PW-02 |
| PF-7 | Con una URL sin `http://`, `darDeAlta` lanza `IllegalArgumentException`, que el controlador no captura | `ErrorComunicacionException` | `ClientePacientes`: «si no se puede contactar con el servidor» | CU-PF-01 |

PF-6 y PF-7 aparecieron al hacer el diseño.

## Anexo B. Implementación (fase 2)

- Los casos ✔ no se implementan. Los ✘ y los ◐ están en clases nuevas terminadas en `CdU22Test`; los ◐, con el oráculo completo. Los tests anteriores no se han tocado. Los «= ID» se implementan una sola vez.
- Los casos se agrupan por criterio con `@Nested`. Los pairwise de SP y VA se leen de `src/test/resources/cdu22/`, generados a partir de las tablas del apartado 7. Los ⚠ van en métodos aparte (`posibleFallo...` y `posiblesFallos...`). SP-PW-012 se ha sacado de `posiblesFallosPairwise` a un método propio, con las mismas entradas y el mismo oráculo, para poder desactivarlo sin desactivar SP-PW-050 y SP-PW-156.
- Clases: `ServicioPacientesImplCdU22Test`, `PacienteRepositorioMySqlCdU22Test`, `PacienteRepositorioEnMemoriaCdU22Test` y `ControladorPacientesCdU22Test` (servidor); `ControladorAltaPacienteImplCdU22Test`, `ClientePacientesHttpCdU22Test` y `VentanaAltaPacienteCdU22Test` (cliente). Auxiliares: `CasoAlta` y `HttpClientFalso`.

### Regla de JaCoCo del CdU22

Es la ejecución `comprobar-cdu22` del `pom.xml` padre. Exige el 80 % de LINE y de BRANCH (BRANCH equivale a la cobertura de decisiones), salvo las excepciones de la tabla, y detiene el build. Se aplica **clase a clase**, para que una clase muy cubierta no compense a otra. No se han tocado la regla del equipo ni los Surefire duplicados.

| Excepción | Justificación |
|---|---|
| En `PacienteRepositorioMySql` solo se exige a `guardar` y `buscarPorDni` | El resto de la clase lee variables de entorno y está excluido (apartado 2). De sus 14 ramas se cubren 7: la clase se quedaría en el 50 % de BRANCH aunque los dos métodos del alta estén probados |
| `VentanaAltaPaciente` queda fuera | Sus tests se omiten donde no hay pantalla, como en GitHub Actions. Allí su cobertura sería del 0 % y el build fallaría sin que hubiera ningún defecto. Su cobertura se mira en el informe local |
| A `buscarPorDni` solo se le exige LINE | Llega al 100 % de LINE, pero solo al 62 % de BRANCH (5 de 8). Su única decisión, d2, está cubierta en true y en false. Las otras 6 ramas son los `if (recurso != null)` que genera el compilador para cerrar los `try-with-resources` cuando el método devuelve un valor. Las 3 que faltan no se pueden alcanzar: si el recurso fuera null, el método habría fallado antes de llegar ahí. Ningún caso de prueba puede subir esa cifra. `guardar` sigue con LINE y BRANCH |

### JaCoCo en rutas con caracteres no ASCII

En la carpeta de trabajo local, cuya ruta contiene `3º`, el agente escribe los datos en `jacoco.exe` en vez de `jacoco.exec`. El plugin no encuentra el fichero y se salta el informe y todas las reglas sin dar error. Una copia en una ruta sin `º` funciona, y de ella son los resultados del anexo C.

---

## Anexo C. Resultados de la ejecución

Hubo dos ejecuciones de `mvn clean verify`. La primera, sobre el código sin corregir, destapó los fallos. La segunda es posterior a las correcciones, se hizo en una copia en una ruta sin `º` (anexo B) y termina en **BUILD SUCCESS** sin ignorar fallos de test.

### Primera ejecución: fallos encontrados

791 tests, de los que 379 eran nuevos: pasaron 768 y fallaron 23. Los 23 eran exactamente los casos ⚠ distintos, así que **los siete posibles fallos se confirmaron**.

| Código | Casos que fallaron | Qué hacía el código |
|---|---|---|
| PF-1 | VA-EU-17 (= VA-PW-65), VA-EU-19 (= VA-PW-73), VA-PW-66, 67, 68, 74, 75 y 76, CF-EU-07 | Leía `29/02/2001` como 28/02/2001 y `31/02/2000` como 29/02/2000 |
| PF-2 | SP-EU-19, SP-EU-24, SP-PW-050 | Daba de alta al paciente sin rechazar el nombre ni los apellidos |
| PF-3 | SP-EU-15, SP-EU-20 | Daba de alta con 256 caracteres |
| PF-4 | SP-EU-26, SP-PW-012 | Aceptaba el 01/01/0001 (en SP-PW-012 seguía adelante y llegaba al fallo del repositorio) |
| PF-2, PF-3 y PF-4 | SP-PW-156 | Daba de alta con los tres datos inválidos |
| PF-5 | CH-PW-05, 13 y 21, CA-PW-04 | El cliente HTTP devolvía null y el controlador lanzaba `NullPointerException` |
| PF-6 | MG-PW-02 | Sobrescribía al paciente sin avisar |
| PF-7 | CU-PF-01 | Lanzaba `IllegalArgumentException` |

### Fallos corregidos

Las entradas y los oráculos de los tests no se han cambiado.

| Código | Por qué se corrige | Corrección | Casos que ahora pasan |
|---|---|---|---|
| PF-1 | Incumple el contrato de `VistaAltaPaciente` | En `VentanaAltaPaciente`, el formato de fecha usa `ResolverStyle.STRICT`. Como así `yyyy` necesita una era, se fija la actual; con `uuuu` se habría pasado a aceptar el año 0 | Los 9 de PF-1 |
| PF-2 | Incumple el contrato de `ServicioPacientes` | En `ServicioPacientesImpl.validarTexto`, `valor.isBlank()` pasa a `valor.trim().isBlank()`: se valida el mismo texto que se guarda | SP-EU-19, SP-EU-24, SP-PW-050 y SP-PW-156 |
| PF-5 | La parte del cliente HTTP incumple el contrato de `ClientePacientes`; la del controlador es la suposición aceptada del apartado 11 | `ClientePacientesHttp` lanza `ErrorComunicacionException` si la respuesta es null. `ControladorAltaPacienteImpl` muestra el error de conexión si recibe null | CH-PW-05, 13 y 21, CA-PW-04 |
| PF-7 | Incumple el contrato de `ClientePacientes` | `ClientePacientesHttp.darDeAlta` convierte también `IllegalArgumentException` en `ErrorComunicacionException` | CU-PF-01 |

SP-PW-156 combina PF-2, PF-3 y PF-4. Pasa porque ahora se rechaza el nombre (PF-2), así que no dice nada sobre PF-3 ni PF-4.

### Fallos sin corregir: tests desactivados

Llevan `@Disabled("PF-x: pendiente de decisión del equipo, ver apartado 11")`. En estos tres, el código sigue haciendo lo mismo que en la primera ejecución.

| Código | Casos desactivados | Por qué |
|---|---|---|
| PF-3 | SP-EU-15, SP-EU-20 | El contrato no fija una longitud máxima |
| PF-4 | SP-EU-26, SP-PW-012 | El contrato no fija una fecha mínima |
| PF-6 | MG-PW-02 | El contrato no dice qué hace `guardar` si el DNI ya existe |

Son 5 casos en 4 métodos. Surefire cuenta 4 omitidos porque un test parametrizado desactivado (el de PF-3) cuenta como uno.

### Qué cambian las correcciones en el diseño

No hacen falta casos nuevos. Las decisiones que cambian ya están cubiertas:

| Unidad | Decisión | true | false |
|---|---|---|---|
| SP | d4 pasa a `valor == null \|\| valor.trim().isBlank()` | SP-DC-03 | SP-DC-01 |
| CA | nueva: respuesta null | CA-PW-04 | CA-PW-01 |
| CH | nueva: respuesta null | CH-PW-05 | CH-PW-09 |

En d4, los tres casos de MC/DC (9.1) siguen valiendo. Las dos decisiones nuevas tienen una sola condición, así que no necesitan MC/DC.

### Surefire (segunda ejecución)

| Módulo | Ejecutados | Pasan | Fallan | Omitidos |
|---|---|---|---|---|
| servidor | 464 (220 anteriores y 244 nuevos) | 460 | 0 | 4 |
| cliente | 326 (192 anteriores y 134 nuevos) | 326 | 0 | 0 |
| **Total** | **790** (412 anteriores y 378 nuevos) | **786** | **0** | **4** |

La cuenta baja de 791 a 790 porque los dos casos del test parametrizado de PF-3 cuentan ahora como uno. «Ejecutados» incluye los omitidos, como en Surefire.

| Clase nueva | Ejecutados | Fallan | Omitidos |
|---|---|---|---|
| `ServicioPacientesImplCdU22Test` | 197 | 0 | 3 (PF-3 y PF-4) |
| `PacienteRepositorioMySqlCdU22Test` | 31 | 0 | 0 |
| `PacienteRepositorioEnMemoriaCdU22Test` | 7 | 0 | 1 (PF-6) |
| `ControladorPacientesCdU22Test` | 9 | 0 | 0 |
| `ControladorAltaPacienteImplCdU22Test` | 9 | 0 | 0 |
| `ClientePacientesHttpCdU22Test` | 32 | 0 | 0 |
| `VentanaAltaPacienteCdU22Test` | 93 | 0 | 0 |

### JaCoCo (segunda ejecución)

| Módulo | Clase | LINE | BRANCH | Regla |
|---|---|---|---|---|
| servidor | `ServicioPacientesImpl` (crítica) | 46/46 (100 %) | 24/24 (100 %) | cumple |
| servidor | `ControladorPacientes` | 10/10 (100 %) | sin ramas | cumple |
| servidor | `RutasPacientes` | 13/13 (100 %) | 2/2 (100 %) | cumple |
| servidor | `PacienteRepositorioEnMemoria` | 5/5 (100 %) | sin ramas | cumple |
| servidor | `PacienteRepositorioMySql.guardar` | 16/16 (100 %) | 2/2 (100 %) | cumple |
| servidor | `PacienteRepositorioMySql.buscarPorDni` | 15/15 (100 %) | 5/8 (62 %) | cumple (solo LINE) |
| servidor | `PacienteRepositorioMySql` (clase entera) | 34/45 (76 %) | 7/14 (50 %) | no se aplica |
| cliente | `ControladorAltaPacienteImpl` | 19/19 (100 %) | 6/6 (100 %) | cumple |
| cliente | `ClientePacientesHttp` | 26/26 (100 %) | 2/2 (100 %) | cumple |
| cliente | `VentanaAltaPaciente` | 52/61 (85 %) | 2/2 (100 %) | no se aplica |
| cliente | `VentanaAltaPaciente.ConversorFecha` | 6/6 (100 %) | 6/6 (100 %) | no se aplica |
| ambos | records y excepciones del alta | 100 % | sin ramas | cumple |

Módulo completo, con el código de todos los casos de uso: servidor, 82,7 % de LINE y 71,8 % de BRANCH; cliente, 74,5 % y 79,7 %. La regla del equipo (75 % de LINE, sin detener el build) avisa en el cliente.
