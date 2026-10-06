# Project Contract

## Build And Test

- Download agent binaries: `./download-agent.sh <nezha|komari> <arm64|arm>`
- Verify binary versions: `./download-agent.sh --version [agent] [arch]`
- Compile Kotlin / Typecheck: `./gradlew compileDebugKotlin`
- Unit tests: `./gradlew testDebugUnitTest`
- Lint checks: `./gradlew lintDebug`
- Build debug APK: `./gradlew assembleDebug`
- Build release APK: `./gradlew assembleRelease` (requires signing configuration)
- Full check suite: `./gradlew check`

## Architecture Boundaries

- Agent abstractions live in `app/src/main/java/now/link/agent/` (`AgentType`, `AgentConfiguration`, `AgentManager`, `AgentManagerFactory`, `UnifiedConfigurationManager`)
- Foreground service & process lifecycle live in `app/src/main/java/now/link/service/` (`UnifiedAgentService`, `UnifiedAgentTileService`)
- UI screens and composables live in `app/src/main/java/now/link/ui/` (`screens/`, `components/`, `theme/`)
- Presentation & state management live in `app/src/main/java/now/link/viewmodel/` (`MainViewModel`, `MainScreenUiState`)
- Utility & system wrappers live in `app/src/main/java/now/link/utils/` (`RootUtils`, `LogManager`, `SPUtils`, `AutoStartManager`, `BootReceiver`)
- App update logic lives in `app/src/main/java/now/link/update/` (`UpdateManager`, `UpdateInfo`)
- Pre-compiled native agent binaries are placed in `app/src/main/jniLibs/{abi}/lib{binary-name}.so` and resolved via `context.applicationInfo.nativeLibraryDir`
- Do not place process execution, shell commands, or service lifecycle logic inside UI composables or ViewModels
- Do not read or write `SPUtils` directly in Composable screens; mediate through `MainViewModel` and `UnifiedConfigurationManager`
- Service state and errors must be communicated via `ServiceStatusManager` broadcasts and `StateFlow`, never through direct Activity-to-Service tight coupling

## Coding Conventions

- Target Java 11 bytecode compatibility with Java 17 toolchain; adhere to `.editorconfig` (4-space indent, max line length 120)
- Dispatch all process spawning, root checks, file I/O, and network requests on `Dispatchers.IO`
- Manage service background execution using `CoroutineScope(SupervisorJob() + Dispatchers.IO)` and ensure proper cancellation
- Expose immutable `StateFlow` from ViewModels for Jetpack Compose UI state; avoid mutable state leakage
- Route all app and agent process output to `LogManager` (`LogManager.d`, `LogManager.w`, `LogManager.e`) to feed the in-app log viewer ring buffer; avoid raw `println()`
- Always consume process standard output and standard error asynchronously to prevent process pipe buffer deadlocks
- Always provide non-root execution fallback when root privileges are unavailable or fail
- Set `SSL_CERT_DIR=/system/etc/security/cacerts` in the agent execution environment for Android TLS certificate verification
- Keep English (`res/values/strings.xml`) and Simplified Chinese (`res/values-zh-rCN/strings.xml`) string resources strictly synchronized for all user-facing strings

## Safety Rails

## NEVER

- Commit `keystore.jks`, `signing.properties`, or CI secrets
- Upgrade to Gradle 9.0 without resolving AGP and plugin deprecations
- Execute shell commands, agent processes, or file I/O on the main/UI thread
- Hardcode binary paths or ABI directories; always resolve dynamically via `nativeLibraryDir`
- Remove non-root fallback execution logic or assume root is always present
- Modify `AndroidManifest.xml` permissions or service types (`dataSync`) without verifying Android 13+ foreground service compliance
- Commit code without running unit tests and checking for compilation errors

## ALWAYS

- Show diff before committing
- Update both English (`strings.xml`) and Chinese (`strings.xml` in `values-zh-rCN`) when adding or modifying user-facing text
- Clean up background processes (`process.destroyForcibly()`, `pkill`) and WakeLocks in `onDestroy()` / stop handlers
- Verify agent download scripts (`download-agent.sh`) when introducing new agent types or architecture targets
- Preserve lint baseline integrity (`app/lint-baseline.xml`) or fix newly introduced lint errors

## Verification

- Unit tests: `./gradlew testDebugUnitTest`
- Code compilation: `./gradlew compileDebugKotlin`
- Android Lint: `./gradlew lintDebug`
- Debug build: `./gradlew assembleDebug`
- Agent binary check: `./download-agent.sh --version nezha arm64`
- UI changes: Verify Compose previews or test on device/emulator across light/dark themes and system orientations

## Compact Instructions

Preserve:

1. Architecture decisions (NEVER summarize)
2. Modified files and key changes
3. Current verification status (pass/fail commands)
4. Open risks, TODOs, rollback notes
