# 🌤️ Cieloscopio

Aplicación de consola desarrollada en **Spring Boot** para consultar el clima y su estado, con la **API** de **OpenWeather**.

## 🚀 Funciones Principales 


- Menu con ciudades sugeridas para consultar.
- búsqueda personalizada de ciudad deseada.
- pronostico de las próximas 24horas
- Visualización de Estado(soleado, lluvioso, etc), Temperatura actual, Temperatura Mínima, Temperatura Máxima, Probabilidad de precipitaciones, Volumen o cantidad de lluvia
- Manejo de excepciones y errores tanto de API como Búsqueda.

## 🛠️ Tecnologías
- Java 17+
- Spring Boot
- GSON (para el mapeo de JSON)
- HttpClient (asíncrono)

## 🔑 Configuración

Para el correcto funcionamiento de la aplicacion se debe crear una variable de entorno `API_KEY_weathermap` con la clave generada en tu cuenta de OpenWeather 