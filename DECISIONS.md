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
 Fecha: 19/10 · It. 0 · Acta: 19/10 · Issue: 
 Contexto: necesitamos un orden de prioridad para
 los casos de uso.
 Decisión: Asignaremos valores de prioridad del
 1 al 4 siendo 1 el mas prioritario y 4 el menos.
 Alternativas: asignar valores del 1 al 5
