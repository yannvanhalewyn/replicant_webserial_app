# Replicant WebSerial App

A boilerplate application demonstrating Replicant with WebSerial API integration.

## Features

- Hello World screen built with Replicant
- WebSerial API integration for connecting to serial devices
- Real-time serial communication (send/receive data)
- Clean, modern UI

## Prerequisites

- Node.js and yarn
- Java (for ClojureScript compilation)

## Getting Started

1. Install dependencies:
```bash
npm install
```

2. Start the development server:
```bash
npm run dev
```

3. Open your browser to http://localhost:8080

## Usage

The app provides a simple interface to:
- Connect to a serial device (requires user gesture)
- Disconnect from the device
- Send test messages to the serial device
- Receive and log data from the serial device (check browser console)

## WebSerial API Notes

- WebSerial API is only available in Chrome, Edge, and Opera browsers
- The API requires HTTPS in production (localhost works for development)
- User interaction (button click) is required to request port access
- The default baud rate is set to 9600 (configurable in `src/app/webserial.cljs:19`)

## Project Structure

```
├── public/
│   └── index.html          # HTML entry point
├── src/
│   └── app/
│       ├── core.cljs       # Main app logic and UI
│       └── webserial.cljs  # WebSerial API integration
├── shadow-cljs.edn         # Shadow-cljs configuration
└── package.json            # Node dependencies
```

## Building for Production

```bash
npm run build
```

The compiled JavaScript will be in `public/js/`.
