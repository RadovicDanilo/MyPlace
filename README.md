# MyPlace

A real-time collaborative pixel canvas inspired by [Reddit's r/Place](https://redditinc.com/blog/how-we-built-rplace).

---

## 🎯 Project Goals

* Recreate the core features of Reddit's **r/Place** in a scalable, real-time application.
* Design a backend capable of supporting **thousands of simultaneous users** using **WebSockets**.
* Efficiently store and broadcast pixel changes using **bit-level data structures** via **Redis**.
* Build an intuitive, smooth, zoomable/pannable frontend canvas interface with **React + TailwindCSS**.

---

## 🧱 Tech Stack

| Layer     | Stack                             |
| --------- | --------------------------------- |
| Frontend  | React + Vite + TailwindCSS        |
| Backend   | Kotlin + Spring Boot + WebSockets |
| Database  | PostgreSQL                        |
| Realtime  | Redis Bitfields                   |
| Container | Docker + Docker Compose           |

---

## ✨ Key Features

### 🎨 Collaborative Canvas

* A fixed-size canvas (1024x1024) where users can place pixels in real time with a cooldown.
* 16 available colors, each encoded using **4 bits**, stored efficiently in Redis using the [`BITFIELD`](https://redis.io/commands/bitfield/) command.
* Canvas state is memory-efficient and can scale to millions of pixels with minimal Redis memory footprint.

### 📡 Real-Time Updates

* WebSocket-based broadcasting: every pixel update is instantly sent to all connected clients.
* No polling, no delay - true real-time interaction.

### 🔍 Frontend UX

* Smooth **zooming and panning** via CSS transforms.
* Live updates reflected directly on canvas with minimal redraws.
* Pixel-perfect rendering using `imageRendering: "pixelated"`. 

### 👥 Scalable Backend

* Redis handles high-throughput pixel writes and reads with atomic operations.
* PostgreSQL stores persistent user data, such as authentication info.
* Modular Spring Boot backend uses Kotlin to keep the codebase concise and expressive.

---

## 🚀 Getting Started

### Prerequisites

* Docker + Docker Compose
* Node.js (if building frontend manually)

### Run the stack:

```bash
docker compose up --build
```

Visit the frontend at [http://localhost:5173](http://localhost:5173).

---

## ⚙️ Environment

Create a `.env` file in the root with:

```env
SPRING_PROFILES_ACTIVE=prod
POSTGRES_DB=myplace
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
OAUTH_JWT_SECRET=<your-secret>
```

---

## 🧪 Developer Notes

* Canvas state is ephemeral and stored in **Redis**, but could be snapshotted to disk if persistence were needed.
* Scaling horizontally is possible — Redis and WebSocket brokers can be clustered.
* Canvas updates are atomic thanks to Redis’ bit-level operations.

---

## 📚 References

* Reddit r/Place internals: [https://redditinc.com/blog/how-we-built-rplace](https://redditinc.com/blog/how-we-built-rplace)
* Redis Bitfields: [https://redis.io/commands/bitfield/](https://redis.io/commands/bitfield/)
* Spring WebSocket Docs: [https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket)
