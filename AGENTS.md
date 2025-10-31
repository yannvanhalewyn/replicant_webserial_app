# Agent Guidelines for Replicant WebSerial

## Build Commands
- **Dev server**: `bb dev` (starts shadow-cljs watch on port 8080)
- **Production build**: `bb build` (shadow-cljs release build)
- **CLJS REPL**: `bb cljs-repl` (connect to running dev server)

## Code Style
- **Formatting**: Use `cljfmt` with Cursive indentation style (cljfmt.edn)
- **Namespaces**: Use `:require` with aliases, alphabetically sorted
- **Private functions**: Use `defn-` prefix for implementation details
- **Naming**: Use kebab-case for functions/vars, namespace-qualified keywords for internal state (e.g., `::connection`)
- **Error handling**: Use `promesa.core/catch` for promise errors, log with `println` or `js/console.log`
- **State management**: Single atom (`defonce state`) with `swap!` for updates
- **UI**: Replicant hiccup-style vectors with event handlers as `{:on {:click [[:event/name args]]}}`
- **Events**: Use multimethods dispatching on event keyword (e.g., `:webserial/connect`)
- **Async**: Use `promesa.core` (aliased as `p`) with `p/then` and `p/catch` chains

## Project Structure
- `src/app/core.cljs` - Main app logic, state, UI, and event handlers
- `src/app/webserial.cljs` - WebSerial API integration (connect/disconnect/send)
- `src/app/preload.cljs` - Dev tooling preload (devtools)
- `public/` - Static assets and compiled JS output

## Dependencies
- Replicant for UI rendering
- Promesa for promise handling
- Shadow-cljs for build tooling
