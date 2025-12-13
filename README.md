![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)

<img src="https://confedi.org.ar/wp-content/uploads/2020/09/fiuba_logo.jpg" width="600" height="173" align="center">

# SCDin - Simulador de Carga Dinámica
Departamento de Energía - FIUBA.

SCDin es un sistema de simulación de carga dinámica para el ensayo de dispositivos mecánicos. Consiste en un software cliente, basado en JAVA SWING, y otro software que corre en un sistema de accionamientos SIMOTION D425. 
<img width="1275" height="678" alt="image" src="https://github.com/user-attachments/assets/e16014a3-b65f-4c7d-ac4e-deb4f7676ba1" />


## Características del sistema

- Simulación de torque en función de la velocidad de giro: permite simular efectos mecánicos como resistencia por pendiente, rozamiento, resistencia aerodinámica e inercia.
- Simulación de torque en función del tiempo: permite la simulación de cualquier función de torque en función del tiempo, discretizada a 100ms.
- Ensayos mixtos: permite superponer las simulación en función de la velocidad con la simulación en función del tiempo.
- Adquisición de datos: se presentan 5 variables en tiempo real (Potencia activa [kW], Tensión de salida [Vrms], Corriente de salida [Arms], Velocidad angular [rad/s] y Torque electromécanico [Nm]). La actualización de variables es cada 100ms y se almacenan en formato CSV.

## Ejemplos de aplicación
Se diseño SCDin para poder realizar simular condiciones de carga en distinos sistemas mecánicos. A continuación algunos ejemplos:
-Simulación de condiciones de carga para vehículos.
-Simulación de condiciones de carga para generadores eólicos y mareomotrices.
-Simulación de condiciones de carga para máquinas herramienta.

## Instalación
En la carpeta de "Installer" del repositorio se presenta un instalador del cliente de SCDin. El software desarrollado para el sistema de accionamientos no está disponible libremente. 

## Documentación


## Licencia
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

