# MyTune

MyTune es un afinador de guitarra multiplataforma desarrollado como un **monorepo** que agrupa todas las implementaciones (Android, iOS y WebApp) junto con recursos compartidos, especificaciones y herramientas de automatización.

El proyecto se desarrolla siguiendo **SDD (Spec‑Driven Development)** y utilizando **Spec‑kit** para gestionar las especificaciones y mantener la alineación entre requerimientos, diseño y código.

## Estructura del monorepo

- `android/` – Aplicación Android (Kotlin, Gradle)
- `ios/` – Aplicación iOS (Swift, Xcode)
- `webapp/` – Aplicación Web (TypeScript + Vite)
- `shared/` – Recursos compartidos entre plataformas (design tokens, presets de afinación, documentación de algoritmos)
- `specs/` – Especificaciones funcionales y técnicas (Spec‑kit + SDD)
- `tools/` – Scripts y configuración de CI/CD y tooling auxiliar

Consulta `shared/README.md` para detalles de los recursos compartidos.

## Spec‑Driven Development (SDD) y Spec‑kit

Este repositorio utiliza **Spec‑Driven Development**:

1. **Escribir o actualizar la spec** en `specs/001-guitar-tuner/` (requerimientos, contratos de audio, estado de UI, etc.).
2. **Validar y refinar la spec** con Spec‑kit (revisiones, checklists, contratos).
3. **Implementar las funcionalidades** en cada plataforma tomando la spec como fuente de verdad.
4. **Mantener la traza** entre la spec y el código (referencias a tareas, decisiones de diseño y pruebas).

Spec‑kit se utiliza para:

- Organizar las specs por features/US (por ejemplo `001-guitar-tuner`).
- Gestionar **checklists**, **contratos** y **planes** de implementación.
- Facilitar la navegación entre documentación, código y tareas.

## Directorio `specs/`

Dentro de `specs/001-guitar-tuner/` encontrarás, entre otros:

- `spec.md` – Especificación principal de la funcionalidad de afinador de guitarra.
- `plan.md` – Plan de implementación guiado por la spec.
- `quickstart.md` – Guía rápida para empezar a trabajar en esta US/feature.
- `data-model.md` – Modelo de datos compartido entre plataformas.
- `contracts/` – Contratos de módulos (por ejemplo audio processing, UI state management).
- `checklists/` – Listas de verificación de requisitos y calidad.

Estas specs son el punto de partida obligado antes de tocar código en cualquier plataforma.

## Flujo de trabajo recomendado

1. **Elegir una tarea/feature** en función de la spec activa (por ejemplo, la US "001-guitar-tuner").
2. **Leer la spec correspondiente** en `specs/001-guitar-tuner/` (especialmente `spec.md`, `plan.md` y los contratos relevantes).
3. **Actualizar la spec si es necesario** (SDD: la spec se mantiene viva y sincronizada con el código).
4. **Implementar en la plataforma objetivo**:
   - Android: código en `android/app/src/main/`
   - iOS: código en `ios/MyTune/`
   - Web: código en `webapp/src/`
5. **Verificar contra la spec** (requisitos, casos de uso, checklists).
6. **Actualizar notas en specs** si hubiera cambios de alcance, decisiones técnicas o atajos temporales.

## Cómo ejecutar cada plataforma

> Estos comandos pueden variar según tu entorno; consulta también la documentación específica dentro de cada carpeta.

### Android

Desde `android/`:

```bash
cd android
./gradlew :app:assembleDebug
```

Abre luego el proyecto en Android Studio si lo prefieres.

### iOS

Desde `ios/`:

```bash
cd ios
open MyTune.xcodeproj
```

Compila y ejecuta la app desde Xcode en un simulador o dispositivo.

### WebApp

Desde `webapp/`:

```bash
cd webapp
npm install
npm run dev
```

La app se servirá típicamente en `http://localhost:5173` (o el puerto configurado por Vite).

## Recursos compartidos

Los recursos comunes (design tokens, presets de afinación, documentación de algoritmos de pitch detection) viven en `shared/` y deben ser la **única fuente de verdad** para todas las plataformas.

Consulta `shared/README.md` para:

- Cómo usar los design tokens en cada plataforma.
- Cómo cargar los presets de afinación.
- Detalles del algoritmo de detección de tono y conversión frecuencia‑nota.

## Contribuir

- Antes de implementar una nueva funcionalidad, **comienza en `specs/`**: revisa o crea la spec con Spec‑kit.
- Mantén actualizada la spec cuando el comportamiento final no coincida con lo inicialmente definido.
- Asegúrate de que Android, iOS y Web sigan usando los recursos de `shared/` para mantener la experiencia alineada.

Este README resume la visión global del monorepo y del flujo SDD. Para detalles por plataforma, revisa la documentación local de cada carpeta.