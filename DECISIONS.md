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

 
