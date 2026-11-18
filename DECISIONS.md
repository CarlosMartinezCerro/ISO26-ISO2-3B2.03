# Decisiones del proyecto
## D-001 · Agente para implementar
Fecha: 13/10 · It. 0 · Acta: 13/10
...
## D-002 · Estructura Maven multimódulo
Fecha: 19/10 · It. 0 · Acta: 19/10 · Issue: #12
Contexto: hay dos aplicaciones, servidor y
 cliente, en un mismo repositorio.
Decisión: pom padre radiodiagnostico con dos
 módulos: servidor y cliente.
Alternativas: dos repositorios; un solo módulo.
Motivo: un mvn verify construye todo y las
 versiones se fijan una vez en el padre.
Consecuencias: cada módulo con su versión; al
 cambiar la del padre, actualizar <parent>.
 ...
 ## D-003 · Prioridad casos de uso
 Fecha: 23/10 · It. 0 · Acta: 23/10 · Issue: #4
 Contexto: necesitamos un orden de prioridad para
 los casos de uso.
 Decisión: Asignaremos valores de prioridad del
 1 al 4 siendo 1 el mas prioritario y 4 el menos.
 Alternativas: asignar valores del 1 al 5

 ## D-004 · Organización de grupos funcionales
Fecha: 30/10 · It. 0 · Acta: 30/10 · Issue: #8

**Contexto:** necesitamos decidir cómo organizar los grupos de trabajo del proyecto, teniendo en cuenta las funciones que desempeñan el cliente y el servidor.

**Decisión:** Como hemos observado que tanto el cliente como el servidor realizan las mismas funciones, hemos decidido organizar los grupos de trabajo en función de las funcionalidades del sistema, creando así grupos funcionales en lugar de separarlos por cliente y servidor.

**Alternativas:** organizar los grupos diferenciando entre cliente y servidor.

 ## D-005 · Definición de la línea base 
 Fecha: 03/10 · It. 0 · Acta:  · Issue: 
 **Contexto:** necesitamos concretar como vamos a organizar el trabajo.
 **Decisión:** - Cada caso de uso será realizado por una persona 
               - En un día vamos a poder realizar varios casos de uso 
               - La jornada laboral será de 40 horas(8 horas/día durante 5 días)
 **Alternativas:** 
## D-006 · Reutilización del paquete informe para imágenes
Fecha: 17/11 · It. 2 · Acta:  · Issue: CdU19
**Contexto:** el CdU17 (crear imagen) y el CdU19 (consultar imagen) trabajan con las mismas imágenes.
**Decisión:** - El CdU19 amplía las clases del paquete `informe` del CdU17 (Imagen, ImagenRepositorio, ServicioImagenes, ControladorImagenes) en lugar de crear otras.
              - En el cliente se usa una interfaz separada (`ClienteConsultaImagenes`) para no obligar al CdU17 a implementar métodos que no usa.
**Alternativas:** paquete `imagen` propio para el CdU19 (descartado: duplicaba Imagen y el repositorio).

## D-007 · Organización de paquetes: MVC por bloques funcionales
Fecha: 18/11 · It. 2 · Acta: 5  · Issue: 
**Contexto:** cada miembro estaba organizando el código de una forma distinta (por capas en el CdU10, un paquete por caso de uso en el CdU22, es.uclm.radiodiagnostico.modelo en el CdU16), lo que provocaba duplicados y conflictos al integrar en develop. El enunciado pide "paquetizar" las clases, aplicar MVC y comunicar los módulos a través de interfaces.
**Decisión:** - Paquete base es.uclm.esi.iso2.radiodiagnostico en cliente y servidor.
              - Vista y controlador en paquetes comunes: vista/ (cliente) y controlador/ (cliente y servidor).
              - El modelo se agrupa por bloques funcionales, no por caso de uso: cita/, paciente/, prueba/, informe/, administracion/.
              - Cada bloque expone interfaces (ServicioX, XRepositorio, ClienteX, VistaX) y las implementaciones se inyectan por constructor.
              - Se toma como referencia la estructura del CdU25.
**Alternativas:** organización por capas (dominio/negocio/persistencia), descartada por mezclar en un mismo paquete clases de casos de uso no relacionados; un paquete por caso de uso, descartada porque duplica entidades como Cita o Imagen.

## D-008 · Tecnología para la interfaz gráfica
Fecha: 18/11 · It. 0 · Acta: 5  · Issue:  

**Contexto:** necesitamos elegir una tecnología para desarrollar las interfaces gráficas del sistema, permitiendo la interacción de los usuarios con la aplicación.

**Decisión:** utilizaremos JavaFX para implementar las interfaces gráficas de la aplicación cliente.

**Alternativas:**
- Java Swing.
- JavaFX.
- Aplicación web.

**Motivo:** JavaFX permite desarrollar interfaces gráficas utilizando Java y facilita la separación entre la presentación y la lógica de la aplicación.

**Consecuencias:**
- Las interfaces gráficas se implementarán mediante JavaFX.
- La lógica de presentación se mantendrá separada de la lógica de negocio.
- El módulo cliente incorporará las dependencias necesarias para utilizar JavaFX.

---

## D-009 · Comunicación entre cliente y servidor mediante API REST
Fecha: 18/11 · It. 0 · Acta: 5  · Issue: 

**Contexto:** necesitamos establecer un mecanismo de comunicación entre los módulos cliente y servidor que permita intercambiar información y mantener ambos módulos desacoplados.

**Decisión:** utilizaremos una API REST basada en HTTP para la comunicación entre cliente y servidor, intercambiando información mediante JSON y apoyada de javalin.

**Alternativas:**
- Comunicación mediante sockets Java.
- Comunicación mediante API REST.

**Motivo:** REST permite separar la implementación del cliente de la del servidor, facilitando el mantenimiento y la evolución independiente de ambos módulos.

**Consecuencias:**
- El servidor expondrá los servicios mediante una API REST.
- El cliente realizará peticiones HTTP al servidor.
- Los datos intercambiados se representarán mediante JSON.
- Los contratos de comunicación deberán estar definidos para garantizar la compatibilidad entre ambos módulos.

---

## D-010 · Sistema gestor de bases de datos
Fecha: 18/11 · It. 0 · Acta:5   · Issue: 

**Contexto:** el sistema necesita almacenar y gestionar de forma persistente la información relacionada con pacientes, citas, pruebas diagnósticas y demás datos necesarios para su funcionamiento.

**Decisión:** utilizaremos MySQL como sistema gestor de bases de datos del proyecto.

**Alternativas:**
- MySQL.
- PostgreSQL.
- Apache Derby.

**Motivo:** se ha seleccionado MySQL como tecnología de almacenamiento persistente para centralizar la información del sistema y permitir su acceso desde el servidor.

**Consecuencias:**
- El servidor será responsable de gestionar las conexiones con la base de datos.
- El acceso a los datos se realizará desde la capa de persistencia.
- Se utilizarán las dependencias y configuraciones necesarias para conectar Java con MySQL.
- El cliente no accederá directamente a la base de datos.

 
