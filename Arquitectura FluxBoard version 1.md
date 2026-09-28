Como Diseñador de Software y Arquitecto de Soluciones, he analizado los requerimientos técnicos y comerciales de **FluxBoard**. Dada la naturaleza del proyecto y las restricciones del sistema operativo detalladas en el "Estudio Mercado App Portapapeles Móvil" y el documento de "FluxBoard imagen de marca y especificaciones de Onboarding", he estructurado la base técnica para garantizar escalabilidad, alto rendimiento y cumplimiento normativo.  
A continuación, presento el **Módulo 1: Descubrimiento y Arquitectura**.

### **Módulo 1: Descubrimiento y Arquitectura**

Para construir un gestor de portapapeles profesional que opere sin fricciones, debemos abordar una restricción crítica: a partir de Android 10, Google restringió completamente a las aplicaciones en segundo plano el acceso a los datos del portapapeles. Por lo tanto, la inserción y recolección de datos debe ocurrir bajo el control directo del usuario mediante una Extensión de Teclado Personalizado.

#### **1\. Stack Tecnológico Sugerido**

Para garantizar que el teclado opere de manera fluida y sin consumir exceso de memoria RAM, descartamos frameworks híbridos (Flutter/React Native) para el servicio central, optando por desarrollo nativo.

| Componente | Tecnología | Justificación Técnica |
| :---- | :---- | :---- |
| **Frontend (App & Teclado)** | Kotlin (Android Native) | Acceso directo a las APIs de bajo nivel de Android (como InputMethodService para el teclado). Máxima eficiencia en memoria y tiempo de respuesta para evitar latencias al escribir o pegar texto. |
| **Base de Datos Local** | Room (SQLite) | Persistencia local ultrarrápida. Permite que el teclado acceda a los recortes de forma offline e instantánea antes de sincronizar con la nube. |
| **Backend de Usuarios** | Supabase (PostgreSQL) | Estructura de costos escalable y predecible que evita penalizaciones financieras por volumen de lectura/escritura en miles de operaciones continuas. Ideal para gestionar la autenticación y la metadata de los usuarios. |
| **Almacenamiento de Objetos** | Cloudflare (Workers \+ KV/D1) | Almacenamiento de ultra baja latencia en el borde (Edge). Optimizado para guardar millones de fragmentos de texto pequeños (recordemos que 10,000 caracteres consumen aproximadamente 10 KB). |
| **Gestión de Suscripciones** | RevenueCat | Infraestructura ideal para gestionar y validar la suscripción anual competitiva de \$14.99 dólares. Facilita la implementación del periodo de prueba gratuito de 7 días (Opt-out) que puede alcanzar una tasa de conversión del 48.8%. |

#### **2\. Estándar Arquitectónico Seleccionado**

Se implementará **Clean Architecture acoplada con el patrón MVVM (Model-View-ViewModel)**.

* **Escalabilidad técnica:** Clean Architecture aísla la lógica de negocio de las interfaces de usuario. Esto es vital porque FluxBoard tiene dos "interfaces" distintas: la Aplicación Principal (Onboarding, Paywall, Ajustes) y el Servicio de Teclado Personalizado (InputMethodService). Ambas interfaces consumirán los mismos Casos de Uso (Use Cases) a través de Repositorios compartidos.  
* **Offline-First Strategy:** El repositorio implementará un patrón de "Single Source of Truth". El teclado leerá siempre de la base de datos local (Room). Un servicio de sincronización en segundo plano (WorkManager) se encargará de subir/descargar los objetos de Cloudflare y validar tokens con Supabase, garantizando que el usuario nunca experimente tiempos de carga al invocar el teclado.

#### **3\. Diagrama de Arquitectura General**

A continuación, el diagrama estructural del ecosistema FluxBoard en Android:

Fragmento de código  
architecture-beta  
    group fluxboard(logo:android, title: "Ecosistema Android (App Cliente)")  
    group backend(logo:cloud, title: "Infraestructura Cloud")

    service app(logo:mobile, title: "App Principal (UI)") in fluxboard  
    service keyboard(logo:keyboard, title: "Teclado (InputMethodService)") in fluxboard  
    service usecases(logo:gears, title: "Casos de Uso (Clean Arch)") in fluxboard  
    service localdb(logo:database, title: "Room DB (Local)") in fluxboard  
    service sync(logo:sync, title: "Sync Manager (WorkManager)") in fluxboard

    service supabase(logo:supabase, title: "Supabase (Auth & Users)") in backend  
    service cloudflare(logo:cloudflare, title: "Cloudflare (Clipboard Objects)") in backend  
    service revcat(logo:server, title: "RevenueCat (Suscripciones)") in backend

    app:R \--\> L:usecases  
    keyboard:T \--\> B:usecases  
    usecases:R \--\> L:localdb  
    usecases:B \--\> T:sync  
      
    sync:R \--\> L:supabase  
    sync:R \--\> L:cloudflare  
      
    app:T \--\> B:revcat

Fragmento de código  
sequenceDiagram  
    participant User as Usuario  
    participant Keyboard as FluxBoard Keyboard  
    participant LocalDB as Room Local DB  
    participant Sync as Sync Service  
    participant Cloudflare as Cloudflare Storage

    User-\>\>Keyboard: Selecciona texto y toca "Copiar"  
    Keyboard-\>\>LocalDB: Guarda objeto de texto (Cifrado local)  
    LocalDB--\>\>Keyboard: Confirmación de guardado  
    Keyboard--\>\>User: Actualiza UI de la tarjeta (Cian eléctrico \#00E5FF)  
      
    rect rgb(30, 30, 30\)  
        Note right of Sync: Proceso Asíncrono  
        Sync-\>\>LocalDB: Detecta nuevo objeto no sincronizado  
        Sync-\>\>Cloudflare: Sube objeto (Payload JSON)  
        Cloudflare--\>\>Sync: Status 200 OK  
        Sync-\>\>LocalDB: Marca objeto como "Sincronizado"  
    end

**Validación de Módulo:**  
He consolidado la base arquitectónica y el flujo de la infraestructura en la nube según sus requerimientos técnicos y de mercado.  
¿Aprueba este Módulo 1 para que proceda con el **Módulo 2: Diseño de Datos**, donde generaré el Modelado de la Base de Datos, el Diagrama Entidad-Relación y los Scripts SQL DDL para Supabase y la estructura para Cloudflare?

### **Módulo 2: Diseño de Datos**

El diseño de datos para FluxBoard exige una arquitectura híbrida que maximice la velocidad de lectura y minimice los costos de infraestructura. Al utilizar Supabase para la base de datos relacional, aseguramos una estructura de costos escalable y predecible que evita las penalizaciones financieras por volumen de lectura y escritura características de otras plataformas. Para complementar esto, Cloudflare (mediante Workers y KV/D1) actuará como el repositorio de objetos de texto, aprovechando que el contenido de texto es sumamente ligero (aproximadamente 10 KB por cada 10,000 caracteres).

#### **1\. Estrategia de Modelado de Datos**

El sistema se divide en tres dominios de datos interconectados:

> * **Dominio de Usuario y Suscripción (Supabase):** Gestiona la identidad, el estado de la suscripción (Free vs. Pro) y las validaciones del periodo de prueba.  
> * **Dominio de Metadatos de Recortes (Supabase):** Almacena las referencias de los recortes, marcas de tiempo, y estados booleanos como la capacidad de fijar recortes (is\_pinned), lo cual es una característica central requerida por el mercado\[cite: 1, 2\].  
> * **Dominio de Carga Útil (Cloudflare KV):** Almacena el texto cifrado real. Supabase solo guarda el identificador (llave) para recuperar el texto desde el borde (Edge) de Cloudflare, reduciendo el peso de la base de datos relacional al escalar hacia historiales virtualmente ilimitados (hasta 10,000 recortes en la versión Pro).

#### **2\. Diagrama Entidad-Relación (ERD)**

El siguiente modelo ilustra la estructura relacional en PostgreSQL (Supabase) y su puente lógico hacia Cloudflare.

Fragmento de código  
erDiagram  
    USERS ||--o{ SUBSCRIPTIONS : "gestiona"  
    USERS ||--o{ DEVICES : "registra"  
    USERS ||--o{ CLIP\_METADATA : "posee"  
    DEVICES ||--o{ CLIP\_METADATA : "origina"  
      
    USERS {  
        uuid id PK "auth.uid()"  
        string email UK  
        timestamp created\_at  
        timestamp last\_login  
    }  
      
    SUBSCRIPTIONS {  
        uuid id PK  
        uuid user\_id FK  
        string status "active, trialing, canceled"  
        string revenuecat\_rc\_id "RC App User ID"  
        boolean is\_pro "True si plan=\$14.99"  
        timestamp expires\_at  
    }  
      
    DEVICES {  
        uuid id PK  
        uuid user\_id FK  
        string device\_name "Ej. Pixel 7 Pro"  
        string fcm\_token "Para invalidación de caché"  
        timestamp last\_sync\_at  
    }  
      
    CLIP\_METADATA {  
        uuid id PK  
        uuid user\_id FK  
        uuid source\_device\_id FK  
        string cloudflare\_key "Llave para buscar el texto"  
        boolean is\_pinned "Estado fijado por el usuario"  
        string content\_hash "SHA-256 para evitar duplicados"  
        integer size\_bytes "Tamaño del texto"  
        timestamp created\_at  
    }

#### **3\. Scripts SQL DDL (Supabase / PostgreSQL)**

Estos scripts definen el esquema exacto, aplicando restricciones de integridad y Row Level Security (RLS) para garantizar que los datos estén completamente aislados por usuario, cumpliendo con las políticas de privacidad requeridas.

SQL  
\-- Habilitar extensión para UUIDs  
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\-- Tabla: USERS (Extensión lógica de auth.users de Supabase)  
CREATE TABLE public.users (  
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,  
    email VARCHAR(255) UNIQUE NOT NULL,  
    created\_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,  
    last\_login TIMESTAMP WITH TIME ZONE  
);

\-- Tabla: SUBSCRIPTIONS  
CREATE TABLE public.subscriptions (  
    id UUID PRIMARY KEY DEFAULT uuid\_generate\_v4(),  
    user\_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,  
    status VARCHAR(50) NOT NULL CHECK (status IN ('active', 'trialing', 'canceled', 'expired')),  
    revenuecat\_rc\_id VARCHAR(255) UNIQUE,  
    is\_pro BOOLEAN DEFAULT FALSE NOT NULL,  
    expires\_at TIMESTAMP WITH TIME ZONE,  
    CONSTRAINT uk\_user\_subscription UNIQUE(user\_id)  
);

\-- Tabla: DEVICES  
CREATE TABLE public.devices (  
    id UUID PRIMARY KEY DEFAULT uuid\_generate\_v4(),  
    user\_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,  
    device\_name VARCHAR(100) NOT NULL,  
    fcm\_token VARCHAR(255),  
    last\_sync\_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()),  
    CONSTRAINT uk\_user\_device UNIQUE(user\_id, device\_name)  
);

\-- Tabla: CLIP\_METADATA  
CREATE TABLE public.clip\_metadata (  
    id UUID PRIMARY KEY DEFAULT uuid\_generate\_v4(),  
    user\_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,  
    source\_device\_id UUID REFERENCES public.devices(id) ON DELETE SET NULL,  
    cloudflare\_key VARCHAR(255) NOT NULL UNIQUE,  
    is\_pinned BOOLEAN DEFAULT FALSE NOT NULL,  
    content\_hash VARCHAR(64) NOT NULL,  
    size\_bytes INTEGER NOT NULL CHECK (size\_bytes \> 0 AND size\_bytes \< 5242880), \-- Max \~5MB límite de texto preventivo  
    created\_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL  
);

\-- Índices de Rendimiento  
CREATE INDEX idx\_clip\_user ON public.clip\_metadata(user\_id);  
CREATE INDEX idx\_clip\_created ON public.clip\_metadata(created\_at DESC);  
CREATE INDEX idx\_clip\_hash ON public.clip\_metadata(content\_hash);

\-- Configuración de Seguridad (Row Level Security)  
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;  
ALTER TABLE public.subscriptions ENABLE ROW LEVEL SECURITY;  
ALTER TABLE public.devices ENABLE ROW LEVEL SECURITY;  
ALTER TABLE public.clip\_metadata ENABLE ROW LEVEL SECURITY;

\-- Políticas RLS (Ejemplo para Clip Metadata)  
CREATE POLICY "Users can only select their own clips"   
ON public.clip\_metadata FOR SELECT   
USING (auth.uid() \= user\_id);

CREATE POLICY "Users can only insert their own clips"   
ON public.clip\_metadata FOR INSERT   
WITH CHECK (auth.uid() \= user\_id);

#### **4\. Estructura de Carga Útil en Cloudflare KV**

En Cloudflare, se utilizará un almacén clave-valor (KV) para la recuperación ultrarrápida del contenido de texto mediante la red Edge. Esta separación protege a la base de datos de transacciones pesadas.

* **Convención de Llave (Key):** clip:{user\_id}:{clip\_metadata\_id}  
* **Estructura del Valor (JSON Payload \- TypeScript Interface):**

TypeScript  
// Contrato de datos almacenado en Cloudflare KV  
interface CloudflareClipPayload {  
  // Identificador referencial a Supabase  
  clip\_id: string;   
  // Contenido de texto real copiado al portapapeles  
  text\_content: string;   
  // Marca de tiempo de inserción en el Edge  
  edge\_timestamp: number;  
  // Bandera de seguridad si detecta un formato de contraseña (ej. ClipDescription.EXTRA\_IS\_SENSITIVE)  
  contains\_sensitive\_pattern: boolean;  
}

¿Aprueba este Módulo 2 para proceder con el **Módulo 3: Lógica y Estructura de Archivos**, donde definiré el File Tree nativo en Android (Kotlin/Clean Architecture), las interfaces de los repositorios y la configuración de WorkManager para la sincronización en segundo plano?

Avanzamos con el **Módulo 3: Lógica y Estructura de Archivos**.  
Para cumplir con las restricciones arquitectónicas de Android, donde la inserción y recolección de datos del portapapeles debe ocurrir bajo el control directo del usuario, el núcleo de FluxBoard se dividirá lógicamente en dos interfaces de presentación (App Principal y Servicio de Teclado) que comparten la misma capa de dominio y datos mediante **Clean Architecture**.

### **1\. Mapa del Sistema de Archivos (File Tree)**

La estructura del proyecto en Kotlin seguirá una separación estricta de responsabilidades, facilitando la inyección de dependencias (vía Hilt/Dagger) y el testing unitario.

Plaintext  
com.fluxboard.app  
├── core/                        \# Componentes transversales  
│   ├── security/                \# Cifrado local y validación de datos sensibles  
│   ├── network/                 \# Clientes HTTP (Ktor/Retrofit)  
│   └── utils/                   \# Constantes y extensiones  
├── di/                          \# Módulos de Inyección de Dependencias  
│   ├── DataModule.kt  
│   ├── NetworkModule.kt  
│   └── DomainModule.kt  
├── domain/                      \# Capa de Dominio (Agnóstica al framework)  
│   ├── models/                  \# Entidades de negocio (ClipItem, UserSubscription)  
│   ├── repository/              \# Contratos (Interfaces) de los repositorios  
│   └── usecases/                \# Casos de uso  
│       ├── SaveClipUseCase.kt  
│       ├── GetClipsUseCase.kt  
│       └── ValidateSubscriptionUseCase.kt  
├── data/                        \# Capa de Datos (Implementaciones)  
│   ├── local/                   \# Base de datos local (Room)  
│   │   ├── FluxDatabase.kt  
│   │   └── ClipDao.kt  
│   ├── remote/                  \# APIs externas  
│   │   ├── SupabaseSource.kt    \# Cliente de PostgreSQL/Auth  
│   │   └── CloudflareSource.kt  \# Cliente de KV/Edge Storage  
│   ├── repository/              \# Implementaciones de domain/repository  
│   │   ├── ClipRepositoryImpl.kt  
│   │   └── AuthRepositoryImpl.kt  
│   └── worker/                  \# Tareas en segundo plano  
│       └── SyncWorker.kt        \# Sincronización asíncrona con Supabase/Cloudflare  
└── presentation/                \# Capa de Interfaz y Servicios Android  
    ├── app/                     \# UI de la Aplicación Principal (Jetpack Compose)  
    │   ├── onboarding/          \# Pantallas 1 y 2  
    │   ├── paywall/             \# Pantalla 3 (Muro de pago y Opt-out trial)  
    │   └── settings/            \# Activación modal del teclado y preferencias  
    └── keyboard/                \# Servicio Core de Teclado  
        ├── FluxBoardKeyboard.kt \# Hereda de InputMethodService  
        └── components/          \# Vistas del teclado (Jetpack Compose / Views)

### **2\. Definición de Contratos de Dominio (Modelos e Interfaces)**

El dominio define los modelos puros y las interfaces (contratos) que la capa de datos debe cumplir. Esto asegura que la lógica de negocio de FluxBoard no dependa directamente de Room, Supabase o Cloudflare.

Kotlin  
package com.fluxboard.app.domain.models

import java.util.Date  
import java.util.UUID

// Entidad de Negocio Principal  
data class ClipItem(  
    val id: UUID \= UUID.randomUUID(),  
    val textContent: String,  
    val isPinned: Boolean \= false,  
    val isSynced: Boolean \= false,  
    val createdAt: Date \= Date()  
)

// Estado de la Suscripción  
enum class SubscriptionTier {  
    FREE, PRO, TRIAL  
}

Kotlin  
package com.fluxboard.app.domain.repository

import com.fluxboard.app.domain.models.ClipItem  
import kotlinx.coroutines.flow.Flow

// Contrato que debe implementar la capa de datos  
interface IClipRepository {  
    // Retorna un flujo reactivo para actualizar el teclado en tiempo real  
    fun getRecentClips(limit: Int): Flow\<List\<ClipItem\>\>  
      
    // Guarda localmente y encola para sincronización  
    suspend fun saveClip(content: String): Result\<Unit\>  
      
    // Cambia el estado de un recorte a fijado/no fijado  
    suspend fun togglePinStatus(clipId: String): Result\<Unit\>  
      
    // Forzar sincronización manual o programada  
    suspend fun syncPendingClips(): Result\<Unit\>  
}

### **3\. Servicio Core del Teclado (InputMethodService)**

Dado que a partir de Android 10, Google restringió completamente a las aplicaciones en segundo plano el acceso a los datos del portapapeles, toda inserción y recolección de datos debe ocurrir bajo el control directo del usuario mediante el teclado. El siguiente controlador ilustra cómo se intercepta el portapapeles de manera legal y reactiva cuando el usuario invoca el teclado.

Kotlin  
package com.fluxboard.app.presentation.keyboard

import android.content.ClipboardManager  
import android.content.Context  
import android.inputmethodservice.InputMethodService  
import android.view.View  
import kotlinx.coroutines.\*  
import javax.inject.Inject

class FluxBoardKeyboard : InputMethodService() {

    @Inject lateinit var saveClipUseCase: SaveClipUseCase  
    @Inject lateinit var getClipsUseCase: GetClipsUseCase  
      
    private val keyboardScope \= CoroutineScope(Dispatchers.Main \+ SupervisorJob())  
    private var clipboardManager: ClipboardManager? \= null

    override fun onCreate() {  
        super.onCreate()  
        // Hilt Injection o equivalente aquí  
        clipboardManager \= getSystemService(Context.CLIPBOARD\_SERVICE) as ClipboardManager  
    }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {  
        super.onStartInputView(info, restarting)  
        // 1\. Cuando el usuario abre el teclado, leemos el portapapeles activamente  
        checkClipboardContent()  
          
        // 2\. Observar los clips de la base de datos (Room) para pintar la UI  
        keyboardScope.launch {  
            getClipsUseCase().collect { clips \-\>  
                // Actualizar la interfaz (Tarjetas visuales)  
                renderClipsUI(clips)  
            }  
        }  
    }

    private fun checkClipboardContent() {  
        clipboardManager?.primaryClip?.let { clipData \-\>  
            if (clipData.itemCount \> 0) {  
                val text \= clipData.getItemAt(0).text?.toString()  
                if (\!text.isNullOrBlank()) {  
                    keyboardScope.launch {  
                        // El UseCase evalúa si el texto ya existe o si es contenido sensible  
                        saveClipUseCase(text)   
                    }  
                }  
            }  
        }  
    }

    // Inserción de texto en la aplicación objetivo  
    fun pasteText(content: String) {  
        val inputConnection \= currentInputConnection  
        inputConnection?.commitText(content, 1)  
    }

    override fun onDestroy() {  
        super.onDestroy()  
        keyboardScope.cancel()  
    }  
}

### **4\. Lógica de Sincronización en Segundo Plano (WorkManager)**

Para garantizar la experiencia *Offline-First* y evitar tiempos de carga en el teclado, la sincronización hacia Supabase y Cloudflare se delega a un hilo secundario gestionado por Android WorkManager.

Kotlin  
package com.fluxboard.app.data.worker

import android.content.Context  
import androidx.work.CoroutineWorker  
import androidx.work.WorkerParameters  
import com.fluxboard.app.domain.usecases.SyncClipsUseCase

class SyncWorker(  
    appContext: Context,  
    workerParams: WorkerParameters,  
    private val syncClipsUseCase: SyncClipsUseCase  
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {  
        return try {  
            // Extrae los clips marcados como is\_synced \= false en Room  
            // Sube el texto a Cloudflare KV  
            // Inserta la metadata y la llave de Cloudflare en Supabase  
            // Actualiza el estado local a is\_synced \= true  
            val success \= syncClipsUseCase()  
            if (success) Result.success() else Result.retry()  
        } catch (e: Exception) {  
            Result.retry()  
        }  
    }  
}

¿Aprueba este Módulo 3 para proceder con el **Módulo 4: Diseño de Interfaz (UI/UX)**, donde definiré la especificación visual (paleta hexadecimal y tipografías descritas en los requerimientos) y los wireframes de la experiencia de Onboarding y Muro de Pago?

### **Módulo 4: Diseño de Interfaz (UI/UX)**

El diseño de FluxBoard se basa en una filosofía de "utilidad pura", transmitiendo permanencia y orden para contrarrestar la naturaleza efímera del portapapeles nativo. A continuación, se define la guía de estilo técnica y la especificación de los wireframes para la aplicación Android.

#### **1\. Identidad Visual y Guía de Estilos**

El sistema cromático descansa principalmente en un modo oscuro profundo, lo cual reduce el consumo de batería, la fatiga visual, y se integra de forma natural sobre las demás aplicaciones cuando se despliega el teclado.  
**Logotipo e Iconografía:**

* **Isotipo:** Diseño abstracto de dos tarjetas deslizándose horizontalmente, simulando movimiento continuo y evocando un sujetapapeles o documentos superpuestos. Este enfoque optimiza el descubrimiento orgánico (ASO).  
* **Logotipo:** Fuente sans-serif geométrica con la palabra "Flux" en peso *Bold* y "Board" en peso *Regular* para un contraste visual moderno.

**Sistema de Colores (WCAG AA/AAA Compliant):**

| Rol del Color | Valor Hexadecimal | Uso y Justificación Funcional |
| :---- | :---- | :---- |
| **Fondo Principal** | \#121212 (Negro Carbón) | Fondo principal de la extensión del teclado y de los paneles de configuración. |
| **Superficies (Cards)** | \#1E1E1E (Gris Pizarra) | Fondos de las tarjetas de recortes individuales, creando jerarquía sobre el fondo negro. |
| **Acento Primario** | \#6366F1 (Índigo Eléctrico) | Botones de acción principal (CTA), como "Comenzar Prueba Gratuita" y contornos de selección. |
| **Acento Secundario** | \#10B981 (Verde Esmeralda) | Confirmaciones de éxito y estado de sincronización con la base de datos Supabase. |
| **Acento Interfaz** | \#00E5FF (Cian Eléctrico) | Dirige el ojo inmediatamente a las acciones de copiado/pegado. |
| **Texto Principal** | \#F9FAFB (Blanco Humo) | Texto principal e historial; garantiza legibilidad de alto contraste sobre \#121212 y \#1E1E1E. |
| **Texto Secundario** | \#9CA3AF (Gris Muteado) | Avisos de privacidad y mitigación de fricción, como la nota sobre contraseñas. |
| **Fondos Alternativos** | \#18181B (Grafito) | Reservado para transiciones de fondo y gradientes sutiles. |

**Sistema Tipográfico:** Para garantizar la velocidad de escaneo visual instantáneo al pegar texto, la tipografía se divide estrictamente según la función del texto.

* **Tipografía de Interfaz (UI):** *Inter*. Empleada para menús de configuración, pantallas de incorporación (H1, Body) y etiquetas informativas.  
* **Tipografía de Datos:** *JetBrains Mono*. Empleada exclusivamente para el texto copiado dentro de las tarjetas visuales, aportando un aspecto técnico óptimo para fragmentos de código o enlaces.

#### **2\. Especificación Estructural: Componente de Tarjeta (Clip Card)**

En lugar de listas de texto plano, la información en el teclado se presenta en tarjetas visuales (cards) bien delineadas, previniendo errores táctiles en dispositivos móviles.

* **Contenedor:** Rectángulo con radio de borde de 8dp. Fondo: \#1E1E1E.  
* **Contenido:** Texto truncado a 3 líneas máximas (Fuente: JetBrains Mono, 14sp, Color: \#F9FAFB).  
* **Metadatos (Inferior):** Icono de sincronización de nube (Verde \#10B981 si está en Supabase), marca de tiempo relativa ("hace 5 min").  
* **Estado Activo:** Al ser pulsada, el borde se ilumina en \#00E5FF (Cian) antes de ejecutar la acción de pegado.

#### **3\. Wireframes: Flujo de Onboarding y Conversión**

El flujo consta de tres pantallas estratégicas seguidas de un modal de activación del teclado. Este embudo está diseñado para maximizar la conversión a la suscripción anual de \$14.99 dólares (prueba Opt-out).  
**Pantalla 1: Identificación del Problema (El Gancho Emocional)**

| Región | Elemento UI | Especificaciones y Texto |
| :---- | :---- | :---- |
| **Top** | **Gráfico** | Animación vectorial: Texto sobrescribiéndose por error (ej. perder un enlace al copiar un meme). |
| **Centro** | **H1 (Inter Bold)** | "Nunca vuelvas a perder un enlace importante." |
| **Centro** | **Body (Inter Reg)** | "FluxBoard guarda automáticamente todo lo que copias para que no tengas que cambiar de aplicación constantemente." |
| **Bottom** | **CTA Primario** | Botón ancho con fondo \#6366F1. Texto: "Continuar". |
| **Bottom** | **Paginación** | Tres puntos (Punto 1 activo). |

**Pantalla 2: La Solución Técnica (El Momento 'Aha')**

| Región | Elemento UI | Especificaciones y Texto |
| :---- | :---- | :---- |
| **Top** | **Mockup Animado** | Captura del teclado FluxBoard desplegándose en una app de mensajería, mostrando tarjetas \#1E1E1E. |
| **Centro** | **H1 (Inter Bold)** | "Tu historial, directo en tu teclado." |
| **Centro** | **Body (Inter Reg)** | "Pega textos, enlaces y correos anteriores en cualquier campo de texto al instante con nuestro teclado nativo." |
| **Bottom** | **CTA Primario** | Botón ancho con fondo \#6366F1. Texto: "Ver cómo funciona" o "Siguiente". |
| **Bottom** | **Paginación** | Tres puntos (Punto 2 activo). |

**Pantalla 3: El Muro de Pago (Hard Paywall)**

| Región | Elemento UI | Especificaciones y Texto |
| :---- | :---- | :---- |
| **Top Nav** | **Botón Cierre** | Icono "X" o texto "Omitir por ahora" (Permite acceso a nivel gratuito restringido). |
| **Header** | **H1 (Inter Bold)** | "Desbloquea FluxBoard Pro" |
| **Centro** | **Beneficios** | Viñetas: Historial Ilimitado, Sincronización en la Nube, Organización Avanzada. |
| **Centro** | **Precio** | Destacado: "7 días de prueba gratis". Secundario: "Luego \$14.99 USD al año. Cancela cuando quieras." |
| **Bottom** | **CTA Conversión** | Gran botón prominente \#6366F1. Texto: "Comenzar Prueba Gratuita". |
| **Footer** | **Avisos Legales** | Enlaces a Política de Privacidad y Términos de Servicio. |

**Pantalla Modal: Activación del Teclado (Mitigación de Drop-off)**

Desplegado justo después del Onboarding/Compra. Ejecuta un Intent directo a la configuración de Android (ACTION\_INPUT\_METHOD\_SETTINGS).

| Estado: Inicial (Fricción Técnica) | Estado: Éxito (Condicional) |
| :---- | :---- |
| **Gráfico:** Lottie GIF mostrando un dedo encendiendo un interruptor y tocando "Aceptar". | **Gráfico:** Ícono grande de "Check" en color \#10B981 (Verde Esmeralda). |
| **H1:** "Último paso: Enciende el motor" | **H1:** "Teclado Activado" |
| **Body:** "Para proteger tus datos, Android requiere que actives manualmente los teclados nuevos..." | **Body:** "FluxBoard está listo para capturar tu historial." |
| **Instrucciones:** 1\. Toca el botón de abajo. 2\. Enciende el interruptor de FluxBoard. 3\. Haz clic en Aceptar... |  |
| **CTA:** "Abrir Ajustes de Teclado" (Fondo \#6366F1) | **CTA:** "¡Todo listo\! Ir a la App" (Fondo \#6366F1) |
| **Aviso Mitigación:** Texto gris \#9CA3AF: "Nota: Android muestra una alerta estándar para todos los teclados. FluxBoard nunca leerá contraseñas ni datos de tarjetas de crédito." |  |

Con este módulo, hemos completado la Arquitectura Técnica, el Diseño de Datos, la Lógica de Código y las Especificaciones UI/UX. La documentación integral está lista para ser entregada a su equipo de desarrollo (Frontend Kotlin/Jetpack Compose, Backend Supabase/Cloudflare).  
¿Desea que generemos un archivo README.md estructurado que resuma estos cuatro módulos para el repositorio de inicio del equipo?

Markdown  
\# FluxBoard \- Documentación Técnica y Arquitectura (Android)

FluxBoard es un gestor de portapapeles profesional para Android diseñado para eliminar la fricción cognitiva del usuario al gestionar información temporal. Dado que a partir de Android 10 el sistema restringe el acceso en segundo plano al portapapeles, el núcleo de esta aplicación opera mediante una **\*\*Extensión de Teclado Personalizado\*\*** de invocación directa.

\#\# 1\. Stack Tecnológico y Arquitectura

El proyecto emplea una arquitectura **\*\*Clean Architecture \+ MVVM\*\*** con una estrategia **\*\*Offline-First\*\***, aislando la interfaz principal y el servicio del teclado bajo los mismos casos de uso de dominio.

\*   **\*\*Frontend (App & Teclado):\*\*** Kotlin nativo (Jetpack Compose para UI Principal, \`InputMethodService\` para el teclado).  
\*   **\*\*Persistencia Local:\*\*** Room (SQLite) para acceso ultrarrápido y disponibilidad sin conexión.  
\*   **\*\*Backend Relacional y Auth:\*\*** Supabase (PostgreSQL), seleccionado por su estructura de costos escalable y predecible.  
\*   **\*\*Almacenamiento de Carga Útil:\*\*** Cloudflare KV / D1 en el Edge para textos ligeros cifrados.  
\*   **\*\*Gestión de Suscripciones:\*\*** RevenueCat, gestionando un plan anual de \$14.99 USD con un periodo de prueba de 7 días (Opt-out).  
\*   **\*\*Sincronización:\*\*** Android \`WorkManager\` para delegar la subida y bajada de objetos en segundo plano.

\#\# 2\. Modelo de Datos (Híbrido)

La estructura de datos separa la metainformación de la carga útil pesada para optimizar el rendimiento.

\#\#\# Supabase (PostgreSQL)  
Gestiona entidades relacionales, estado de suscripción y metadatos de los clips.  
\*   **\*\*\`users\` / \`devices\`:\*\*** Gestión de identidad y control de acceso cruzado.  
\*   **\*\*\`subscriptions\`:\*\*** Validación de estado \`is\_pro\` mediante webhooks de RevenueCat.  
\*   **\*\*\`clip*\_metadata\`:\*\* Almacena referencias (\`cloudflare\_key\`), estado de fijación (\`is\_pinned\`) y hashes (\`content\_hash\`)\[cite: 1, 2\]. Todo protegido bajo Row Level Security (RLS)\[cite: 1\].***

***\#\#\# Cloudflare KV***  
***Almacena el texto puro.***  
***\*   \*\*Llave:\*\* \`clip:{user\_id}:{clip\_metadata\_id}\`***  
***\*   \*\*Valor:\*\* Objeto JSON con el texto real y banderas de detección de patrones sensibles (ej. contraseñas)\[cite: 1, 2\].***

***\#\# 3\. Mapa del Sistema de Archivos (Core)***

***\`\`\`text***  
***com.fluxboard.app***  
***├── core/                        \# Utilidades y encriptación local***  
***├── di/                          \# Módulos Hilt/Dagger***  
***├── domain/                      \# Modelos puros e Interfaces de repositorios***  
***├── data/***  
***│   ├── local/                   \# Room DB (FluxDatabase, ClipDao)***  
***│   ├── remote/                  \# SupabaseSource, CloudflareSource***  
***│   └── worker/                  \# SyncWorker para sincronización asíncrona***  
***└── presentation/***  
    ***├── app/                     \# UI Compose (Onboarding, Paywall, Settings)***  
    ***└── keyboard/                \# InputMethodService y UI del teclado nativo***

## **4\. Sistema de Diseño (UI/UX)**

La interfaz transmite orden y permanencia mediante un modo oscuro profundo, minimizando la fatiga visual. Las listas tradicionales se reemplazan por **tarjetas visuales** para evitar errores táctiles.

### **Sistema Cromático**

* **Fondo Principal:** \#121212 (Negro Carbón).  
* **Superficies (Tarjetas):** \#1E1E1E (Gris Pizarra).  
* **Acento Primario (CTAs):** \#6366F1 (Índigo Eléctrico).  
* **Acento Interfaz (Acciones):** \#00E5FF (Cian Eléctrico).  
* **Confirmaciones/Sincronización:** \#10B981 (Verde Esmeralda).  
* **Texto Principal:** \#F9FAFB (Blanco Humo).

### **Tipografía**

* **Interfaz (UI) y Textos de Apoyo:** Inter (Legibilidad óptima en interfaces compactas).  
* **Datos y Contenido del Portapapeles:** JetBrains Mono (Facilita el escaneo visual rápido de fragmentos de código o enlaces).

## **5\. Flujos Críticos de Usuario (Onboarding)**

El embudo inicial está optimizado para demostrar valor instantáneo y generar altas conversiones (hasta 48.8% en pruebas Opt-out).

> 1. **Gancho Emocional:** "Nunca vuelvas a perder un enlace importante."\[cite: 2\]  
> 2. **Momento Aha:** Demostración animada del teclado inyectando historial directamente en otras aplicaciones\[cite: 2\].  
> 3. **Muro de Pago (Hard Paywall):** Oferta clara ("7 días de prueba gratis, luego \$14.99 USD al año") con una opción de salida ("Omitir por ahora") hacia el nivel gratuito\[cite: 2\].  
> 4. **Activación del Teclado:** Modal guiado post-compra con un Intent directo a los ajustes del sistema (ACTION\_INPUT\_METHOD\_SETTINGS) y texto disuasorio confirmando la privacidad de las contraseñas para evitar el abandono de usuarios\[cite: 2\].

### **Módulo 5: Analíticas y Telemetría (Módulo Adicional)**

Para medir la viabilidad comercial y técnica de FluxBoard sin comprometer la estricta privacidad del portapapeles exigida por los sistemas operativos, el módulo de analíticas debe operar de manera anónima y basada en eventos (Event-Driven). Se recomienda la integración conjunta de **RevenueCat** (ya incluido para métricas financieras) y **PostHog** (como motor de analíticas de producto de código abierto, amigable con la privacidad).

#### **1\. Matriz de Indicadores Clave de Rendimiento (KPIs)**

| Categoría | Indicador (KPI) | Justificación y Meta Estratégica |
| :---- | :---- | :---- |
| **Financiera (RevenueCat)** | **Trial Conversion Rate** | Mide el porcentaje de usuarios que completan los 7 días de prueba y pasan al cobro de \$14.99 USD. Meta: 48.8% (Estándar para Opt-out con tarjeta). |
| **Financiera (RevenueCat)** | **Churn Rate (Cancelaciones)** | Identifica la tasa de abandono mensual. El principal motivo de churn en apps de suscripción es la "falta de uso" o ruptura del bucle de hábito. |
| **Producto (PostHog)** | **Keyboard Activation Drop-off** | Mide cuántos usuarios instalan la app pero abandonan en el modal de activación de Android por fricción técnica o alertas de seguridad. |
| **Producto (PostHog)** | **Paste Actions per User (DAU)** | Cuantifica la frecuencia con la que el usuario despliega el teclado y pega un recorte. Valida si la herramienta se ha convertido en memoria muscular digital. |
| **Técnica (Supabase/PostHog)** | **Sync Latency & Success Rate** | Mide el tiempo en milisegundos que toma el WorkManager en subir un objeto a Cloudflare Edge y el porcentaje de fallos por falta de conectividad. |

#### **2\. Arquitectura del Módulo Analítico**

El sistema de rastreo debe abstraerse detrás de una interfaz (AnalyticsTracker) en la capa core para evitar acoplar la lógica de negocio a un SDK específico. **Regla de oro:** El texto copiado (textContent) jamás debe enviarse a ningún servicio de analíticas. Solo se registrarán los identificadores de evento y metadatos anónimos.

Fragmento de código  
architecture-beta  
    group app(logo:android, title: "Aplicación Android (Capa Presentación)")  
    group core(logo:gears, title: "Core (Infraestructura)")  
    group vendors(logo:cloud, title: "Servicios Externos")

    service ui(logo:mobile, title: "Flujo UI (Onboarding / Ajustes)") in app  
    service keyboard(logo:keyboard, title: "Teclado (Eventos de uso)") in app  
      
    service tracker(logo:puzzle, title: "AnalyticsTracker (Interface)") in core  
    service posthog(logo:posthog, title: "PostHogTracker (Impl)") in core  
    service revcat\_event(logo:server, title: "RevenueCatTracker (Impl)") in core

    service ph\_api(logo:api, title: "PostHog API (Product Metrics)") in vendors  
    service rc\_api(logo:api, title: "RevenueCat (Financial Metrics)") in vendors

    ui:R \--\> L:tracker  
    keyboard:B \--\> T:tracker  
      
    tracker:R \--\> L:posthog  
    tracker:R \--\> L:revcat\_event  
      
    posthog:R \--\> L:ph\_api  
    revcat\_event:R \--\> L:rc\_api

#### **3\. Actualización del Prompt para Opencode**

Copia y pega el siguiente *prompt* actualizado en la interfaz de Opencode. Este texto incluye la integración del módulo de analíticas (Fase 6\) y los requerimientos de abstracción.  
**Actúa como un Desarrollador Android Senior (Kotlin), Arquitecto de Software y Experto en Clean Architecture.**  
Tu objetivo es programar la versión 1 de **FluxBoard**, un gestor de portapapeles profesional para Android. En la raíz de este *workspace* se encuentra el archivo Arquitectura FluxBoard version 1.md. Este documento contiene el mapa exacto del sistema, los contratos de datos, las restricciones de privacidad del sistema operativo y las directrices UI/UX.  
**Reglas Críticas de Desarrollo:**

> * **No inventes arquitecturas:** Cíñete estrictamente a la estructura de paquetes definida en el archivo MD (Domain, Data, Presentation).  
> * **Offline-First:** El teclado siempre debe leer y escribir desde Room de manera síncrona. La interacción con Supabase y Cloudflare se hará exclusivamente a través de WorkManager.  
> * **Privacidad Absoluta:** Bajo NINGUNA circunstancia envíes el contenido del portapapeles (textContent) a herramientas de analíticas o logs de consola.  
> * **Fidelidad Visual:** Utiliza exactamente los códigos HEX de color y las fuentes (Inter y JetBrains Mono) estipuladas.  
> * **Desarrollo Iterativo:** No intentes generar toda la aplicación en una sola respuesta. Sigue las Fases de Ejecución y detente al final de cada una para solicitar mi revisión.

**Fases de Ejecución (Paso a Paso):**

* **Fase 1: Andamiaje y Dependencias.** Lee Arquitectura FluxBoard version 1.md. Configura el archivo build.gradle.kts (app y project). Añade dependencias para Jetpack Compose, Room, Dagger-Hilt, WorkManager, Supabase Kotlin Client, Ktor, RevenueCat y PostHog (Analíticas). Genera la estructura de carpetas com.fluxboard.app según el File Tree. *Detente y espera mi confirmación.*  
* **Fase 2: Dominio y Base de Datos Local.** Crea los modelos de datos limpios (ClipItem, SubscriptionTier) y las interfaces de los repositorios. Implementa FluxDatabase y ClipDao usando Room. Asegúrate de mapear correctamente las entidades locales a los modelos de dominio. *Detente y espera mi confirmación.*  
* **Fase 3: Servicio Central (Teclado).** Crea la clase FluxBoardKeyboard heredando de InputMethodService. Implementa la lógica para interceptar el portapapeles cuando la vista se activa y para inyectar texto mediante InputConnection. Implementa el diseño de las "Clip Cards" en Jetpack Compose envueltas en un AbstractComposeView. *Detente y espera mi confirmación.*  
* **Fase 4: Flujo Principal (App UI).** Desarrolla las pantallas de Onboarding (1 y 2), el Muro de Pago (Pantalla 3\) y la pantalla modal de activación técnica usando Jetpack Compose. Implementa la navegación y vincula los botones a la lógica de RevenueCat. Aplica el esquema de colores y tipografía con precisión. *Detente y espera mi confirmación.*  
* **Fase 5: Sincronización (Backend).** Implementa SyncWorker con WorkManager. Escribe la lógica para leer recortes no sincronizados en Room, subirlos al Edge (Cloudflare KV) y registrar la metadata en Supabase mediante sus respectivos clientes remotos. *Detente y espera mi confirmación.*  
* **Fase 6: Infraestructura de Analíticas (Nuevo).** Crea una interfaz AnalyticsTracker en core/analytics. Implementa esta interfaz usando el SDK de PostHog para eventos de producto (ej. Keyboard\_Activated, Paywall\_Viewed) y enlaza el ID de usuario de Supabase con RevenueCat para métricas financieras. Inyecta este tracker en los ViewModels y el Servicio de Teclado. *Detente y espera mi confirmación.*

Comienza leyendo el archivo Arquitectura FluxBoard version 1.md y ejecuta inmediatamente la **Fase 1**. No expliques lo que vas a hacer, simplemente genera los archivos de configuración y la estructura de directorios.  
