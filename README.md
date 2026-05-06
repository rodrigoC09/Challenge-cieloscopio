# 🌤️ Cieloscopio

Aplicación de consola desarrollada en **Spring Boot** para consultar el clima y su estado, con la **API** de **OpenWeather**.

## 🚀 Funciones Principales 


- Menu con ciudades sugeridas para consultar.
- Búsqueda personalizada de ciudad deseada.
- Pronostico de las próximas 24horas
- Pronostico de 3 dias
- Histórico de ciudades con fecha y hora de la consulta
- Visualización de Estado(soleado, lluvioso, etc), Temperatura actual, Temperatura Mínima, Temperatura Máxima, Probabilidad de precipitaciones, Volumen o cantidad de lluvia, Sensación térmica, Humedad, Viento
- Manejo de excepciones y errores tanto de API como Búsqueda.

## 🛠️ Tecnologías
- Java 17+
- Spring Boot
- GSON (para el mapeo de JSON)
- HttpClient (asíncrono)
- **Modularidad:** Uso de `Enums`
- **Programación Asíncronica:** Implementacion de `CompletableFuture`
- **Mapeo de Datos:** Uso de `Records` y anotaciones `GSON`
- **Gestión de Errores:** Excepciones personalizadas.
- **Escalabilidad progresiva**

## 🔑 Configuración

Para el correcto funcionamiento de la aplicacion se debe crear una variable de entorno `API_KEY_weathermap` con la clave generada en tu cuenta de OpenWeather 

## 📋 Mejoras a Futuro
- Implementación de cálculo matemático para Fase Lunar.

- Integración de Índice UV mediante endpoints Pro.